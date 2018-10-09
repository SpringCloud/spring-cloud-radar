package com.ppdai.infrastructure.radar.client;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import com.google.common.collect.Lists;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.base.AppDto;
import com.ppdai.infrastructure.radar.biz.dto.base.AppMetaDto;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppMetaRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppMetaResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.HeartBeatRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.HeartBeatResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterClientRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterClientResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;

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
    }

    private void initRegisterInstanceRequestAndResponse() {
        registerInstanceRequest = new RegisterInstanceRequest();
        registerInstanceRequest.setCandAppId("10010001");
        registerInstanceRequest.setAppName("RegistryInstance");
        registerInstanceRequest.setCandInstanceId("262a15e7-6b60-432d-abdb-e939fff2fd76");
        registerInstanceRequest.setClientIp("10.1.30.2");
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
        registerInstanceRequest_retry.setClientIp("10.1.30.2");
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
        registerInstanceRequest_fail_noretry.setClientIp("10.1.30.2");
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
                                .withBody(JsonUtil.toJsonNull(registerInstanceRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JsonUtil.toJsonNull(registerInstanceResponse))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/registerInstance")
                                .withBody(JsonUtil.toJsonNull(registerInstanceRequest_retry))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JsonUtil.toJsonNull(registerInstanceResponse_retry))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/registerInstance")
                                .withBody(JsonUtil.toJsonNull(registerInstanceRequest_fail_noretry))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JsonUtil.toJsonNull(registerInstanceResponse_fail_noretry))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/registerClient")
                                .withBody(JsonUtil.toJsonNull(registerClientRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JsonUtil.toJsonNull(registerClientResponse))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/heartbeat")
                                .withBody(JsonUtil.toJsonNull(heartBeatRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JsonUtil.toJsonNull(heartBeatResponse))
                                //.withDelay(new Delay(SECONDS, 40))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/instance/deRegisterInstance")
                                .withBody(JsonUtil.toJsonNull(deRegisterRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JsonUtil.toJsonNull(deRegisterResponse))
                );

        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/getApp")
                                .withBody(JsonUtil.toJsonNull(getAppRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JsonUtil.toJsonNull(getAppResponse))
                );

        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/getAppPolling")
                                .withBody(JsonUtil.toJsonNull(getAppRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JsonUtil.toJsonNull(getAppResponse))
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request().withMethod("POST")
                                .withPath("/api/client/app/getAppMeta")
                                .withBody(JsonUtil.toJsonNull(getAppMetaRequest))
                )
                .respond(
                        response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JsonUtil.toJsonNull(getAppMetaResponse))
                );

    }




    @After
    public void stopProxy() {
        mockRegistryServer.stop();
    }
}
