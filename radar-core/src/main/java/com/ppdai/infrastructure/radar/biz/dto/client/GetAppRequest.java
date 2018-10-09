package com.ppdai.infrastructure.radar.biz.dto.client;

import java.util.Map;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;

public class GetAppRequest extends BaseRequest {
	private Map<String,Long> appVersion;	
	private String ip;	
	private boolean containOffline;
	private long inTime=0;
	
	public Map<String, Long> getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(Map<String, Long> appVersion) {
		this.appVersion = appVersion;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public boolean isContainOffline() {
		return containOffline;
	}
	public void setContainOffline(boolean containOffline) {
		this.containOffline = containOffline;
	}
	public long getInTime() {
		return inTime;
	}
	public void setInTime(long inTime) {
		this.inTime = inTime;
	}
	@Override
	public int hashCode(){
		return super.hashCode();
	}	
	
	public boolean equals(GetAppRequest getAppRequest){
		return this==getAppRequest;
	}
}
