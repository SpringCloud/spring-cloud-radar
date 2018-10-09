package com.ppdai.infrastructure.rest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.ppdai.infrastructure.radar.biz.polling.InstanceTimeOutCleaner;
import com.ppdai.infrastructure.radar.biz.service.AppCacheService;
import com.ppdai.infrastructure.radar.biz.service.AppClusterService;
import com.ppdai.infrastructure.radar.biz.service.AppService;
import com.ppdai.infrastructure.rest.controller.client.ClientAppNotifyController;

@Component
public class SoaBootstrapListener implements ApplicationListener<ContextRefreshedEvent>, Ordered {
	private static final Logger log = LoggerFactory.getLogger(SoaBootstrapListener.class);
	private static boolean isInit = false;
	@Autowired
	private ClientAppNotifyController clientServ;
	@Autowired
	private InstanceTimeOutCleaner instanceTimeOutCleaner;

	@Autowired
	private AppCacheService appCacheService;

	@Autowired
	private AppService appService;

	
	@Autowired
	private AppClusterService appClusterService;


	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (!isInit) {
			try {
				//注册事件
				appCacheService.addListener(clientServ);
				appCacheService.addListener(appService);
				appCacheService.addListener(appClusterService);
				//开启定时内存数据同步
				appCacheService.start();
				//开启定时数据清理
				instanceTimeOutCleaner.start();
				appService.startCache();
				// appClusterService.startCache();
				isInit = true;
				log.info("soa初始化成功！");
			} catch (Exception e) {
				log.error("soa初始化异常", e);
				throw e;
			}
		}

	}



}
