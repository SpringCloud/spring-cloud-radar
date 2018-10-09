package com.ppdai.infrastructure.demo.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ppdai.infrastructure.demo.dto.RadarDemoRequest;
import com.ppdai.infrastructure.demo.dto.RadarDemoResponse;

@RestController
public class RadarConsumerController {	
	@Autowired
	private RestTemplate restTemplate;

	@GetMapping(value = "/demo")
	public RadarDemoResponse demo() {
		RadarDemoRequest radarDemoRequest = new RadarDemoRequest();
		radarDemoRequest.setTest("hello-world");
		//1010111 为provider的appid
		return restTemplate.postForEntity("http://1010111/demo", radarDemoRequest, RadarDemoResponse.class).getBody();
	}
}
