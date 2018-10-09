package com.ppdai.infrastructure.radar.biz.dto.client;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseResponse;
import com.ppdai.infrastructure.radar.biz.dto.base.AppMetaDto;


public class GetAppMetaResponse extends BaseResponse {
	List<AppMetaDto> appMetas;

	public List<AppMetaDto> getAppMetas() {
		return appMetas;
	}

	public void setAppMetas(List<AppMetaDto> appMetas) {
		this.appMetas = appMetas;
	}	
}
