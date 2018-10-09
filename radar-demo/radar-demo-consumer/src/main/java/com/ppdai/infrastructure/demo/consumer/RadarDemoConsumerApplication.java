package com.ppdai.infrastructure.demo.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.ppdai.infrastructure.radar.client.BootstrapScanConfig;

@SpringBootApplication
@ComponentScan(basePackageClasses={BootstrapScanConfig.class,RadarDemoConsumerApplication.class})
public class RadarDemoConsumerApplication {
	public static void main(String[] args) {
		SpringApplication.run(RadarDemoConsumerApplication.class, args);
	}
}
