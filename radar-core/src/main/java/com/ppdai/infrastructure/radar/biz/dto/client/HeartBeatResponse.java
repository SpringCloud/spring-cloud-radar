package com.ppdai.infrastructure.radar.biz.dto.client;

import com.ppdai.infrastructure.radar.biz.dto.BaseResponse;

public class HeartBeatResponse extends BaseResponse {
	// 心跳时间间隔
	private int heartbeatTime;

	public int getHeartbeatTime() {
		return heartbeatTime;
	}

	public void setHeartbeatTime(int heartbeatTime) {
		this.heartbeatTime = heartbeatTime;
	}
}
