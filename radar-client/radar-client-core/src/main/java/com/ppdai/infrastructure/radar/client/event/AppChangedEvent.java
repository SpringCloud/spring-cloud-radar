package com.ppdai.infrastructure.radar.client.event;

import java.util.Map;

import com.ppdai.infrastructure.radar.client.dto.RadarApp;

public class AppChangedEvent {
	private Map<String, RadarApp> changedEvent;

	public Map<String, RadarApp> getChangedEvent() {
		return changedEvent;
	}

	public void setChangedEvent(Map<String, RadarApp> changedEvent) {
		this.changedEvent = changedEvent;
	}
}
