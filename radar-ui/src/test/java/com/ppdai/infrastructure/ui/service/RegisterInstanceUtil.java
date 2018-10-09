package com.ppdai.infrastructure.ui.service;

import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;

public class RegisterInstanceUtil {
    public static  RegisterInstanceRequest setRegisterInstanceRequest() {
        RegisterInstanceRequest registerInstanceRequest = new RegisterInstanceRequest();
        registerInstanceRequest.setAppName("wApp");
        registerInstanceRequest.setCandInstanceId("w545878765");
        registerInstanceRequest.setCandAppId("w4376432739387");
        registerInstanceRequest.setClusterName("wCluster");
        registerInstanceRequest.setClientIp("172.20.16.23");
        registerInstanceRequest.setLan("java");
        registerInstanceRequest.setSdkVersion("1.0");
        return registerInstanceRequest;
    }

    public static InstanceEntity createTestInstanceEntity() {
        InstanceEntity instanceEntity = new InstanceEntity();
        instanceEntity.setCandAppId("w4376432739387");
        instanceEntity.setAppName("wApp");
        instanceEntity.setAppClusterName("wCluster");
        instanceEntity.setCandInstanceId("w545878765");
        instanceEntity.setIp("172.20.16.23");
        instanceEntity.setPort(8080);
        instanceEntity.setLan("java");
        instanceEntity.setSdkVersion("1.0");
        return instanceEntity;
    }

}
