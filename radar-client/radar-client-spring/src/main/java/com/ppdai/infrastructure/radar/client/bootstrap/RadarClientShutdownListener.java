package com.ppdai.infrastructure.radar.client.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;

import com.ppdai.infrastructure.radar.client.DiscoveryClient;
import org.springframework.stereotype.Component;

@Component
public class RadarClientShutdownListener implements ApplicationListener<ContextClosedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(RadarClientShutdownListener.class);
	@Autowired
	private Environment env;
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		if ("true".equals(env.getProperty("radar.instance.registerSelf", "true"))) {
			try {
				DiscoveryClient.getInstance().deregister();
				logger.info("注册退出！");
			} catch (Exception e) {
				logger.error("deregister_error", e);
			}
		}
	}

}
