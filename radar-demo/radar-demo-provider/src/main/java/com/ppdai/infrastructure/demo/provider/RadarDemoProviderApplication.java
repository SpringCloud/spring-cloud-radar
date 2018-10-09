package com.ppdai.infrastructure.demo.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@Import(value={BoostracpScanConfig.class})
public class RadarDemoProviderApplication {
	public static void main(String[] args) {
		SpringApplication.run(RadarDemoProviderApplication.class, args);
	}
}
