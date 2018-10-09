package com.ppdai.infrastructure.ui.config;

import com.ppdai.infrastructure.radar.biz.common.util.SpringUtil;
import com.ppdai.infrastructure.radar.biz.service.AppService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class BootstraprListener implements ApplicationListener<ContextRefreshedEvent> {


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        AppService appService = SpringUtil.getBean(AppService.class);
        if(appService!=null){
            appService.startCache();
        }
    }
}
