package com.ppdai.infrastructure.ui.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ppdai.infrastructure.radar.biz.service.UserService;
import com.ppdai.infrastructure.ui.service.impl.LdapUserService;

@Configuration
public class RadarConfig {
	@Bean
	@ConditionalOnMissingBean
	public UserService defaultUser() {
		return new LdapUserService();
	}
}
