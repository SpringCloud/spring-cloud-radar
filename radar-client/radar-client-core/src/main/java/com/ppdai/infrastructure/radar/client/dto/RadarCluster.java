package com.ppdai.infrastructure.radar.client.dto;

import java.util.Collections;
import java.util.List;

/**
 * Created by zhangyicong on 17-12-12.
 */
public class RadarCluster {

	private String clusterName;
	private int deleteFlag;
	private List<RadarInstance> instances;

	private RadarCluster() {

	}

	public int getDeleteFlag() {
		return deleteFlag;
	}

	public String getClusterName() {
		return clusterName;
	}

	public List<RadarInstance> getInstances() {
		return instances;
	}

	public static Builder getBuilder() {
		return new Builder();
	}

	public static class Builder {
		private RadarCluster clusterRadar;

		public Builder() {
			clusterRadar = new RadarCluster();
		}

		public Builder withClusterName(String clusterName) {
			clusterRadar.clusterName = clusterName;
			return this;
		}

		public Builder withDeleteFlag(int deleteFlag) {
			clusterRadar.deleteFlag = deleteFlag;
			return this;
		}

		public Builder withInstances(List<RadarInstance> instances) {
			clusterRadar.instances = Collections.unmodifiableList(instances);
			return this;
		}

		public RadarCluster build() {
			return clusterRadar;
		}
	}
}
