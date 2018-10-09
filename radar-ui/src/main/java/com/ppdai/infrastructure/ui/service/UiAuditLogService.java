package com.ppdai.infrastructure.ui.service;

import com.ppdai.infrastructure.radar.biz.dal.UiAuditLogRepository;
import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.radar.biz.entity.AuditLogEntity;
import com.ppdai.infrastructure.radar.biz.service.UserService;
import com.ppdai.infrastructure.ui.service.common.UiResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * UiAuditLogService
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Service
public class UiAuditLogService{
    @Autowired
    private UiAuditLogRepository uiAuditLogRepository;
    @Autowired
    private UserService userService;
    /**
     * 插入操作记录
     *
     * @param request
     * @param tableName
     * @param id
     * @param content
     */
    public void insertLog(HttpServletRequest request, String tableName, long id, String content) {
        String userId = "";
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        userId= userService.getCurrentUser().getUserId();

        parameterMap.put("tbName", tableName);
        parameterMap.put("refId", id);
        parameterMap.put("content", content);
        parameterMap.put("insertBy", userId);

        uiAuditLogRepository.insertLog(parameterMap);
    }

    /**
     * 查找AuditLog
     * @param parameterMap
     * @return
     */
    public UiResponse findByInstanceId(Map parameterMap) {
        UiResponse uiResponse = new UiResponse();
        Long count;
        List<AuditLogEntity> auditLogList;

        count = uiAuditLogRepository.auditLogCount(parameterMap);
        auditLogList = uiAuditLogRepository.findByInstanceId(parameterMap);
        return UiResponseHelper.setUiResponse(uiResponse, count, auditLogList);
    }
}
