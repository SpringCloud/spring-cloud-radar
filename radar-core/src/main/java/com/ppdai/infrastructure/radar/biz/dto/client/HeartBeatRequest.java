package com.ppdai.infrastructure.radar.biz.dto.client;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;

public class HeartBeatRequest extends BaseRequest {
	private long instanceId;
	private List<Long> instanceIds;

	public List<Long> getInstanceIds() {
		return instanceIds;
	}

	public void setInstanceIds(List<Long> instanceIds) {
		this.instanceIds = instanceIds;
	}

	public long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(long instanceId) {
		this.instanceId = instanceId;
	}
}
