package com.ppdai.infrastructure.ui.controller;

import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.ui.service.UiAppClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AppClusterController
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Controller
@RequestMapping("/app/cluster")
public class AppClusterController {
    @Autowired
    private UiAppClusterService uiAppClusterService;

    /**
     * "详情展示"中cluster的跳转路由
     *instanceId为实例的自增id
     * @param model
     * @param instanceId
     * @return
     */
    @RequestMapping("/detail")
    public String detail(Model model, String instanceId) {
        model.addAttribute("instanceId", instanceId);
        return "app/cluster/detail";
    }

    /**
     * "详情展示"中cluster的数据加载
     *instanceId为实例的自增id
     * @param request
     * @param instanceId
     * @return
     */
    @RequestMapping("/detail/data")
    @ResponseBody
    public UiResponse detailData(HttpServletRequest request, @RequestParam("instanceId") String instanceId) {
        return uiAppClusterService.findByInstanceId(instanceId);
    }

    /**
     * 左侧"导航栏"中cluster的路由跳转
     *
     * @return
     */
    @RequestMapping("/list")
    public String list() {
        return "app/cluster/list";
    }

    /**
     * 左侧"导航栏"中cluster的数据加载
     *
     * @param request
     * @param clusterName
     * @param appId
     * @return
     */
    @RequestMapping("/list/data")
    @ResponseBody
    public UiResponse listData(HttpServletRequest request, String clusterName, String appId) {
        int pageNum = Integer.parseInt(request.getParameter("page"));
        int pageSize = Integer.parseInt(request.getParameter("limit"));
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        parameterMap.put("clusterName", clusterName);
        parameterMap.put("appId", appId);
        parameterMap.put("pageIndex", (pageNum - 1) * pageSize);
        parameterMap.put("pageSize", pageSize);
        return uiAppClusterService.findByClusterName(parameterMap);
    }
}
