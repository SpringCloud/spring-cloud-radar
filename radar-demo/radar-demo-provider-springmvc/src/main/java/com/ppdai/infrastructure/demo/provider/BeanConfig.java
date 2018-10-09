package com.ppdai.infrastructure.demo.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ppdai.infrastructure.radar.client.config.RadarClientConfig;

@Configuration
public class BeanConfig {
	private static final Logger logger = LoggerFactory.getLogger(BeanConfig.class);
	@Bean
	public RadarClientConfig radarClientConfig(){
		RadarClientConfig radarClientConfig=new RadarClientConfig("http://localhost:8080");		
		radarClientConfig.setAppName("radar-demo-provider-1");
		radarClientConfig.setCandAppId("1010111");
		radarClientConfig.setClusterName("default");
		radarClientConfig.setPort(8085);
		radarClientConfig.setCandInstanceId("radar-demo-provider-5");
		radarClientConfig.getTags().put("test1", "test1");
		radarClientConfig.getTags().put("test2", "test2");
		
		return radarClientConfig;
	}
}
