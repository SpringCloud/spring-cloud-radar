package com.ppdai.infrastructure.ui.controller;

import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.ui.service.ConfigService;
import com.ppdai.infrastructure.ui.service.SdkVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SdkVersionController {
    @Autowired
    SdkVersionService sdkVersionService;

    /**
     * sdk版本的使用统计页面
     *
     * @return
     */
    @GetMapping("/app/sdkVersion")
    public String getConfig() {
        return "app/sdkVersion";
    }


    @RequestMapping("/app/sdkVersion/data")
    @ResponseBody
    public UiResponse getConfigData(){
        return sdkVersionService.getAllSdkVersion();
    }


}
