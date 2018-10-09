package com.ppdai.infrastructure.radar.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ppdai.infrastructure.radar.client.dto.RadarInstance;

/**
 * Created by zhangyicong on 17-12-11.
 */
public class InstanceRadarTest {

    @Test
    public void testInstanceRadar() {
        String instanceId = "1-1-1-1";
        String host = "10.1.3.10";
        String appId = "10240001";
        String appName = "authtoken";
        String cluster = "dc1.pro";


        RadarInstance instanceRadar = RadarInstance.getBuilder()
                .withCandInstanceId(instanceId)
                .withHost(host)
                .withCandAppId(appId)
                .withAppName(appName)
                .withClusterName(cluster).build();

        assertEquals(instanceId, instanceRadar.getCandInstanceId());
        assertEquals(host, instanceRadar.getHost());
        assertEquals(appId, instanceRadar.getCandAppId());
        assertEquals(appName, instanceRadar.getAppName());
        assertEquals(cluster, instanceRadar.getClusterName());
    }
}
