package com.ppdai.infrastructure.radar.biz.entity;

/// <summary>
///  
/// </summary>
public class AppClusterEntity extends BaseEntity {
	/// <summary>
	///
	/// </summary>
	private long appId;
	/// <summary>
	///
	/// </summary>
	private String candAppId;
	/// <summary>
	///
	/// </summary>
	private String appName;
	/// <summary>
	///
	/// </summary>
	private String clusterName;
	/// <summary>
	///
	/// </summary>
	private String blackList;
	/// <summary>
	///
	/// </summary>
	private String whiteList;
	/// <summary>
	/// 0 表示不开启限流
	/// </summary>
	private int limitQps;
	/// <summary>
	/// 多种类型逗号隔开
	/// </summary>
	private String gatewayVisual;
	/// <summary>
	/// 是否开启自保护
	/// </summary>
	private int enableSelf;

	/// <summary>
	///
	/// </summary>
	public long getAppId() {
		return appId;
	}

	public void setAppId(long appId1) {
		appId = appId1;
	}

	/// <summary>
	///
	/// </summary>
	public String getCandAppId() {
		return candAppId;
	}

	public void setCandAppId(String candAppId1) {
		candAppId = candAppId1;
	}

	/// <summary>
	///
	/// </summary>
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName1) {
		appName = appName1;
	}

	/// <summary>
	///
	/// </summary>
	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName1) {
		clusterName = clusterName1;
	}

	/// <summary>
	///
	/// </summary>
	public String getBlackList() {
		return blackList;
	}

	public void setBlackList(String blackList1) {
		blackList = blackList1;
	}

	/// <summary>
	///
	/// </summary>
	public String getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(String whiteList1) {
		whiteList = whiteList1;
	}

	/// <summary>
	/// 0 表示不开启限流
	/// </summary>
	public int getLimitQps() {
		return limitQps;
	}

	public void setLimitQps(int limitQps1) {
		limitQps = limitQps1;
	}

	/// <summary>
	/// 多种类型逗号隔开
	/// </summary>
	public String getGatewayVisual() {
		return gatewayVisual;
	}

	public void setGatewayVisual(String gatewayVisual1) {
		gatewayVisual = gatewayVisual1;
	}

	/// <summary>
	/// 是否开启自保护
	/// </summary>
	public int getEnableSelf() {
		return enableSelf;
	}

	public void setEnableSelf(int enableSelf1) {
		enableSelf = enableSelf1;
	}
}
