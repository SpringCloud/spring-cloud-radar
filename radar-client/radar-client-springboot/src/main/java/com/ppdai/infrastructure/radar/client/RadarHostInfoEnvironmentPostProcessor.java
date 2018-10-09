package com.ppdai.infrastructure.radar.client;

import java.util.LinkedHashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ppdai.infrastructure.radar.biz.common.util.IPUtil;

@Component
public class RadarHostInfoEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
	// Before ConfigFileApplicationListener
	private int order = ConfigFileApplicationListener.DEFAULT_ORDER + 11;

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String netCard = environment.getProperty("radar.network.netCard", "");
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		// map.put("spring.cloud.client.ipAddress",IPUtil.getLocalIP(getNetCard(environment)));
		if (StringUtils.isEmpty(environment.getProperty("radar.instance.host"))) {
			map.put("spring.cloud.client.ipAddress", IPUtil.getLocalIP(netCard));
		} else {
			map.put("spring.cloud.client.ipAddress", environment.getProperty("radar.instance.host"));
		}
		MapPropertySource propertySource = new MapPropertySource("radarClientHostInfo", map);
		environment.getPropertySources().addLast(propertySource);
	}

}
