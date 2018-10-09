package com.ppdai.infrastructure.demo.provider;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ppdai.infrastructure.demo.dto.RadarDemoRequest;
import com.ppdai.infrastructure.demo.dto.RadarDemoResponse;
import com.ppdai.infrastructure.radar.biz.common.util.IPUtil;

@RestController
public class RadarProviderController {
	@PostMapping(value = "/demo")
	public RadarDemoResponse demo(@RequestBody RadarDemoRequest request) {
		RadarDemoResponse response = new RadarDemoResponse();
		response.setResult(IPUtil.getLocalIP());
		return response;
	}
}
