package com.ppdai.infrastructure.radar.biz.dto.client;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;


public class DeRegisterInstanceRequest extends BaseRequest{
	private long instanceId;

	public long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(long instanceId) {
		this.instanceId = instanceId;
	}
}
