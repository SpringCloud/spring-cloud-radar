package com.ppdai.infrastructure.radar.biz.dto.client;

import com.ppdai.infrastructure.radar.biz.dto.BaseResponse;

public class RegisterInstanceResponse extends BaseResponse {
	private long instanceId;
	private String candInstanceId;
	// 心跳时间间隔
	private int heartbeatTime;

	public int getHeartbeatTime() {
		return heartbeatTime;
	}

	public void setHeartbeatTime(int heartbeatTime) {
		this.heartbeatTime = heartbeatTime;
	}

	public String getCandInstanceId() {
		return candInstanceId;
	}

	public void setCandInstanceId(String candInstanceId) {
		this.candInstanceId = candInstanceId;
	}

	public long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(long instanceId) {
		this.instanceId = instanceId;
	}
}
