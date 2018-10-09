package com.ppdai.infrastructure.ui.vo;

import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import org.springframework.beans.BeanUtils;

public class AppEntityVo extends AppEntity{

    private int role=2;

    public AppEntityVo(AppEntity appEntity){
        BeanUtils.copyProperties(appEntity,this);
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
