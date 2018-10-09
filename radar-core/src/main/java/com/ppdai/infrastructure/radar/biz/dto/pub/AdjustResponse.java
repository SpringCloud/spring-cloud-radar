package com.ppdai.infrastructure.radar.biz.dto.pub;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseResponse;

public class AdjustResponse extends BaseResponse {
	private List<Long> instanceIds;
	private List<String> noCandInstanceIds;
	private List<Long> noIds;
	
	public List<Long> getInstanceIds() {
		return instanceIds;
	}
	public void setInstanceIds(List<Long> instanceIds) {
		this.instanceIds = instanceIds;
	}
	public List<String> getNoCandInstanceIds() {
		return noCandInstanceIds;
	}
	public void setNoCandInstanceIds(List<String> noCandInstanceIds) {
		this.noCandInstanceIds = noCandInstanceIds;
	}
	public List<Long> getNoIds() {
		return noIds;
	}
	public void setNoIds(List<Long> noIds) {
		this.noIds = noIds;
	}
}
