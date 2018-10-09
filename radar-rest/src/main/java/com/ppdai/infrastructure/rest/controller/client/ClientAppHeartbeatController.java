package com.ppdai.infrastructure.rest.controller.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.client.HeartBeatRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.HeartBeatResponse;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;

@RestController
@RequestMapping(RadarConstanst.INSTPRE)
public class ClientAppHeartbeatController {
	private static final Logger log = LoggerFactory.getLogger(ClientAppHeartbeatController.class);
	private final Map<Long, Boolean> mapAppPolling = new ConcurrentHashMap<>(2500);
	@Autowired
	private InstanceService instanceService;
	@Autowired
	private SoaConfig soaConfig;
	private ThreadPoolExecutor executor = null;
	private ThreadPoolExecutor executorRun = null;
	private volatile int heartBeatThreadSize = 5;
	// @Autowired
	// private Util util;

	@PostConstruct
	private void init() {
		heartBeatThreadSize = soaConfig.getHeartBeatThreadSize();
		executor = new ThreadPoolExecutor(1, 1, 3L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10),
				SoaThreadFactory.create("heartbeat", Thread.MAX_PRIORITY, true),
				new ThreadPoolExecutor.CallerRunsPolicy());

		executorRun = new ThreadPoolExecutor(heartBeatThreadSize, heartBeatThreadSize, 10L, TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(5000), SoaThreadFactory.create("heartbeat-run", Thread.MAX_PRIORITY - 1, true),
				new ThreadPoolExecutor.CallerRunsPolicy());
		soaConfig.registerChanged(new Runnable() {
			@Override
			public void run() {
				if (heartBeatThreadSize != soaConfig.getHeartBeatThreadSize()) {
					heartBeatThreadSize = soaConfig.getHeartBeatThreadSize();
					executorRun.setCorePoolSize(heartBeatThreadSize);
				}

			}
		});
		executor.execute(() -> {
			heartbeat();
		});
	}

	@PreDestroy
	private void close() {
		try {
			executor.shutdown();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void heartbeat() {
		while (true) {
			if (mapAppPolling.size() > 0) {
				log.info("doHeartBeat_start");
				Transaction catTransaction = null;
				try {					
					Map<Long, Boolean> map = new HashMap<>(mapAppPolling);
					catTransaction = Tracer.newTransaction("Service",
							"heartbeatBatch-" + (map.size()-map.size()%10));
					List<List<Long>> idss = Util.split(new ArrayList<>(map.keySet()),
							soaConfig.getHeartbeatBatchSize());
					CountDownLatch countDownLatch = new CountDownLatch(idss.size());
					for (List<Long> ids : idss) {
						executorRun.execute(() -> {
							doHeartbeat(ids);
							countDownLatch.countDown();
						});
						for (Long id : ids) {
							mapAppPolling.remove(id);
						}
					}
					countDownLatch.await();
					catTransaction.setStatus(Transaction.SUCCESS);
				} catch (Exception e) {
					catTransaction.setStatus(e);
				} finally {
					catTransaction.complete();
				}
				log.info("doHeartBeat_end");
			}
			// 通过随机的方式来避免数据库的洪峰压力
			Util.sleep(soaConfig.getHeartbeatSleepTime());
		}

	}

	private void doHeartbeat(List<Long> ids) {
		Transaction catTransaction = null;
		try {
			catTransaction = Tracer.newTransaction("Service",
					"/api/client/app/instance/heartbeat-" + soaConfig.getHeartbeatBatchSize());
			instanceService.heartBeat(ids);
			if (soaConfig.isFullLog()) {
				ids.forEach(t1 -> {
					logMethod(t1, " heart end");
				});
			}
			catTransaction.setStatus(Transaction.SUCCESS);

		} catch (Exception e) {
			log.error("heartBeatfail失败", e);
			catTransaction.setStatus(e);
		}
		catTransaction.complete();
	}

	private void logMethod(long id, String action) {
		log.info(soaConfig.getLogPrefix() + " is {}", id, action);
	}

	// 发送心跳，直接返回
	@PostMapping("/heartbeat")
	public HeartBeatResponse heartBeat(@RequestBody HeartBeatRequest request) {
		HeartBeatResponse response = new HeartBeatResponse();
		response.setSuc(true);
		response.setHeartbeatTime(soaConfig.getHeartBeatTime());
		if (soaConfig.isFullLog()) {
			logMethod(request.getInstanceId(), " heart_begin");
		}
		if (soaConfig.getHeartBeatAsyn()) {
			asynHeartBeat(request);
		} else {
			synHeartBeat(request);
		}
		if (soaConfig.isFullLog()) {
			logMethod(request.getInstanceId(), " heart_end_" + response.getHeartbeatTime());
		}
		return response;
	}

	private void synHeartBeat(HeartBeatRequest request) {
		doHeartbeat(Arrays.asList(request.getInstanceId()));
	}

	private void asynHeartBeat(HeartBeatRequest request) {
		try {
			if (request != null) {
				if (request.getInstanceId() > 0) {
					mapAppPolling.put(request.getInstanceId(), true);
				}
				if (!CollectionUtils.isEmpty(request.getInstanceIds())) {
					request.getInstanceIds().forEach(t1 -> {
						mapAppPolling.put(t1, true);
					});
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
