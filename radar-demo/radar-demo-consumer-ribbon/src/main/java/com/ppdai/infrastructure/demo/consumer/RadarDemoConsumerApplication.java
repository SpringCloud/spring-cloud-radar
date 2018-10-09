package com.ppdai.infrastructure.demo.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RadarDemoConsumerApplication {
	
//	@Bean	
//    @LoadBalanced //开启负债均衡的能力   
//    public RestTemplate restTemplate() 
//    {		
//		
//		//OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory=new OkHttp3ClientHttpRequestFactory();
//		
//		HttpComponentsClientHttpRequestFactory rf =new HttpComponentsClientHttpRequestFactory();
//			rf.setReadTimeout(1 * 1000);
//			rf.setConnectTimeout(1 * 1000);
//			rf.setConnectionRequestTimeout(2000);
//        return new RestTemplate(rf);
//    }
	public static void main(String[] args) {
		SpringApplication.run(RadarDemoConsumerApplication.class, args);
	}
}
