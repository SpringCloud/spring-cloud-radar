package com.ppdai.infrastructure.radar.biz.dto.pub;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;

public class AdjustSupperStatusRequest  extends BaseRequest {
	private List<String> candInstanceIds;
	private List<Long> ids;
	private int status;//0，不开启，1,强制true，-1强制false
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
}
