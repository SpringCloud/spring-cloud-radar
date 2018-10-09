package com.ppdai.infrastructure.radar.biz.dto.client;

import java.util.Map;

import com.ppdai.infrastructure.radar.biz.dto.BaseResponse;
import com.ppdai.infrastructure.radar.biz.dto.base.AppDto;


public class GetAppResponse extends BaseResponse {
	//key为服务名
	private Map<String, AppDto> app;
	//为0的时候，不用sleep，当sleepTime不等于0的时候sleep 这个毫秒数，只是针对长连接的时候
	private int sleepTime;
	public Map<String, AppDto> getApp() {
		return app;
	}
	public void setApp(Map<String, AppDto> app) {
		this.app = app;
	}
	public int getSleepTime() {
		return sleepTime;
	}
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	
}
