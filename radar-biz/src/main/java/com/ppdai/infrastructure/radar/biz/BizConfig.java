package com.ppdai.infrastructure.radar.biz;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.ppdai.infrastructure.radar.biz.common.trace.TraceFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.TraceFactory.TraceCheck;

//@EnableAutoConfiguration
@Configuration
@ComponentScan(basePackageClasses={BizConfig.class})
public class BizConfig {
	@Autowired
	private Environment env;
	@PostConstruct
	private void init(){
		TraceFactory.setTraceCheck(new TraceCheck() {			
			@Override
			public boolean isEnabled(String name) {
				if (env == null) {
					return false;
				} else {
					return "1".equals(env.getProperty("radar.trace.enable", "1")) && "1".equals(env.getProperty("radar.trace."+name, "1"));
				}
			}
		});
	}
}
