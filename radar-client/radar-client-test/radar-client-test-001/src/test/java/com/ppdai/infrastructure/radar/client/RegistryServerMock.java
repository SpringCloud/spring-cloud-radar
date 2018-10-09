package com.ppdai.infrastructure.radar.client;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.base.AppDto;
import com.ppdai.infrastructure.radar.biz.dto.base.AppMetaDto;
import com.ppdai.infrastructure.radar.biz.dto.client.*;
import org.junit.After;
import org.junit.Before;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.util.HashMap;
import java.util.Map;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Created by zhangyicong on 17-12-15.
 */
public class RegistryServerMock {

    private ClientAndServer mockRegistryServer;

    protected RegisterInstanceRequest registerInstanceRequest;
    protected RegisterInstanceResponse registerInstanceResponse;

    protected RegisterInstanceRequest registerInstanceRequest_retry;
    protected RegisterInstanceResponse registerInstanceResponse_retry;

    protected RegisterInstanceRequest registerInstanceRequest_fail_noretry;
    protected RegisterInstanceResponse registerInstanceResponse_fail_noretry;

    protected HeartBeatRequest heartBeatRequest;
    protected HeartBeatResponse heartBeatResponse;

    protected RegisterClientRequest registerClientRequest;
    protected RegisterClientResponse registerClientResponse;

    protected DeRegisterInstanceRequest deRegisterRequest;
    protected DeRegisterInstanceResponse deRegisterResponse;

    protected GetAppRequest getAppRequest;
    protected GetAppResponse getAppResponse;

    protected GetAppMetaRequest getAppMetaRequest;
    protected GetAppMetaResponse getAppMetaResponse;

    private void initDeRegisterRequestAndResponse() {
        deRegisterRequest = new DeRegisterInstanceRequest();
        deRegisterRequest.setInstanceId(1);

        deRegisterResponse = new DeRegisterInstanceResponse();
        deRegisterResponse.setSuc(true);
        heartBeatResponse.setCode(RadarConstanst.NO);
    }

    private void initHeartBeatRequestAndResponse() {
        heartBeatRequest = new HeartBeatRequest();
        heartBeatRequest.setInstanceId(1);

        heartBeatResponse = new HeartBeatResponse();
        heartBeatResponse.setSuc(true);
        heartBeatResponse.setCode(RadarConstanst.NO);
        heartBeatResponse.setHeartbeatTime(2);
    }

    private void initRegisterInstanceRequestAndResponse() {
        registerInstanceRequest = new RegisterInstanceRequest();
        registerInstanceRequest.setCandAppId("10010001");
        registerInstanceRequest.setAppName("RegistryInstance");
        registerInstanceRequest.setCandInstanceId("262a15e7-6b60-432d-abdb-e939fff2fd76");
        registerInstanceRequest.setIp("10.1.30.2");
        registerInstanceRequest.setPort(0);
        registerInstanceRequest.setLan("java");
        registerInstanceRequest.setSdkVersion("0.0.1");
        registerInstanceRequest.setClusterName("dc1.prd");

        registerInstanceResponse = new RegisterInstanceResponse();
        registerInstanceResponse.setInstanceId(1);
        registerInstanceResponse.setSuc(true);
    }

    private void initRegisterInstanceRequestAndResponse_retry() {
        registerInstanceRequest_retry = new RegisterInstanceRequest();
        registerInstanceRequest_retry.setCandAppId("10010001");
        registerInstanceRequest_retry.setAppName("RegistryInstanceRetry");
        registerInstanceRequest_retry.setCandInstanceId("262a15e7-6b60-432d-abdb-e939fff2fd76");
        registerInstanceRequest_retry.setIp("10.1.30.2");
        registerInstanceRequest_retry.setPort(0);
        registerInstanceRequest_retry.setLan("java");
        registerInstanceRequest_retry.setSdkVersion("0.0.1");
        registerInstanceRequest_retry.setClusterName("dc1.prd");

        registerInstanceResponse_retry = new RegisterInstanceResponse();
        registerInstanceResponse_retry.setInstanceId(1);
        registerInstanceResponse_retry.setSuc(false);
        registerInstanceResponse_retry.setCode(RadarConstanst.NO);
    }

    private void initRegisterInstanceRequestAndResponse_fail_noretry() {
        registerInstanceRequest_fail_noretry = new RegisterInstanceRequest();
        registerInstanceRequest_fail_noretry.setCandAppId("10010001");
        registerInstanceRequest_fail_noretry.setAppName("RegistryInstanceFailNoRetry");
        registerInstanceRequest_fail_noretry.setCandInstanceId("262a15e7-6b60-432d-abdb-e939fff2fd76");
        registerInstanceRequest_fail_noretry.setIp("10.1.30.2");
        registerInstanceRequest_fail_noretry.setPort(0);
        registerInstanceRequest_fail_noretry.setLan("java");
        registerInstanceRequest_fail_noretry.setSdkVersion("0.0.1");
        registerInstanceRequest_fail_noretry.setClusterName("dc1.prd");

        registerInstanceResponse_fail_noretry = new RegisterInstanceResponse();
        registerInstanceResponse_fail_noretry.setInstanceId(1);
        registerInstanceResponse_fail_noretry.setSuc(false);
        registerInstanceResponse_fail_noretry.setCode(RadarConstanst.YES);
    }

    private void initGetAppRequestAndResponse() {
        getAppRequest = new GetAppRequest();
        Map<String, Long> appVersion = new HashMap<>();
        appVersion.put("1111", 0L);
        getAppRequest.setAppVersion(appVersion);
        getAppRequest.setContainOffline(false);
        getAppRequest.setIp("1.1.1.1");
        getAppRequest.setInTime(0L);

        getAppResponse = new GetAppResponse();
        getAppResponse.setSleepTime(1);
        AppDto appDto = new AppDto();
        AppMetaDto appMetaDto = new AppMetaDto();
        appMetaDto.setVersion(0L);
        appDto.setAppMeta(appMetaDto);
        Map<String, AppDto> app = new HashMap<>();
        app.put("1111", appDto);
        getAppResponse.setApp(app);
        getAppResponse.setSuc(true);
        getAppResponse.setCode(RadarConstanst.YES);
        getAppResponse.setSleepTime(0);
    }

    private void initGetAppMetaRequestAndResponse() {
        getAppMetaRequest = new GetAppMetaRequest();
        getAppMetaResponse = new GetAppMetaResponse();
        AppMetaDto appMetaDto = new AppMetaDto();
        appMetaDto.setVersion(1L);
        appMetaDto.setCandAppId("121");
        getAppMetaResponse.setAppMetas(Lists.newArrayList(appMetaDto));
        getAppMetaResponse.setCode(RadarConstanst.YES);
        getAppMetaResponse.setSuc(true);
    }

    @Before
    public void setUp() {

        initRegisterInstanceRequestAndResponse();
        initRegisterInstanceRequestAndResponse_retry();
        initRegisterInstanceRequestAndResponse_fail_noretry();
        initHeartBeatRequestAndResponse();
        initDeRegisterRequestAndResponse();
        initGetAppRequestAndResponse();
        initGetAppMetaRequestAndResponse();

        mockRegistryServer = startClientAndServer(1080);
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/registerInstance")
                                .withBody(JSON.toJSONString(registerInstanceRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JSON.toJSONString(registerInstanceResponse))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/registerInstance")
                                .withBody(JSON.toJSONString(registerInstanceRequest_retry))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JSON.toJSONString(registerInstanceResponse_retry))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/registerInstance")
                                .withBody(JSON.toJSONString(registerInstanceRequest_fail_noretry))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JSON.toJSONString(registerInstanceResponse_fail_noretry))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/registerClient")
                                .withBody(JSON.toJSONString(registerClientRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JSON.toJSONString(registerClientResponse))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/heartbeat")
                                .withBody(JSON.toJSONString(heartBeatRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JSON.toJSONString(heartBeatResponse))
                                //.withDelay(new Delay(SECONDS, 40))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/deRegisterInstance")
                                .withBody(JSON.toJSONString(deRegisterRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JSON.toJSONString(deRegisterResponse))
                );

        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/getApp")
                                .withBody(JSON.toJSONString(getAppRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JSON.toJSONString(getAppResponse))
                );

        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/getAppPolling")
                                .withBody(JSON.toJSONString(getAppRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JSON.toJSONString(getAppResponse))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/getAppMeta")
                                .withBody(JSON.toJSONString(getAppMetaRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JSON.toJSONString(getAppMetaResponse))
                );

    }




    @After
    public void stopProxy() {
        mockRegistryServer.stop();
    }
}
