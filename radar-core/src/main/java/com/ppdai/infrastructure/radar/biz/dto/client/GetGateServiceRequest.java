package com.ppdai.infrastructure.radar.biz.dto.client;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;


public class GetGateServiceRequest extends BaseRequest {
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getGateWay() {
		return gateWay;
	}
	public void setGateWay(String gateWay) {
		this.gateWay = gateWay;
	}
	public boolean isContainOffline() {
		return containOffline;
	}
	public void setContainOffline(boolean containOffline) {
		this.containOffline = containOffline;
	}
	private String ip;	
	//网关过滤，一次只能过滤一个
	private String gateWay;
	private boolean containOffline; 
}
