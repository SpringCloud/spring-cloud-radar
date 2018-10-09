package com.ppdai.infrastructure.ui.service;

import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.entity.AuditLogEntity;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;
import com.ppdai.infrastructure.ui.AbstractTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestUiAuditLogService extends AbstractTest {
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private UiAuditLogService uiAuditLogService;


    @Test
    public void testLog() {
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("w545878765");
        HttpServletRequest request = new MockHttpServletRequest();
        uiAuditLogService.insertLog(request,"instance", instanceEntity.getId(), "修改了");
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        parameterMap.put("instanceId", Long.toString(instanceEntity.getId()));
        List auditLogList=uiAuditLogService.findByInstanceId(parameterMap).getData();
        Assert.assertNotNull(auditLogList);

    }
}
