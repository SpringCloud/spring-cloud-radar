package com.ppdai.infrastructure.ui.controller;

import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.ui.service.UiAuditLogService;
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
 * AuditLogController
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Controller
@RequestMapping("/app/log")
public class AuditLogController {
    @Autowired
    private UiAuditLogService uiAuditLogService;

    /**
     * "详情展示"中 操作日志的跳转路由
     *
     * @param model
     * @param instanceId
     * @return
     */
    @RequestMapping("/detail")
    public String detail(Model model, String instanceId) {
        model.addAttribute("instanceId", instanceId);
        return "app/log/detail";
    }

    /**
     * "详情展示"中 操作日志的数据加载
     *
     * @param request
     * @param instanceId
     * @return
     */
    @RequestMapping("/detail/data")
    @ResponseBody
    public UiResponse detailData(HttpServletRequest request, @RequestParam("instanceId") String instanceId) {
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        int pageNum = Integer.parseInt(request.getParameter("page"));
        int pageSize = Integer.parseInt(request.getParameter("limit"));
        parameterMap.put("instanceId", instanceId);
        parameterMap.put("pageIndex", (pageNum - 1) * pageSize);
        parameterMap.put("pageSize", pageSize);

        return uiAuditLogService.findByInstanceId(parameterMap);
    }
}
