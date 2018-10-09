package com.ppdai.infrastructure.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
// @EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true)
// @EnableTransactionManagement
// @EnableAspectJAutoProxy
// @EnableApolloConfig({"application","基础框架.microservice.starter.v1"})
public class RestApplication {
	public static void main(String[] args) {
		SpringApplication.run(RestApplication.class, args);
	}

}
