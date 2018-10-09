package com.ppdai.infrastructure.rest.controller.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.exception.SoaException;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.MetricSingleton;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;

@RestController
@RequestMapping(RadarConstanst.INSTPRE)
public class ClientAppRegisterInstanceController {
	
	private static final Logger log = LoggerFactory.getLogger(ClientAppRegisterInstanceController.class);
	private static final long TIMEOUT = 30 * 1000;// 30 seconds
	private final Map<RegisterInstanceRequest, DeferredResult<RegisterInstanceResponse>> mapAppPolling = new ConcurrentHashMap<>();

	@Autowired
	private InstanceService instanceService;
	@Autowired
	private SoaConfig soaConfig;
	private static AtomicLong registCounter = new AtomicLong(0);
	private volatile long registTime = 0;
	// private volatile long registerTime = 0;
	private volatile int threadSize = 5;
	private ThreadPoolExecutor executor = null;
	private volatile boolean isRunning=true;
	@PostConstruct
	private void init() {
		initMetric();
		threadSize = soaConfig.getRegisterInstanceThreadSize();
		executor = new ThreadPoolExecutor(threadSize, threadSize, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(50), SoaThreadFactory.create("registerInstance", true),
				new ThreadPoolExecutor.DiscardOldestPolicy());
		soaConfig.registerChanged(new Runnable() {
			@Override
			public void run() {
				if (threadSize != soaConfig.getRegisterInstanceThreadSize()) {
					threadSize = soaConfig.getRegisterInstanceThreadSize();
					log.info("RegisterInstanceThreadSize_Changed_{}", threadSize);
					executor.setMaximumPoolSize(threadSize);
					executor.setCorePoolSize(threadSize);
				}
			}
		});
		executor.execute(() -> {
			registerInstance();
		});
	}

	@PreDestroy
	private void close(){
		isRunning=false;
	}
	
	private void initMetric() {
		MetricSingleton.getMetricRegistry().register(MetricRegistry.name("data.registerInstanceCount"),
				new Gauge<Long>() {
					@Override
					public Long getValue() {
						return registCounter.get();
					}
				});
		MetricSingleton.getMetricRegistry().register(MetricRegistry.name("data.registerInstanceTime"),
				new Gauge<Long>() {
					@Override
					public Long getValue() {
						return registTime;
					}
				});
	}

	private void registerInstance() {
		log.info("doRegisterInstance");
		while (isRunning) {
			saveAysn();
			Util.sleep(soaConfig.getRegisterInstanceSleepTime());
		}
	}

	private void saveAysn() {
		try {
			Map<RegisterInstanceRequest, DeferredResult<RegisterInstanceResponse>> mapTemp = new HashMap<>(
					mapAppPolling);
			if (mapTemp.size() > 0) {
				// 先根据appid归组，一个appid下有多个实例，然后开启多线程进行数据库保存操作
				Map<Integer, RegisterInstanceBatch> mapSp = spBatch(mapTemp);
				for (RegisterInstanceBatch requests1 : mapSp.values()) {
					if (requests1.size() > 0) {
						CountDownLatch countDownLatch = new CountDownLatch(requests1.size());
						for (AppRegisterInstance requests : requests1.getRgBatch()) {
							executor.execute(() -> {
								doRegisterInstance(requests.getAppRg());
								countDownLatch.countDown();
							});
						}
						countDownLatch.await();
					}
				}
			}
		} catch (Exception e) {
			log.error("saveAysn_error", e);
		}

	}

	private Map<Integer, RegisterInstanceBatch> spBatch(
			Map<RegisterInstanceRequest, DeferredResult<RegisterInstanceResponse>> mapTemp) {
		// 归类，将相同appid的归为一类
		Map<String, AppRegisterInstance> mapAppIdRequest = new HashMap<>();
		mapTemp.keySet().forEach(t1 -> {
			if (!mapAppIdRequest.containsKey(t1.getCandAppId())) {
				mapAppIdRequest.put(t1.getCandAppId(), new AppRegisterInstance());
			}
			mapAppIdRequest.get(t1.getCandAppId()).add(t1);
		});
		// 归组，根据线程池的大小，将几个appid归为一组，这样可以多线程处理，提高吞吐
		Map<Integer, RegisterInstanceBatch> mapSp = new HashMap<>();
		// 线程池中有个线程用来触发检查任务,所以需要减一
		int pollSize = threadSize - 1;
		for (AppRegisterInstance requests : mapAppIdRequest.values()) {
			if (mapSp.size() == 0) {
				mapSp.put(1, new RegisterInstanceBatch());
			} else if (mapSp.get(mapSp.size()).size() == pollSize) {
				mapSp.put(mapSp.size() + 1, new RegisterInstanceBatch());
			}
			mapSp.get(mapSp.size()).add(requests);
			// doRegisterInstance(requests);
		}
		return mapSp;
	}

	// 批量注册
	private void doRegisterInstance(List<RegisterInstanceRequest> request1) {
		Transaction catTransaction = Tracer.newTransaction("Service",
				"/api/client/app/instance/registerInstanceS");
		try {
			Map<String, RegisterInstanceResponse> responses = instanceService.registerInstance(request1);
			request1.forEach(t1 -> {
				if (responses.containsKey(t1.getCandInstanceId()) && mapAppPolling.containsKey(t1)) {
					responses.get(t1.getCandInstanceId()).setCode(RadarConstanst.YES);
					mapAppPolling.get(t1).setResult(responses.get(t1.getCandInstanceId()));
				}
			});
			catTransaction.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			log.error("doRegisterInstances_error", e);
			catTransaction.setStatus(e);
		}
		catTransaction.complete();
	}

	@GetMapping("/registerInstanceCount")
	public long registerInstanceCount() {
		return registCounter.get();
	}

	@PostMapping("/registerInstance")
	public DeferredResult<RegisterInstanceResponse> registerInstance(@RequestBody RegisterInstanceRequest request) {
		RegisterInstanceResponse response = new RegisterInstanceResponse();
		response.setSuc(true);
		response.setCode(RadarConstanst.YES);
		checkVaild(request, response);
		if (!response.isSuc()) {
			log.info("registerInstance_check_error：" + response.getMsg() + ",输入参数为：" + JsonUtil.toJsonNull(request));
			DeferredResult<RegisterInstanceResponse> deferredResult = new DeferredResult<>(TIMEOUT,
					new RegisterInstanceResponse());
			response.setCode(RadarConstanst.YES);
			deferredResult.setResult(response);
			return deferredResult;
		} else {
			if (soaConfig.isAsyn()) {
				return registerAysn(request);
			} else {
				return registerSyn(request);
			}
		}
	}

	@PostMapping("/deRegisterInstance")
	public DeRegisterInstanceResponse deRegisterInstance(@RequestBody DeRegisterInstanceRequest request) {
		try {
			DeRegisterInstanceResponse response = instanceService.deRegister(request);
			if (request != null) {
				log.info(soaConfig.getLogPrefix() + "_{},参数为：{}下线成功，结束", request.getInstanceId(),
						JsonUtil.toJsonNull(request));
			}
			response.setCode(RadarConstanst.YES);
			return response;
		} catch (Exception e) {
			DeRegisterInstanceResponse response = new DeRegisterInstanceResponse();
			log.error("unRegistfail失败", e);
			response.setSuc(false);
			response.setCode(RadarConstanst.NO);
			response.setMsg("unRegist失败");
			throw new SoaException(response, e);
		}
	}

	// 同步单个注册
	private DeferredResult<RegisterInstanceResponse> registerSyn(RegisterInstanceRequest request) {
		RegisterInstanceResponse response;
		Transaction catTransaction = Tracer.newTransaction("Service",
				"/api/client/app/instance/registerInstance");
		try {
			response = instanceService.registerInstance(request);
			response.setCode(RadarConstanst.YES);
			DeferredResult<RegisterInstanceResponse> deferredResult = new DeferredResult<>(TIMEOUT, response);
			deferredResult.setResult(response);
			catTransaction.setStatus(Transaction.SUCCESS);
			return deferredResult;
		} catch (Exception e) {
			catTransaction.setStatus(e);
			response = new RegisterInstanceResponse();
			response.setCode(RadarConstanst.NO);
			response.setSuc(false);
			response.setMsg("registerInstanceSynFail");
			log.error("registerInstanceSynFail", e);
			return new DeferredResult<>(TIMEOUT, response);
		} finally {
			catTransaction.complete();
		}
	}

	private void checkVaild(RegisterInstanceRequest request, RegisterInstanceResponse registResponse) {
		instanceService.checkRegistVaild(request, registResponse);
	}

	private DeferredResult<RegisterInstanceResponse> registerAysn(RegisterInstanceRequest request) {
		try {
			RegisterInstanceResponse response = new RegisterInstanceResponse();
			response.setSuc(false);
			response.setCode(RadarConstanst.NO);
			DeferredResult<RegisterInstanceResponse> deferredResult = new DeferredResult<>(TIMEOUT, response);
			mapAppPolling.put(request, deferredResult);
			request.setInTime(System.currentTimeMillis());
			registCounter.incrementAndGet();
			logMethod(request, "registerInstance begin", "参数是:" + JsonUtil.toJsonNull(request));
			deferredResult.onTimeout(() -> {
				logMethod(request, "registerInstance tiemout");
			});
			deferredResult.onCompletion(() -> {
				try {
					if (mapAppPolling.remove(request) != null) {
						registCounter.decrementAndGet();
						registTime = System.currentTimeMillis() - request.getInTime();
						logMethod(request, "registerInstance end");
					}
				} catch (Exception e) {

				}
			});
			return deferredResult;
		} catch (Exception e) {
			RegisterInstanceResponse response = new RegisterInstanceResponse();
			// response.setHeartbeatTime(soaConfig.getHeartBeatTime());
			log.error("registerInstance_fail", e);
			response.setSuc(false);
			response.setCode(RadarConstanst.NO);
			response.setMsg("注册失败");
			logMethod(request, "registerInstance fail");
			try {
				if (mapAppPolling.containsKey(request)) {
					mapAppPolling.remove(request);
				}
			} catch (Exception e1) {
				// TODO: handle exception
			}
			throw new SoaException(response, e);
		}
	}

	private void logMethod(RegisterInstanceRequest request, String action) {
		log.info("{}_canappId_{}_canInstanceid_{}", action.replaceAll(" ", "_"), request.getCandAppId(),
				request.getCandInstanceId());
	}

	private void logMethod(RegisterInstanceRequest request, String action, String info) {
		log.info("{}_canappId_{}_canInstanceid_{},{}", action.replaceAll(" ", "_"), request.getCandAppId(),
				request.getCandInstanceId(), info);
	}

	// 几个appid归为一组
	class RegisterInstanceBatch {
		private List<AppRegisterInstance> rgBatch = new ArrayList<>();

		public int size() {
			return rgBatch.size();
		}

		public void add(AppRegisterInstance appRegisterInstance) {
			rgBatch.add(appRegisterInstance);
		}

		public List<AppRegisterInstance> getRgBatch() {
			return rgBatch;
		}

		public void setRgBatch(List<AppRegisterInstance> rgBatch) {
			this.rgBatch = rgBatch;
		}

	}

	// 一组相同appid的实例集合
	class AppRegisterInstance {
		private List<RegisterInstanceRequest> appRg = new ArrayList<>();

		public int size() {
			return appRg.size();
		}

		public void add(RegisterInstanceRequest registerInstanceRequest) {
			appRg.add(registerInstanceRequest);
		}

		public List<RegisterInstanceRequest> getAppRg() {
			return appRg;
		}

		public void setAppRg(List<RegisterInstanceRequest> appRg) {
			this.appRg = appRg;
		}

	}
}
