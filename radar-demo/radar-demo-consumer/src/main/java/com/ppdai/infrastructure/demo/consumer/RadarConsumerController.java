package com.ppdai.infrastructure.demo.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ppdai.infrastructure.demo.dto.RadarDemoRequest;
import com.ppdai.infrastructure.demo.dto.RadarDemoResponse;

@RestController
public class RadarConsumerController {
	@GetMapping(value = "/demo")
	public RadarDemoResponse demo() {
		RadarDemoRequest radarDemoRequest=new RadarDemoRequest();
		radarDemoRequest.setTest("hello-world");
		return RadarProviderProxy.demo(radarDemoRequest);
	}
	@GetMapping(value = "/sub")
	public String sub() {		
		return "ok";
	}
}
