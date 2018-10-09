package com.ppdai.infrastructure.radar.client.resource;

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
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstancesRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstancesResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.PubDeleteRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.PubDeleteResponse;

/**
 * Created by zhangyicong on 17-12-12.
 */
public interface RadarResource {
	
	void hs();
	
    /**
     * 注册实例
     * @param request
     * @return
     */
    RegisterInstanceResponse registerInstance(RegisterInstanceRequest request) throws Exception;

    /**
     * 注销实例
     * @param request
     * @return
     */
    DeRegisterInstanceResponse deRegisterInstance(DeRegisterInstanceRequest request) throws Exception;

    /**
     * 注册应用消费的客户端
     * @param request
     * @return
     */
    RegisterClientResponse registerClient(RegisterClientRequest request) throws Exception;

    /**
     * 心跳检查
     * @param request
     * @return
     */
    HeartBeatResponse heartbeat(HeartBeatRequest request) throws Exception;

    /**
     * 查询应用，立马返回
     * @param request
     * @return
     */
    GetAppResponse getApp(GetAppRequest request) throws Exception;
    
    /**
     * 查询应用，立马返回
     * @param request
     * @return
     */
    GetAppMetaResponse getAppMeta(GetAppMetaRequest request) throws Exception;
    
    /**
     * 获取长连接，通知
     * @param request
     * @return
     */
    GetAppResponse getAppPolling(GetAppRequest request) throws Exception;
    
    
    /**
     * 调整实例上下线
     * @param request
     * @return
     */
    AdjustResponse adjust(AdjustRequest request) throws Exception;
    
    
    AddInstancesResponse addInstance(AddInstancesRequest request) throws Exception;
    
    GetStatusResponse getStatus(GetStatusRequest request)throws Exception;
    
    
    PubDeleteResponse pubDelete(PubDeleteRequest request) throws Exception;
    
}
