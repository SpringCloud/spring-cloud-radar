package com.ppdai.infrastructure.radar.biz.dto.base;

//@Data
public class AppClusterMetaDto {
	private long id;
	private String clusterName;
	private transient String blackList;
	private transient String whiteList;
	private transient String gatewayVisual;
	private int limitQps;
	private transient int enableSelf;
	/*
	 * 标识是否删除 1,表示删除，0表示未删除
	 */
	private int deleteFlag;
	
	public int getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public int getEnableSelf() {
		return enableSelf;
	}

	public void setEnableSelf(int enableSelf) {
		this.enableSelf = enableSelf;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getBlackList() {
		return blackList;
	}

	public void setBlackList(String blackList) {
		this.blackList = blackList;
	}

	public String getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(String whiteList) {
		this.whiteList = whiteList;
	}

	public String getGatewayVisual() {
		return gatewayVisual;
	}

	public void setGatewayVisual(String gatewayVisual) {
		this.gatewayVisual = gatewayVisual;
	}

	public int getLimitQps() {
		return limitQps;
	}

	public void setLimitQps(int limitQps) {
		this.limitQps = limitQps;
	}
}
