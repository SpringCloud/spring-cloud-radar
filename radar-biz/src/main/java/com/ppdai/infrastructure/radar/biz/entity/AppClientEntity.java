package com.ppdai.infrastructure.radar.biz.entity;

/// <summary>
///  
/// </summary>
public class AppClientEntity extends BaseEntity {
	/// <summary>
	/// 服务提供端的app1_id
	/// </summary>
	private String providerCandAppId;
	/// <summary>
	/// 当前消费端的appid
	/// </summary>
	private String consumerCandAppId;
	/// <summary>
	/// 子环境名称
	/// </summary>
	private String consumerClusterName;

	public String getProviderCandAppId() {
		return providerCandAppId;
	}

	public void setProviderCandAppId(String providerCandAppId) {
		this.providerCandAppId = providerCandAppId;
	}

	public String getConsumerCandAppId() {
		return consumerCandAppId;
	}

	public void setConsumerCandAppId(String consumerCandAppId) {
		this.consumerCandAppId = consumerCandAppId;
	}

	public String getConsumerClusterName() {
		return consumerClusterName;
	}

	public void setConsumerClusterName(String consumerClusterName) {
		this.consumerClusterName = consumerClusterName;
	}

}
