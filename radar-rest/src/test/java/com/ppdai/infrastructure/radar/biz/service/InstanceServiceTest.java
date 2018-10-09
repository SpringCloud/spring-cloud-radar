package com.ppdai.infrastructure.radar.biz.service;

import com.google.common.collect.Lists;
import com.ppdai.infrastructure.radar.AbstractTest;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.dal.AppClusterRepository;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.*;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


public class InstanceServiceTest extends AbstractTest {
    Logger log = LoggerFactory.getLogger(InstanceServiceTest.class);

    @Autowired
    InstanceService instanceService;

    @Autowired
    AppClusterRepository appClusterRepository;

    @Autowired
    AppCacheService appCacheService;

    @Autowired
    AppService appService;

    @Test
    public void registerInstance() {
        RegisterInstanceRequest registerInstanceRequest = buildRequest();
        RegisterInstanceResponse response = instanceService.registerInstance(registerInstanceRequest);
        log.info(response.getMsg());
        Assert.assertTrue(response.isSuc());
        Assert.assertNotNull(instanceService.findByCandInstanceId("111"));

        Assert.assertNotNull(appClusterRepository.findByUq(registerInstanceRequest.getCandAppId(), registerInstanceRequest.getClusterName()));
    }

    private RegisterInstanceRequest buildRequest() {
        RegisterInstanceRequest registerInstanceRequest = new RegisterInstanceRequest();
        registerInstanceRequest.setAppName("a");
        registerInstanceRequest.setCandAppId("111");
        registerInstanceRequest.setCandInstanceId("111");
        registerInstanceRequest.setClusterName("bb");
        registerInstanceRequest.setClientIp("1.11.1.1");
        registerInstanceRequest.setLan("132");
        registerInstanceRequest.setSdkVersion("1");
        return registerInstanceRequest;
    }

    @Test
    public void registerInstance1() {
        RegisterInstanceRequest registerInstanceRequest = buildRequest();
        Map<String, RegisterInstanceResponse> responseMap = instanceService.registerInstance(Lists.newArrayList(registerInstanceRequest));
        Assert.assertTrue(responseMap.get(registerInstanceRequest.getCandAppId()).isSuc());
        Assert.assertNotNull(instanceService.findByCandInstanceId("111"));

        Assert.assertNotNull(appClusterRepository.findByUq(registerInstanceRequest.getCandAppId(), registerInstanceRequest.getClusterName()));

    }

    @Test
    public void pubDeleteByCandInstanceId() {
        PubDeleteResponse response = instanceService.pubDelete(new PubDeleteRequest());
        Assert.assertFalse(response.isSuc());

        RegisterInstanceRequest registerInstanceRequest = buildRequest();
        instanceService.registerInstance(registerInstanceRequest);
        Assert.assertNotNull(instanceService.findByCandInstanceId("111"));
        instanceService.pubDelete(buildPubDeleteRequest());
        Assert.assertNull(instanceService.findByCandInstanceId("111"));
    }

    @Test
    public void pubDeleteById() {
        RegisterInstanceRequest registerInstanceRequest = buildRequest();
        instanceService.registerInstance(registerInstanceRequest);
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("111");

        instanceService.pubDelete(buildPubDeleteRequest(instanceEntity.getAppId()));
        Assert.assertNull(instanceService.findByCandInstanceId("111"));
    }

    private PubDeleteRequest buildPubDeleteRequest(Long appId) {
        PubDeleteRequest pubDeleteRequest = new PubDeleteRequest();
        pubDeleteRequest.setCandInstanceIds(Lists.newArrayList("111"));
        pubDeleteRequest.setIds(Lists.newArrayList(appId));
        return pubDeleteRequest;
    }

    private PubDeleteRequest buildPubDeleteRequest() {
        PubDeleteRequest pubDeleteRequest = new PubDeleteRequest();
        pubDeleteRequest.setCandInstanceIds(Lists.newArrayList("111"));
        return pubDeleteRequest;
    }

    @Test
    public void adjust() {
        RegisterInstanceRequest registerInstanceRequest = buildRequest();
        instanceService.registerInstance(registerInstanceRequest);
        instanceService.adjust(buildAdjunstRequestTrue());
        Assert.assertEquals(instanceService.findByCandInstanceId("111").getPubStatus(), 1);

        instanceService.adjust(buildAdjunstRequestFalse());
        Assert.assertEquals(instanceService.findByCandInstanceId("111").getPubStatus(), 0);
    }

    private AdjustRequest buildAdjunstRequestTrue() {
        AdjustRequest adjustRequest = new AdjustRequest();
        adjustRequest.setCandInstanceIds(Lists.newArrayList("111"));
        adjustRequest.setUp(true);
        return adjustRequest;
    }

    private AdjustRequest buildAdjunstRequestFalse() {
        AdjustRequest adjustRequest = new AdjustRequest();
        adjustRequest.setCandInstanceIds(Lists.newArrayList("111"));
        adjustRequest.setUp(false);
        return adjustRequest;
    }

    @Test
    public void adjustSupperStatus() {
        AdjustSupperStatusResponse response = instanceService.adjustSupperStatus(buildSuperStatusRequest());
        Assert.assertTrue(response.isSuc());

        Assert.assertEquals(-1, instanceService.findByCandInstanceId("111").getSupperStatus());

    }

    private AdjustSupperStatusRequest buildSuperStatusRequest() {
        instanceService.registerInstance(buildRequest());
        AdjustSupperStatusRequest request = new AdjustSupperStatusRequest();
        request.setCandInstanceIds(Lists.newArrayList("111"));
        request.setStatus(-1);
        return request;
    }

    @Test
    public void addInstance() {
        AddInstancesResponse response = instanceService.addInstance(buildInstanceRequest());
        Assert.assertTrue(response.isSuc());
        Assert.assertNotNull(instanceService.findByCandInstanceId("111"));
    }

    private AddInstancesRequest buildInstanceRequest() {
        AddInstanceDto addInstanceDto = new AddInstanceDto();
        addInstanceDto.setCandInstanceId("111");
        AddInstancesRequest addInstancesRequest = new AddInstancesRequest();
        addInstancesRequest.setCandAppId("11");
        addInstancesRequest.setCandInstances(Lists.newArrayList(addInstanceDto));
        addInstancesRequest.setClusterName("aaa");
        return addInstancesRequest;
    }

    @Test
    public void deRegister() {
        RegisterInstanceResponse response = instanceService.registerInstance(buildRequest());
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("111");
        Assert.assertEquals(1, instanceEntity.getInstanceStatus());
        instanceService.deRegister(buildRequest(instanceEntity.getId()));
        Assert.assertEquals(0, instanceService.findByCandInstanceId("111").getInstanceStatus());
    }

    private DeRegisterInstanceRequest buildRequest(Long id) {
        DeRegisterInstanceRequest request = new DeRegisterInstanceRequest();
        request.setInstanceId(id);
        return request;
    }

    @Test
    public void getStatus() {
        instanceService.registerInstance(buildRequest());
        GetStatusRequest getStatusRequest = new GetStatusRequest();
        getStatusRequest.setCandInstanceIds(Lists.newArrayList("111"));
        GetStatusInstanceDto instanceDto = instanceService.getStatus(getStatusRequest).getPubInstances().get(0);
        log.info(JsonUtil.toJson(instanceDto));
        Assert.assertEquals(1, instanceDto.getHeartStatus());
        Assert.assertEquals(0, instanceDto.getPubStatus());
        Assert.assertEquals(1, instanceDto.getInstanceStatus());
        Assert.assertEquals(0, instanceDto.getSupperStatus());
        Assert.assertEquals(0, instanceDto.getStatus());
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("111");
        instanceService.adjust(buildAdjunstRequestTrue());
        instanceDto = instanceService.getStatus(getStatusRequest).getPubInstances().get(0);
        Assert.assertEquals(1, instanceDto.getPubStatus());
        Assert.assertEquals(1, instanceDto.getStatus());

    }

    @Test
    public void updateHeartStatus() throws InterruptedException {
        instanceService.registerInstance(buildRequest());
        InstanceEntity instanceEntity = instanceService.findByCandInstanceId("111");
        Thread.sleep(2 * 1000);
        instanceService.updateHeartStatus(Lists.newArrayList(instanceEntity), false, 1);
        Assert.assertEquals(0, instanceService.findByCandInstanceId("111").getHeartStatus());

    }

    @Test
    public void getCount() throws InterruptedException {
        instanceService.registerInstance(buildRequest());
        Assert.assertEquals(1, instanceService.getCount());
        Thread.sleep(2000);
        Assert.assertEquals(0, appCacheService.getApp().get("111").getClusters().get(0).getAppClusterMeta().getDeleteFlag());
        instanceService.deleteInstance(instanceService.findByCandInstanceId(Lists.newArrayList("111")));
        Thread.sleep(2000);
        Assert.assertEquals(1, appCacheService.getApp().get("111").getClusters().get(0).getAppClusterMeta().getDeleteFlag());
        Assert.assertEquals(1, appService.getApp(new GetAppRequest()).getApp().get("111").getClusters().get(0).getAppClusterMeta().getDeleteFlag());
    }
}