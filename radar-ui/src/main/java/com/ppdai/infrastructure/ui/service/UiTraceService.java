package com.ppdai.infrastructure.ui.service;

import java.util.List;
import java.util.Map;

import com.ppdai.infrastructure.radar.biz.dal.SoaLockRepository;
import com.ppdai.infrastructure.radar.biz.dto.ui.CombinedInstanceDto;
import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.radar.biz.entity.SoaLockEntity;
import com.ppdai.infrastructure.ui.service.common.UiResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.dal.UiConnectionsRepository;
import com.ppdai.infrastructure.ui.service.common.Constant;

/**
 * TraceController
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Service
public class UiTraceService {
    @Autowired
    private SoaConfig soaConfig;
    @Autowired
    private UiConnectionsRepository uiConnectionsRepository;
    @Autowired
    private SoaLockRepository soaLockRepository;

    private RestTemplate restTemplate = new RestTemplate();

    public String stat() {
        String url = soaConfig.getRadarUrl() + Constant.getStatUrl();
        return call(url, String.class);
    }

    public Object cache1() {
        return call(soaConfig.getRadarUrl() + Constant.getCache1Url(), Map.class);
    }

    public Object cache(Integer flag, String appId) {
        String url = soaConfig.getRadarUrl() + Constant.getCacheUrl();
        return call(url + "?flag=" + flag + "&appId=" + appId, Map.class);
    }

    public Object trace() {
        return call(soaConfig.getRadarUrl() + Constant.getTraceUrl(), Map.class);
    }

    public String test() {
        String url = soaConfig.getRadarUrl() + Constant.getTestUrl();
        return call(url, String.class);
    }

    private <T> T call(String url, Class<T> t) {
        Transaction transaction = Tracer.newTransaction("radar-rest", url);
        try {
            ResponseEntity<T> responseEntity = restTemplate.getForEntity(url, t);
            transaction.setStatus(Transaction.SUCCESS);
            return responseEntity.getBody();
        } catch (Exception e) {
            transaction.setStatus(e);
        } finally {
            transaction.complete();
        }
        return null;
    }

    public String connections() {
        try{
            Map maxConnectionMap = uiConnectionsRepository.maxConnectionsCount();
            Map ThreadsRunningMap = uiConnectionsRepository.connectionsCount();
            String result = "最大连接数为：" +maxConnectionMap.get("Value")+ "<br/>当前连接数为：" + ThreadsRunningMap.get("Value");
            return result;
        }
        catch(Exception e){
            return "获取连接数异常，异常信息为："+e.getMessage();
        }

    }

    public UiResponse getLockData(Map parameterMap){
        UiResponse uiResponse = new UiResponse();
        Long count;
        List<SoaLockEntity> lockDataList;

        count = soaLockRepository.countBy(parameterMap);
        lockDataList = soaLockRepository.findBy(parameterMap);
        return UiResponseHelper.setUiResponse(uiResponse, count, lockDataList);
    }

}
