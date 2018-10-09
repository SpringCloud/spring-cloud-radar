package com.ppdai.infrastructure.radar.biz.dto.base;

import java.util.List;

//@Data
public class AppDto {
	private AppMetaDto appMeta;
	// 用户达到时间
	// private Date reachedTime;
	private List<AppClusterDto> clusters;

	public AppMetaDto getAppMeta() {
		return appMeta;
	}

	public void setAppMeta(AppMetaDto appMeta) {
		this.appMeta = appMeta;
	}

	public List<AppClusterDto> getClusters() {
		return clusters;
	}

	public void setClusters(List<AppClusterDto> clusters) {
		this.clusters = clusters;
	}
}
