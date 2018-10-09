package com.ppdai.infrastructure.radar.client.http.proxy;

public class ServiceMeta {
	private String appId;	
	
	private String serviceName;
	public ServiceMeta(String appId,String serviceName){
		this.appId=appId;
		this.serviceName=serviceName;
	}
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
}
