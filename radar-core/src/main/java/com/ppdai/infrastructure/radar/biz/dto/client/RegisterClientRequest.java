package com.ppdai.infrastructure.radar.biz.dto.client;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;

public class RegisterClientRequest extends BaseRequest {
	private List<String> providerCandAppIds;
	private String consumerCandAppId;
	private String consumerClusterName;

	public List<String> getProviderCandAppIds() {
		return providerCandAppIds;
	}

	public void setProviderCandAppIds(List<String> providerCandAppIds) {
		this.providerCandAppIds = providerCandAppIds;
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
