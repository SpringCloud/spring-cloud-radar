package com.ppdai.infrastructure.rest.controller.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.listener.AppReleaseListener;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.MetricSingleton;
import com.ppdai.infrastructure.radar.biz.common.trace.TraceFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.TraceMessage;
import com.ppdai.infrastructure.radar.biz.common.trace.TraceMessageItem;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppResponse;
import com.ppdai.infrastructure.radar.biz.service.AppService;

@RestController
@RequestMapping(RadarConstanst.APPPRE)
public class ClientAppNotifyController implements AppReleaseListener {
	private static final Logger log = LoggerFactory.getLogger(ClientAppNotifyController.class);
	private static final long TIMEOUT = 30 * 1000;// 30 seconds
	private final Map<GetAppRequest, DeferredResult<GetAppResponse>> mapAppPolling = new ConcurrentHashMap<>();

	@Autowired
	private AppService appService;

	private static AtomicLong longPollingCounter = new AtomicLong(0);
	@Autowired
	private SoaConfig soaConfig;
	private TraceMessage traceMessage3 = TraceFactory.getInstance("longDataPollingCount");
	

	@PostConstruct
	private void init() {
		MetricSingleton.getMetricRegistry().register(MetricRegistry.name("data.AppPollingCount"),
				new Gauge<Long>() {
					@Override
					public Long getValue() {
						return longPollingCounter.get();
					}
				});	
	}

	@GetMapping("/getAppPollingCount")
	public long getServicePollingCount() {
		return longPollingCounter.get();
	}

	@PostMapping("/getAppPolling")
	public DeferredResult<GetAppResponse> getServicePolling(@RequestBody GetAppRequest request) {
		GetAppResponse response = new GetAppResponse();
		response.setSuc(true);
		response.setSleepTime(RandomUtils.nextInt(50,2000));
		request.setInTime(System.currentTimeMillis());
		DeferredResult<GetAppResponse> deferredResult = new DeferredResult<>(TIMEOUT, response);
		GetAppResponse getApplicationResponse = doCheckServPolling(request);
		if (getApplicationResponse != null) {
			if (soaConfig.isFullLog()) {
				getFollowMsg(request, "getServicePolling direct end notify");
			}
			deferredResult.setResult(getApplicationResponse);
		} else {
			mapAppPolling.put(request, deferredResult);
			if (soaConfig.isFullLog()) {
				getFollowMsg(request, "getServicePolling wait notify");
			}
			long count = longPollingCounter.incrementAndGet();
			TraceMessageItem traceMessageItem = new TraceMessageItem();
			
			traceMessageItem.status = count + "";			
			if (count > soaConfig.getPollingSize()) {
				if (soaConfig.isFullLog()) {
					getFollowMsg(request, "getServicePolling exce size notify");
				}
				response.setSleepTime(RandomUtils.nextInt(50,2000));
				deferredResult.setResult(response);
				longPollingCounter.decrementAndGet();
				
			} else {
				deferredResult.onTimeout(() -> {
					getFollowMsg(request, "getServicePolling time out notify");
					logWatchedKeysToCat(request, "infrastructure.LongPoll.TimeOutKeys");
				});
				deferredResult.onCompletion(() -> {
					Transaction transaction = Tracer.newTransaction("Service", "getServicePolling");
					try {
						if (mapAppPolling.remove(request) != null) {
							if (soaConfig.isFullLog()) {
								getFollowMsg(request, "getServicePolling finished notify");
							}
							long count1 = longPollingCounter.decrementAndGet();							
							traceMessageItem.msg = count + "_" + count1;
							traceMessageItem.end();
							traceMessage3.add(traceMessageItem);
							logWatchedKeysToCat(request, "infrastructure.LongPoll.CompletionKeys");
						}
						transaction.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						transaction.setStatus(e);
					} finally {
						transaction.complete();
					}
				});
			}
		}
		return deferredResult;
	}

	private void getFollowMsg(GetAppRequest request, String action) {
		if (soaConfig.isFullLog()) {
			if (request != null && request.getAppVersion() != null) {
				for (String key : request.getAppVersion().keySet()) {
					log.info("app_{}_is_{},and request version is {}", key, action.replaceAll(" ", "_"),
							request.getAppVersion().get(key));
				}
			}
		}
	}

	@Override
	public void handleService() {
		try {
			notifyMessage();
		} catch (Exception e) {
		}
	}

	private void notifyMessage() {
		int notifyBatchSize = 0;
		Map<GetAppRequest, DeferredResult<GetAppResponse>> mapTemp = new HashMap<>(mapAppPolling);
		for (GetAppRequest request : mapTemp.keySet()) {
			try {
				notifyBatchSize++;
				GetAppResponse response = doCheckServPolling(request);
				if (response != null && mapAppPolling.containsKey(request)) {
					mapTemp.get(request).setResult(response);
				}
			} catch (Exception e) {

			}
			if (soaConfig.getNotifyWaitTime() > 0 && notifyBatchSize > soaConfig.getNotifyBatchSize()) {
				Util.sleep(soaConfig.getNotifyWaitTime());
			}
		}
	}

	private GetAppResponse doCheckServPolling(GetAppRequest request) {
		GetAppResponse response = appService.getApp(request);
		if (!CollectionUtils.isEmpty(response.getApp())) {
			return response;
		}
		return null;
	}

	private void logWatchedKeysToCat(GetAppRequest request, String eventName) {

	}
}