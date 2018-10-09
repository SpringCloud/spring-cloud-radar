package com.ppdai.infrastructure.radar.biz.dto.client;

import java.util.Map;

import com.ppdai.infrastructure.radar.biz.dto.BaseResponse;
import com.ppdai.infrastructure.radar.biz.dto.base.AppDto;


public class GetGateServiceResponse extends BaseResponse {
	//key为服务名
	private Map<String, AppDto> app;

	public Map<String, AppDto> getApp() {
		return app;
	}

	public void setApp(Map<String, AppDto> app) {
		this.app = app;
	}	
}
