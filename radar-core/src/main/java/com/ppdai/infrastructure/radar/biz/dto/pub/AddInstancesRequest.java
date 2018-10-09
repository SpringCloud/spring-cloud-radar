package com.ppdai.infrastructure.radar.biz.dto.pub;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;

public class AddInstancesRequest extends BaseRequest {
	private String candAppId;	
	private String clusterName;
	private String appName;
	private List<AddInstanceDto> candInstances;

	public String getCandAppId() {
		return candAppId;
	}
	public void setCandAppId(String candAppId) {
		this.candAppId = candAppId;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public List<AddInstanceDto> getCandInstances() {
		return candInstances;
	}
	public void setCandInstances(List<AddInstanceDto> candInstances) {
		this.candInstances = candInstances;
	}
}
