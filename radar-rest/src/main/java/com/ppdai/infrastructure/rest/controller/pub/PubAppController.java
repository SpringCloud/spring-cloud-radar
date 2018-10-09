package com.ppdai.infrastructure.rest.controller.pub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.exception.SoaException;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstancesRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstancesResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustSupperStatusRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustSupperStatusResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.PubDeleteRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.PubDeleteResponse;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;

@RestController
@RequestMapping(RadarConstanst.PUBPRE)
public class PubAppController {
	private static final Logger log = LoggerFactory.getLogger(PubAppController.class);
	@Autowired
	private InstanceService instanceService;
	@Autowired
	private SoaConfig soaConfig;

	@PostMapping("/addInstance")
	public AddInstancesResponse addInstances(@RequestBody AddInstancesRequest request) {
		try {
			return instanceService.addInstance(request);
		} catch (DataIntegrityViolationException e) {
			AddInstancesResponse response = new AddInstancesResponse();
			log.error("注册服务失败addInstancesfailcandInstanceId 不唯一", e);
			response.setSuc(false);
			response.setMsg("candInstanceId 不唯一");
			throw new SoaException(response, e);
		} catch (Exception e) {
			AddInstancesResponse response = new AddInstancesResponse();
			log.error("注册服务失败addInstancesfail", e);
			response.setSuc(false);
			response.setMsg("注册服务失败");
			throw new SoaException(response, e);
		}
	}

	@PostMapping("/adjust")
	public AdjustResponse adjust(@RequestBody AdjustRequest request) {
		try {
			AdjustResponse response = instanceService.adjust(request);
			String reqJson=JsonUtil.toJsonNull(request);
			String repJson=JsonUtil.toJsonNull(response);
			if (!CollectionUtils.isEmpty(response.getInstanceIds())) {
				response.getInstanceIds().forEach(t1 -> {
					log.info(soaConfig.getLogPrefix() + "_adjust 状态调节成功,参数为：{}，返回{}，结束", t1, reqJson,
							repJson);
				});
			}
			return response;
		} catch (Exception e) {
			AdjustResponse response = new AdjustResponse();
			log.error("调节流量失败adjustfail", e);
			response.setSuc(false);
			response.setMsg("调节流量失败");
			throw new SoaException(response, e);
		}
	}
	@PostMapping("/adjustSupperStatus")
	public AdjustSupperStatusResponse adjustSupperStatus(@RequestBody AdjustSupperStatusRequest request) {
		try {
			AdjustSupperStatusResponse response = instanceService.adjustSupperStatus(request);
			if (!CollectionUtils.isEmpty(response.getInstanceIds())) {
				response.getInstanceIds().forEach(t1 -> {
					log.info(soaConfig.getLogPrefix() + "_adjustSupperStatus 状态调节成功,参数为：{}，返回{}，结束", t1, JsonUtil.toJsonNull(request),
							JsonUtil.toJsonNull(response));
				});
			}
			return response;
		} catch (Exception e) {
			AdjustSupperStatusResponse response = new AdjustSupperStatusResponse();
			log.error("调节流量失败AdjustSupper", e);
			response.setSuc(false);
			response.setMsg("调节流量失败");
			throw new SoaException(response, e);
		}
	}
	@PostMapping("/getStatus")
	public GetStatusResponse getStatus(@RequestBody GetStatusRequest request) {
		try {
			return instanceService.getStatus(request);
		} catch (Exception e) {
			GetStatusResponse response = new GetStatusResponse();
			log.error("获取状态失败getStatusfail", e);
			response.setSuc(false);
			response.setMsg("获取状态失败");
			throw new SoaException(response, e);
		}
	}

	@PostMapping("/pubDel")
	public PubDeleteResponse pubDelete(@RequestBody PubDeleteRequest request) {
		try {
			PubDeleteResponse response = instanceService.pubDelete(request);
			if (!CollectionUtils.isEmpty(response.getInstanceIds())) {
				response.getInstanceIds().forEach(t1 -> {
					log.info(soaConfig.getLogPrefix() + "删除成功,参数为：{}，返回{}，结束", t1, JsonUtil.toJsonNull(request),
							JsonUtil.toJsonNull(response));
				});
			}
			return response;
		} catch (Exception e) {
			PubDeleteResponse response = new PubDeleteResponse();
			log.error("删除实例失败pubDeletefail", e);
			response.setSuc(false);
			response.setMsg("删除实例失败");
			throw new SoaException(response, e);
		}
	}

}
