package com.ppdai.infrastructure.ui.controller;

import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.ui.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConfigController {
    @Autowired
    ConfigService configService;

    /**
     * 显示内部配置项的页面
     *
     * @param model
     * @return
     */
    @GetMapping("/app/soaConfig")
    public String getConfig(Model model) {

        return "app/soaConfig";
    }


    @RequestMapping("/app/soaConfig/data")
    @ResponseBody
    public UiResponse getConfigData(){
        return configService.getConfigData();
    }


}
