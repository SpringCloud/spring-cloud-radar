package com.ppdai.infrastructure.radar.biz.dto.pub;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;

public class UpdateVersionRequest extends BaseRequest {
	private List<Long> appIds;

	public List<Long> getAppIds() {
		return appIds;
	}

	public void setAppIds(List<Long> appIds) {
		this.appIds = appIds;
	}
}
