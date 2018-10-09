package com.ppdai.infrastructure.radar.client.config;

import java.util.HashMap;
import java.util.Map;

public class RadarClientConfig {
	// 注册中心地址
	private String registryUrl;
	// 连接池获取时间
	private int connectionTimeout = 35;
	// 接口访问时间
	private int readTimeout = 35;
	// 注意此端口可以不传
	private String candInstanceId;
	private int port;
	private String candAppId;
	private String appName;
	private String clusterName;
	private String host;
	private boolean registerSelf = true;
	private Map<String, String> tags = new HashMap<>();

	public RadarClientConfig(String url) {
		this.registryUrl = url;
	}

	public RadarClientConfig() {

	}

	public String getRegistryUrl() {
		return registryUrl;
	}

	public void setRegistryUrl(String registryUrl) {
		this.registryUrl = registryUrl;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public String getCandInstanceId() {
		return candInstanceId;
	}

	public void setCandInstanceId(String candInstanceId) {
		this.candInstanceId = candInstanceId;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
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

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isRegisterSelf() {
		return registerSelf;
	}

	public void setRegisterSelf(boolean registerSelf) {
		this.registerSelf = registerSelf;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

}
