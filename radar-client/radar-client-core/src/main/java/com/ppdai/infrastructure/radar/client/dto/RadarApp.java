package com.ppdai.infrastructure.radar.client.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangyicong on 17-12-12.
 */
public class RadarApp {
	private String candAppId;
	private String appName;
	private long id;
	private long version;
	private int allowCross;
	private String domain;
	private Map<String, RadarCluster> clusters = new HashMap<>();

	public int getAllowCross() {
		return allowCross;
	}

	public String getCandAppId() {
		return candAppId;
	}

	public String getAppName() {
		return appName;
	}

	public long getId() {
		return id;
	}

	public Map<String, RadarCluster> getClusters() {
		return clusters;
	}

	public long getVersion() {
		return version;
	}

	public String getDomain() {
		return domain;
	}

	public static Builder getBuilder() {
		return new Builder();
	}

	public static class Builder {
		private RadarApp appRadar;

		public Builder() {
			appRadar = new RadarApp();
		}

		public Builder withCandAppId(String candAppId) {
			appRadar.candAppId = candAppId;
			return this;
		}

		public Builder withAppName(String appName) {
			appRadar.appName = appName;
			return this;
		}

		public Builder withId(long id) {
			appRadar.id = id;
			return this;
		}

		public Builder withClusters(Map<String, RadarCluster> clusters) {
			appRadar.clusters = clusters;
			return this;
		}

		public Builder withVersion(long version) {
			appRadar.version = version;
			return this;
		}

		public Builder withDomain(String domain) {
			appRadar.domain = domain;
			return this;
		}

		public Builder withAllowCross(int allowCross) {
			appRadar.allowCross = allowCross;
			return this;
		}

		public RadarApp build() {
			return appRadar;
		}
	}
}
