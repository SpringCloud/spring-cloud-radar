package com.ppdai.infrastructure.radar.biz.dto.pub;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseResponse;


public class GetStatusResponse extends BaseResponse {
	private List<GetStatusInstanceDto> pubInstances;

	public List<GetStatusInstanceDto> getPubInstances() {
		return pubInstances;
	}

	public void setPubInstances(List<GetStatusInstanceDto> pubInstances) {
		this.pubInstances = pubInstances;
	}
}
