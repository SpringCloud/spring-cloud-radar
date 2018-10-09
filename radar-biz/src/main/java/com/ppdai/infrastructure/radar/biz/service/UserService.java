package com.ppdai.infrastructure.radar.biz.service;


import com.ppdai.infrastructure.radar.biz.bo.OrganizationBo;
import com.ppdai.infrastructure.radar.biz.bo.UserBo;

import java.util.Map;

/**
 * UserService
 *
 * @author wanghe
 * @date 2018/03/21
 */
public interface UserService {
    /**
     * 登录验证
     * @param userName
     * @param passWord
     * @return
     */
    boolean login(String userName, String passWord);

    Map<String, UserBo> getUsers();

    Map<String, OrganizationBo> getOrgs();

    UserBo getCurrentUser();
}
