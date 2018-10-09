package com.ppdai.infrastructure.radar.client.event;

import com.ppdai.infrastructure.radar.client.dto.RadarInstance;

public interface PreRegisterListener {
	void onPreRegister(RadarInstance.Builder builder);
}
