package com.ppdai.infrastructure.ui.controller;

import com.ppdai.infrastructure.radar.biz.bo.OrganizationBo;
import com.ppdai.infrastructure.radar.biz.service.UserService;
import com.ppdai.infrastructure.ui.dto.request.AppUpdateRequest;
import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.radar.biz.service.AppService;
import com.ppdai.infrastructure.ui.dto.request.AppSearchRequest;
import com.ppdai.infrastructure.ui.service.UiAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AppController
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Controller
@RequestMapping("/app")
public class AppController {
    @Autowired
    private UiAppService uiAppService;
    @Autowired
    private AppService appService;
    @Autowired
    private UserService userService;

    /**
     * "详情展示"中app的跳转路由
     *
     * @param model
     * @param instanceId
     * @return
     */
    @RequestMapping("/detail")
    public String detail(Model model, String instanceId) {
        model.addAttribute("instanceId", instanceId);
        return "app/detail";
    }

    /**
     * "详情展示"中app的数据加载
     *
     * @param request
     * @param instanceId
     * @return
     */
    @RequestMapping("/detail/data")
    @ResponseBody
    public UiResponse detailData(HttpServletRequest request, @RequestParam("instanceId") String instanceId) {
        return uiAppService.findByInstanceId(instanceId);
    }

    /**
     * 左侧"导航栏"中app的跳转路由
     *
     * @return
     */
    @RequestMapping("/list")
    public String list() {
        return "app/list";
    }

    /**
     * 左侧"导航栏"中app的数据加载
     *
     * @return
     */
    @RequestMapping("/list/data")
    @ResponseBody
    public UiResponse listData(AppSearchRequest appSearchRequest) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        int pageNum = Integer.parseInt(appSearchRequest.getPage());
        int pageSize = Integer.parseInt(appSearchRequest.getLimit());
        parameterMap.put("appName", appSearchRequest.getAppName());
        parameterMap.put("pageIndex", (pageNum - 1) * pageSize);
        parameterMap.put("pageSize", pageSize);
        parameterMap.put("appId",appSearchRequest.getAppId());
        parameterMap.put("departmentName",appSearchRequest.getDepartmentName());
        parameterMap.put("ownerName",appSearchRequest.getOwnerName());
        return uiAppService.findByAppName(parameterMap);
    }

    /**
     * 操作指南
     *
     * @return
     */
    @RequestMapping("/manual")
    public String manual() {
        return "app/manual";

    }

    /**
     * 更新版本
     *
     * @param appId
     * @return
     */
    @RequestMapping("/update/version")
    @ResponseBody
    public String updateVersion(@RequestParam("appId") long appId) {      
        List<Long> appIds = new ArrayList<>();
        appIds.add(appId);
        try {
            appService.updateVersionByIds(appIds);
            return "true";
        } catch (Exception e) {
            return "false";
        }
    }


    @RequestMapping(value = "/edit",method = RequestMethod.POST)
    @ResponseBody
    public UiResponse edit(AppUpdateRequest appUpdateRequest) {
        UiResponse uiResponse = uiAppService.update(appUpdateRequest);
        return uiResponse;
    }



}
