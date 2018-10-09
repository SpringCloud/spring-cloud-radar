package com.ppdai.infrastructure.radar.biz.dto.pub;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;

public class PubDeleteRequest  extends BaseRequest {
	private List<String> candInstanceIds;
	private List<Long> ids;
	public List<String> getCandInstanceIds() {
		return candInstanceIds;
	}
	public void setCandInstanceIds(List<String> candInstanceIds) {
		this.candInstanceIds = candInstanceIds;
	}
	public List<Long> getIds() {
		return ids;
	}
	public void setIds(List<Long> ids) {
		this.ids = ids;
	}	
}
