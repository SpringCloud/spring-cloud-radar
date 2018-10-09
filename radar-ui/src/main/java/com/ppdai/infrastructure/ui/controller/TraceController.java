package com.ppdai.infrastructure.ui.controller;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.ui.service.UiTraceService;
import com.ppdai.infrastructure.ui.service.impl.DefaultRoleService;
import com.ppdai.infrastructure.ui.service.common.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TraceController
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Controller
@RequestMapping("/app")
public class TraceController {
	@Autowired
	private DefaultRoleService defaultRoleService;
	@Autowired
	private UiTraceService uiTraceService;
	@Autowired
	private SoaConfig soaConfig;
	/**
	 * 判断trace接口是否可以展示，(只有超级管理员才可以查看trace)
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("/hide")
	@ResponseBody
	public int hide(HttpServletRequest request) {
		return defaultRoleService.getRole(null);
	}

	@GetMapping("/stat")
	@ResponseBody
	public String stat() {
		return uiTraceService.stat();
	}

	@GetMapping("/cache1")
	@ResponseBody
	public Object getCache() {
		return uiTraceService.cache1();
	}

	@GetMapping("/trace")
	@ResponseBody
	public Object getTrace() {
		return uiTraceService.trace();
	}

	/**
	 * 显示内部配置项的页面
	 * @param request
	 * @param model
	 * @return
	 */
	@GetMapping("/config")
	public String getConfig(HttpServletRequest request,Model model){
		model.addAllAttributes(setModelMap(request));
		return "app/config";
	}

	/**
	 * 返回 调用radar-rest自检接口的 页面
	 * @return
	 */
	@GetMapping("/test")
	public String test(Model model) {
		model.addAttribute("radarRestUrl",soaConfig.getRadarUrl()+ Constant.getTestUrl());
		model.addAttribute("testSynAtlasUrl",soaConfig.getRdportalUrl()+Constant.getTestSynAtlasUrl());
		return "app/test";
	}


	@GetMapping("/test/getData")
	@ResponseBody
	public String getTestData() {
		return uiTraceService.test();
	}


	@RequestMapping("/cachelist")
	public String list() {
		return "app/cachelist";
	}

	@GetMapping("/cachelist/getData")
	@ResponseBody
	public Object getData(@RequestParam(value = "flag", required = false) Integer flag,
						  @RequestParam(value = "appId", required = false) String appId) {
		return uiTraceService.cache(flag, appId);
	}

	/**
	 * 数据库连接数
	 * @return
	 */
	@GetMapping("/connections")
	@ResponseBody
	public String connections() {
		return uiTraceService.connections();
	}

	private Map setModelMap(HttpServletRequest request){
		Map modelMap=new HashMap();
		modelMap.put("atlasUrl",soaConfig.getAtlasUrl());
		modelMap.put("pauthUrl",soaConfig.getPauthApiUrl());
		modelMap.put("radarUrl",soaConfig.getRadarUrl());
		return modelMap;
	}

	@GetMapping("/lock")
	public String lock(Model model) {
		return "app/lock";
	}

	@GetMapping("/lock/data")
	@ResponseBody
	public UiResponse lockData(HttpServletRequest request,Model model) {
		int pageNum = Integer.parseInt(request.getParameter("page"));
		int pageSize = Integer.parseInt(request.getParameter("limit"));
		Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
		parameterMap.put("pageIndex", (pageNum - 1) * pageSize);
		parameterMap.put("pageSize", pageSize);

		return uiTraceService.getLockData(parameterMap);
	}


}
