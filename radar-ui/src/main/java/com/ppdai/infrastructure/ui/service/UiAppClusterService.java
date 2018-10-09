package com.ppdai.infrastructure.ui.service;

import com.ppdai.infrastructure.radar.biz.dal.UiAppClusterRepository;
import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.radar.biz.entity.AppClusterEntity;
import com.ppdai.infrastructure.radar.biz.service.AppClusterService;
import com.ppdai.infrastructure.ui.service.common.UiResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * UiAppClusterService
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Service
public class UiAppClusterService  {
    @Autowired
    private UiAppClusterRepository uiAppClusterRepository;

    /**
     * 根据实例的自增id查询
     *
     * @param instanceId
     * @return
     */
    public UiResponse findByInstanceId(String instanceId) {
        UiResponse uiResponse = new UiResponse();
        Long count;
        List<AppClusterEntity> appClusterList;
        count = uiAppClusterRepository.appClusterCount(instanceId);
        appClusterList = uiAppClusterRepository.findByInstanceId(instanceId);
        return UiResponseHelper.setUiResponse(uiResponse, count, appClusterList);
    }

    /**
     * 根据集群名查询
     *
     * @param parameterMap
     * @return
     */
    public UiResponse findByClusterName(Map parameterMap) {
        UiResponse uiResponse = new UiResponse();
        Long count;
        List<AppClusterEntity> appClusterList;

        count = uiAppClusterRepository.countByAppIdOrClusterName(parameterMap);
        appClusterList = uiAppClusterRepository.findByAppIdOrClusterName(parameterMap);
        return UiResponseHelper.setUiResponse(uiResponse, count, appClusterList);
    }


}
