package com.ppdai.infrastructure.ui.service;

import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;
import com.ppdai.infrastructure.ui.AbstractTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestUIAppService extends AbstractTest{
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private UiAppService uiAppService;
    @Test
    public void testFindByInstanceId(){
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("w545878765");
        AppEntity appEntity=(AppEntity) uiAppService.findByInstanceId(Long.toString(instanceEntity.getId())).getData().get(0);
        Assert.assertEquals("wApp", appEntity.getAppName());
    }

    @Test
    public void testFindByAppName(){
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("w545878765");
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        parameterMap.put("appName", "wApp");
        AppEntity appEntity=(AppEntity) uiAppService.findByAppName(parameterMap).getData().get(0);
        Assert.assertEquals("wApp", appEntity.getAppName());
    }

    @Test
    public void testUpdate(){
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("w545878765");
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        parameterMap.put("appName", "wApp");
        AppEntity appEntity=(AppEntity) uiAppService.findByAppName(parameterMap).getData().get(0);
        Assert.assertEquals("wApp", appEntity.getAppName());
        int result=uiAppService.update(appEntity);
        Assert.assertEquals(1, result);
    }
}
