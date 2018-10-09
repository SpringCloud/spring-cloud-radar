package com.ppdai.infrastructure.radar.biz.dto.pub;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseResponse;


public class AdjustSupperStatusResponse extends BaseResponse {
	private List<Long> instanceIds;

	public List<Long> getInstanceIds() {
		return instanceIds;
	}
	public void setInstanceIds(List<Long> instanceIds) {
		this.instanceIds = instanceIds;
	}
}
