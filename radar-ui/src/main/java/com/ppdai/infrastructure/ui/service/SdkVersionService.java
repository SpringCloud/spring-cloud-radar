package com.ppdai.infrastructure.ui.service;

import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.ui.service.common.UiResponseHelper;
import com.ppdai.infrastructure.ui.vo.SdkVersionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SdkVersionService {
    @Autowired
    private UiInstanceService uiInstanceService;

    public UiResponse getAllSdkVersion() {
        UiResponse uiResponse = new UiResponse();
        Map<String, Integer> sdkVersionMap = new HashMap<String, Integer>();
        List<String> sdkVersionList = uiInstanceService.getAllSdkVersion();
        List<SdkVersionVo>list=new ArrayList<>();

        for (String version:sdkVersionList) {
            if(sdkVersionMap.containsKey(version)){
                Integer count = sdkVersionMap.get(version);
                count++;
                sdkVersionMap.put(version, count);
            }else{
                sdkVersionMap.put(version,1);
            }
        }

        for(String version:sdkVersionMap.keySet()){
            SdkVersionVo sdkVersionVo=new SdkVersionVo();
            sdkVersionVo.setVersion(version);
            sdkVersionVo.setNum(sdkVersionMap.get(version));
            list.add(sdkVersionVo);
        }

        return UiResponseHelper.setUiResponse(uiResponse, new Long(list.size()), list);
    }
}
