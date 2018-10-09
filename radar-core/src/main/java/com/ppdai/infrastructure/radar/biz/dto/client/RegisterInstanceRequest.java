package com.ppdai.infrastructure.radar.biz.dto.client;

import java.util.Map;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;

//@Data
public class RegisterInstanceRequest extends BaseRequest {
	private String clusterName;
	private String candAppId;
	private String appName;
	//private String ip;
	private int port;
	private String candInstanceId;
	
	// 多个以逗号隔开
	private String servName;
	private Map<String, String> tag;
	private long inTime;

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getCandAppId() {
		return candAppId;
	}

	public void setCandAppId(String candAppId) {
		this.candAppId = candAppId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

//	public String getIp() {
//		return ip;
//	}
//
//	public void setIp(String ip) {
//		this.ip = ip;
//	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getCandInstanceId() {
		return candInstanceId;
	}

	public void setCandInstanceId(String candInstanceId) {
		this.candInstanceId = candInstanceId;
	}
	
	public String getServName() {
		return servName;
	}

	public void setServName(String servName) {
		this.servName = servName;
	}

	public Map<String, String> getTag() {
		return tag;
	}

	public void setTag(Map<String, String> tag) {
		this.tag = tag;
	}

	public long getInTime() {
		return inTime;
	}

	public void setInTime(long inTime) {
		this.inTime = inTime;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public boolean equals(RegisterInstanceRequest registerInstanceRequest) {
		return this == registerInstanceRequest;
	}
}
