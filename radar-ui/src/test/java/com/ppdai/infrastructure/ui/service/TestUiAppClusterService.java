package com.ppdai.infrastructure.ui.service;

import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.entity.AppClusterEntity;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;
import com.ppdai.infrastructure.ui.AbstractTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestUiAppClusterService extends AbstractTest {
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private UiAppClusterService uiAppClusterService;

    @Test
    public void testFindByInstanceId() {
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("w545878765");
        Assert.assertEquals(testInstanceEntity.getAppClusterName(), instanceEntity.getAppClusterName());
        long instanceId=instanceEntity.getId();
        AppClusterEntity appClusterEntity=(AppClusterEntity) uiAppClusterService.findByInstanceId(Long.toString(instanceId)).getData().get(0);
        Assert.assertEquals("wCluster", appClusterEntity.getClusterName());
    }

    @Test
    public void testFindByClusterName(){
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        parameterMap.put("clusterName", "wCluster");
        AppClusterEntity appClusterEntity=(AppClusterEntity) uiAppClusterService.findByClusterName(parameterMap).getData().get(0);
        Assert.assertEquals("wCluster", appClusterEntity.getClusterName());
    }


}
