package com.ppdai.infrastructure.rest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.ppdai.infrastructure.radar.biz.polling.InstanceTimeOutCleaner;
import com.ppdai.infrastructure.radar.biz.service.AppCacheService;

@Component
public class SoaShutdownListener implements ApplicationListener<ContextClosedEvent>{
	private static final Logger log = LoggerFactory.getLogger(SoaShutdownListener.class);
	@Autowired
	private AppCacheService servCacheService;
	@Autowired
	private InstanceTimeOutCleaner instanceTimeOutCleaner;
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		try{
		servCacheService.stop();
		instanceTimeOutCleaner.stop();
		log.info("soa客户端关闭！");
		} catch (Exception e) {
			log.error("soaclosederror",e);
		}	
	}
}
