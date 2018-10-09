package com.ppdai.infrastructure.ui.service.impl;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import com.ppdai.infrastructure.radar.biz.service.RoleService;
import com.ppdai.infrastructure.radar.biz.service.UserService;

/**
 * UserService
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Service
public class DefaultRoleService implements RoleService {
    private static final int superUser = 0;
    private static final int departmentManager = 1;
    private static final int generalUser = 2;

    @Autowired
    UserService userService;

    /**
     * 从token中获取用户信息
     *
     * @param appEntity
     * @return
     */
    @Override
    public int getRole(AppEntity appEntity) {
        String userId=userService.getCurrentUser().getUserId();
        if(!StringUtils.isEmpty(userId)) {
            if(userService.getCurrentUser().isAdmin()){
                return superUser;
            }
            if(appEntity!=null){
                String onwner=appEntity.getOwnerId();
                String member=appEntity.getMemberId();
                if(!StringUtils.isEmpty(onwner)&&Arrays.asList(onwner.split(",")).contains(userId)){
                    return departmentManager;
                }
                if(!StringUtils.isEmpty(member)&&Arrays.asList(member.split(",")).contains(userId)){
                    return departmentManager;
                }
            }
        }
        return generalUser;

    }



}
