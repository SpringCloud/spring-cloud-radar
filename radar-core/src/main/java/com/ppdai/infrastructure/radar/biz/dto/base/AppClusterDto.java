package com.ppdai.infrastructure.radar.biz.dto.base;

import java.util.List;

//@Data
public class AppClusterDto{
	private AppClusterMetaDto appClusterMeta;
	
	private List<InstanceDto> instances; 
	public AppClusterMetaDto getAppClusterMeta() {
		return appClusterMeta;
	}
	public void setAppClusterMeta(AppClusterMetaDto appClusterMetaDto) {
		this.appClusterMeta = appClusterMetaDto;
	}
	public List<InstanceDto> getInstances() {
		return instances;
	}
	public void setInstances(List<InstanceDto> instances) {
		this.instances = instances;
	}
	
	
}
