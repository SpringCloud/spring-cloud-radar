package com.ppdai.infrastructure.rest.controller.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.Counter;
import com.ppdai.infrastructure.radar.biz.common.exception.SoaException;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppMetaRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppMetaResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.UpdateVersionRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.UpdateVersionResponse;
import com.ppdai.infrastructure.radar.biz.service.AppService;

@RestController
@RequestMapping(RadarConstanst.APPPRE)
public class ClientAppController {
	private static final Logger log = LoggerFactory.getLogger(ClientAppNotifyController.class);
	@Autowired
	private AppService appService;
	private Counter counter = null;

	//直接获取app信息，不等待
	@PostMapping("/getApp")
	public GetAppResponse getApp(@RequestBody GetAppRequest request) {
		try {
			return appService.getApp(request);
		} catch (Exception e) {
			GetAppResponse response = new GetAppResponse();
			log.error("getAppFail失败", e);
			response.setSuc(false);
			response.setMsg("获取应用失败");
			throw new SoaException(response, e);
		}
	}

	@PostMapping("/getAppMeta")
	public GetAppMetaResponse getAppMeta(@RequestBody GetAppMetaRequest request) {
		try {
			return appService.getAppMeta(request);
		} catch (Exception e) {
			GetAppMetaResponse response = new GetAppMetaResponse();
			log.error("getAppMeta失败", e);
			response.setSuc(false);
			response.setMsg("获取应用失败");
			throw new SoaException(response, e);
		}
	}
	@PostMapping("/updateVersion")
	public UpdateVersionResponse updateVersion(@RequestBody UpdateVersionRequest request) {
		try {
			UpdateVersionResponse response = new UpdateVersionResponse();
			if (request == null || CollectionUtils.isEmpty(request.getAppIds())) {
				response.setSuc(false);
				response.setMsg("参数不能为空!");
				return response;
			}
			response.setSuc(true);
			appService.updateVersionByIds(request.getAppIds());
			for (long appid : request.getAppIds()) {
				log.info("app_{}_update_version in updateVersion method更新版本,结束", appid);
			}
			return response;
		} catch (Exception e) {
			UpdateVersionResponse response = new UpdateVersionResponse();
			log.error("updateVersion失败", e);
			response.setSuc(false);
			response.setMsg("updateVersion失败");
			throw new SoaException(response, e);
		}
	}

	public long getCount() {
		if (counter != null) {
			return counter.getCount();
		}
		return 0;
	}
}
