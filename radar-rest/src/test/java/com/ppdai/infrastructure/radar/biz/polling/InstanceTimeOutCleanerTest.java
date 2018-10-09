package com.ppdai.infrastructure.radar.biz.polling;

import com.google.common.collect.Lists;
import com.ppdai.infrastructure.radar.AbstractTest;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusInstanceDto;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusRequest;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;
import com.ppdai.infrastructure.radar.biz.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class InstanceTimeOutCleanerTest extends AbstractTest {

    @Autowired
    private InstanceTimeOutCleaner instanceTimeOutCleaner;

    @Autowired
    private InstanceService instanceService;


    @Test
    public void checkNormalHeartTime() {
        instanceTimeOutCleaner.stop();
        InstanceEntity instanceEntity = new InstanceEntity();
        instanceEntity.setCandInstanceId("121");
        instanceEntity.setIp("1.1.1.1");
        instanceEntity.setHeartStatus(0);
        instanceService.save(instanceEntity);
        GetStatusRequest getStatusRequest = new GetStatusRequest();
        getStatusRequest.setCandInstanceIds(Lists.newArrayList("121"));
        Assert.assertEquals(0, instanceService.findByCandInstanceId("121").getHeartStatus());
        instanceTimeOutCleaner.checkNormalHeartTime();
        Assert.assertEquals(1, instanceService.findByCandInstanceId("121").getHeartStatus());
        instanceService.deleteInstance(Lists.newArrayList(instanceService.findByCandInstanceId("121")) );
    }

    @Test
    public void checkHeartTime() throws InterruptedException {
        instanceTimeOutCleaner.stop();
        InstanceEntity instanceEntity = new InstanceEntity();
        instanceEntity.setCandInstanceId("123");
        instanceEntity.setIp("1.1.1.10");
        instanceEntity.setHeartStatus(1);
        instanceService.save(instanceEntity);
        GetStatusRequest getStatusRequest = new GetStatusRequest();
        getStatusRequest.setCandInstanceIds(Lists.newArrayList("123"));
        Assert.assertEquals(1, instanceService.findByCandInstanceId("123").getHeartStatus());
        Thread.sleep(14 * 1000);
        instanceTimeOutCleaner.checkExpiredHeartTime();
        Assert.assertEquals(0, instanceService.findByCandInstanceId("123").getHeartStatus());
        instanceService.deleteInstance(Lists.newArrayList(instanceService.findByCandInstanceId("123")) );
    }


}