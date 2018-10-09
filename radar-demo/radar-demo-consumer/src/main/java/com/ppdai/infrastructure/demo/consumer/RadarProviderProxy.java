package com.ppdai.infrastructure.demo.consumer;

import java.util.Map;

import com.ppdai.infrastructure.demo.dto.RadarDemoRequest;
import com.ppdai.infrastructure.demo.dto.RadarDemoResponse;
import com.ppdai.infrastructure.radar.client.http.common.annotation.SoaService;
import com.ppdai.infrastructure.radar.client.http.proxy.ISoaService;
import com.ppdai.infrastructure.radar.client.http.proxy.SoaServiceFactory;

@SoaService(appId = "1010111", appName = "radar-demo-provider")
public class RadarProviderProxy {
	private static ISoaService _serviceImpl = SoaServiceFactory.create(RadarProviderProxy.class);

	public static RadarDemoResponse demo(RadarDemoRequest request) {
		return _serviceImpl.request(RadarDemoResponse.class, "/demo", request);
	}

	public static RadarDemoResponse demo(RadarDemoRequest request, Map<String, String> header) {
		return _serviceImpl.request(RadarDemoResponse.class, "/demo", request, header);
	}
}