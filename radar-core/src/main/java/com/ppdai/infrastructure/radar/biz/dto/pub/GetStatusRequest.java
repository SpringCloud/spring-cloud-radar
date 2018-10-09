package com.ppdai.infrastructure.radar.biz.dto.pub;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;


public class GetStatusRequest  extends BaseRequest {
	private List<String> candInstanceIds;

	public List<String> getCandInstanceIds() {
		return candInstanceIds;
	}

	public void setCandInstanceIds(List<String> candInstanceIds) {
		this.candInstanceIds = candInstanceIds;
	}
}
