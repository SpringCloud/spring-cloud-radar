package com.ppdai.infrastructure.ui.service;

import com.google.common.collect.Lists;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.dto.ui.CombinedInstanceDto;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;
import com.ppdai.infrastructure.ui.AbstractTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestUiInstanceService extends AbstractTest {
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private UiInstanceService uiInstanceService;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testFindInstance() throws Exception {
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        HttpServletRequest request = new MockHttpServletRequest();
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        parameterMap = setParameterMap(parameterMap);
        CombinedInstanceDto combinedInstanceDto = (CombinedInstanceDto) uiInstanceService.findInstance(request, parameterMap).getData().get(0);
        Assert.assertEquals(testInstanceEntity.getAppClusterName(), combinedInstanceDto.getAppClusterName());
        Assert.assertEquals(testInstanceEntity.getCandAppId(), combinedInstanceDto.getCandAppId());
        deRegister(combinedInstanceDto.getId());
    }

    @Test
    public void testFindExpiredInstance() throws Exception{
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        Thread.sleep(2 * 1000);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        HttpServletRequest request = new MockHttpServletRequest();
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        parameterMap = setParameterMap(parameterMap);
        long lastNoActiveTime = System.currentTimeMillis() - 60 * 1000;
        parameterMap.put("lastNoActiveTime", formatter.format(new Date(lastNoActiveTime)));
        parameterMap.put("statusSelect", 10);
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("w545878765");
        Assert.assertEquals(testInstanceEntity.getAppClusterName(), instanceEntity.getAppClusterName());
        Assert.assertEquals(testInstanceEntity.getCandAppId(), instanceEntity.getCandAppId());
        //更新实例心跳状态
        instanceService.updateHeartStatus(Lists.newArrayList(instanceEntity), false, 1);
        Assert.assertEquals(0, instanceService.findByCandInstanceId("w545878765").getHeartStatus());
        CombinedInstanceDto combinedInstanceDto = (CombinedInstanceDto) uiInstanceService.findExpiredInstance(request, parameterMap).getData().get(0);
        Assert.assertEquals(testInstanceEntity.getAppClusterName(), combinedInstanceDto.getAppClusterName());
        Assert.assertEquals(testInstanceEntity.getCandAppId(), combinedInstanceDto.getCandAppId());
        deRegister(combinedInstanceDto.getId());
    }

    @Test
    public void testFindByInstanceId() throws Exception{
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        Thread.sleep(2 * 1000);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("w545878765");
        Assert.assertEquals(testInstanceEntity.getAppClusterName(), instanceEntity.getAppClusterName());
        Assert.assertEquals(testInstanceEntity.getCandAppId(), instanceEntity.getCandAppId());
        HttpServletRequest request = new MockHttpServletRequest();
        CombinedInstanceDto combinedInstanceDto = (CombinedInstanceDto) uiInstanceService.findByInstanceId(request, Long.toString(instanceEntity.getId())).getData().get(0);
        Assert.assertEquals(testInstanceEntity.getAppClusterName(), combinedInstanceDto.getAppClusterName());
        Assert.assertEquals(testInstanceEntity.getCandAppId(), combinedInstanceDto.getCandAppId());
        deRegister(combinedInstanceDto.getId());
    }

    @Test
    public void testSetFinalStatus() throws Exception{
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        Thread.sleep(2 * 1000);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        HttpServletRequest request = new MockHttpServletRequest();
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        parameterMap = setParameterMap(parameterMap);
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("w545878765");
        Assert.assertEquals(testInstanceEntity.getAppClusterName(), instanceEntity.getAppClusterName());
        Assert.assertEquals(testInstanceEntity.getCandAppId(), instanceEntity.getCandAppId());
        //更新实例心跳状态
        instanceService.updateHeartStatus(Lists.newArrayList(instanceEntity), false, 1);
        Assert.assertEquals(0, instanceService.findByCandInstanceId("w545878765").getHeartStatus());
        CombinedInstanceDto combinedInstanceDto = (CombinedInstanceDto) uiInstanceService.findInstance(request, parameterMap).getData().get(0);
        Assert.assertEquals(testInstanceEntity.getAppClusterName(), combinedInstanceDto.getAppClusterName());
        Assert.assertEquals(testInstanceEntity.getCandAppId(), combinedInstanceDto.getCandAppId());
        uiInstanceService.setFinalStatus(combinedInstanceDto);
        Assert.assertEquals(0, combinedInstanceDto.getFinalStatus());
    }

    @Test
    public void testUpdateSupperStatus() throws Exception{
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        Thread.sleep(2 * 1000);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        HttpServletRequest request = new MockHttpServletRequest();
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("w545878765");
        Assert.assertEquals(testInstanceEntity.getAppClusterName(), instanceEntity.getAppClusterName());
        int superStatus = instanceEntity.getSupperStatus();
        String result = uiInstanceService.updateSupperStatus(request, Integer.toString(superStatus), "1", instanceEntity.getId());
        Assert.assertEquals("false", result);
    }

    @Test
    public void testUpdatePublishStatus() throws Exception{
        RegisterInstanceRequest registerInstanceRequest = RegisterInstanceUtil.setRegisterInstanceRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        Thread.sleep(2 * 1000);
        InstanceEntity testInstanceEntity = RegisterInstanceUtil.createTestInstanceEntity();
        HttpServletRequest request = new MockHttpServletRequest();
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("w545878765");
        Assert.assertEquals(testInstanceEntity.getAppClusterName(), instanceEntity.getAppClusterName());
        int publishStatus = instanceEntity.getPubStatus();
        String result = uiInstanceService.updatePublishStatus(request, Integer.toString(publishStatus), instanceEntity.getId());
        Assert.assertEquals("false", result);
    }


    private Map<String, Object> setParameterMap(Map<String, Object> parameterMap) {
        parameterMap.put("clusterName", "wCluster");
        parameterMap.put("appId", "w4376432739387");
        return parameterMap;
    }

    private void deRegister(long instanceId) {
        DeRegisterInstanceRequest deRegisterInstanceRequest = new DeRegisterInstanceRequest();
        deRegisterInstanceRequest.setInstanceId(instanceId);
        instanceService.deRegister(deRegisterInstanceRequest);
    }
}
