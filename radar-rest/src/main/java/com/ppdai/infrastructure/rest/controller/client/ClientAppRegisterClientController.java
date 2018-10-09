package com.ppdai.infrastructure.rest.controller.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterClientRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterClientResponse;
import com.ppdai.infrastructure.radar.biz.service.AppClientService;

//暂时不用
@RestController
@RequestMapping(RadarConstanst.APPPRE)
public class ClientAppRegisterClientController {
	private static final Logger log = LoggerFactory.getLogger(ClientAppRegisterInstanceController.class);
	private final BlockingQueue<RegisterClientRequest> mapAppPolling = new ArrayBlockingQueue<>(1000);
	@Autowired
	private AppClientService appClientService;
	@Autowired
	private SoaConfig soaConfig;
	private int threadSize = 5;
	private ThreadPoolExecutor executor = null;

	@PostConstruct
	private void init() {
		threadSize = soaConfig.getRegisterClientThreadSize();		
		executor = new ThreadPoolExecutor(threadSize+1,threadSize+1,3L,TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), SoaThreadFactory.create("registerClient", true),new ThreadPoolExecutor.DiscardPolicy());
		executor.execute(() -> {
			registerClient();
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
	private void registerClient() {
		log.info("doRegisterClient");
		while (true) {
			try {
				if (mapAppPolling.size() > 0) {
					CountDownLatch countDownLatch = new CountDownLatch(soaConfig.getRegisterClientThreadSize() - 1);
					for (int i = 0; i < soaConfig.getRegisterClientThreadSize() - 1; i++) {
						executor.execute(() -> {
							doRegisterClient(countDownLatch);
						});						
					}
					countDownLatch.await();
				}

			} catch (Exception e) {
				// TODO: handle exception
			}
			Util.sleep(soaConfig.getRegisterClientSleepTime());
		}
	}

	private void doRegisterClient(CountDownLatch countDownLatch) {
		Transaction catTransaction = null;
		try {
			RegisterClientRequest request = mapAppPolling.poll();
			if (request == null) {				
				return;
			}
			catTransaction = Tracer.newTransaction("Service", "/api/client/app/instance/registerClient");
			appClientService.registerClient(request.getConsumerCandAppId(),request.getConsumerClusterName(),request.getProviderCandAppIds());
			RegisterClientResponse response = new RegisterClientResponse();
			response.setSuc(true);
			log.info("注册客户端成功,参数为:{}，返回{}，结束", JsonUtil.toJsonNull(request), JsonUtil.toJsonNull(response));
			catTransaction.setStatus(Transaction.SUCCESS);

		} catch (Exception e) {
			RegisterClientResponse response = new RegisterClientResponse();
			log.error("注册客户端失败registerfail", e);
			response.setSuc(false);
			response.setMsg("注册客户端失败");
			catTransaction.setStatus(e);
		}finally {
			countDownLatch.countDown();
			if(catTransaction != null){
				catTransaction.complete();
			}
		}
	}

	@PostMapping("/registerClient")
	public RegisterClientResponse registerClient(@RequestBody RegisterClientRequest request) {
		RegisterClientResponse response = new RegisterClientResponse();
		response.setSuc(true);
		try {
			mapAppPolling.put(request);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return response;
	}

}
