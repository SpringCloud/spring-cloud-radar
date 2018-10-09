package com.ppdai.infrastructure.ui.service;

import com.ppdai.infrastructure.radar.biz.bo.OrganizationBo;
import com.ppdai.infrastructure.radar.biz.dal.UiAppRepository;
import com.ppdai.infrastructure.ui.dto.request.AppUpdateRequest;
import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import com.ppdai.infrastructure.radar.biz.service.AppService;
import com.ppdai.infrastructure.radar.biz.service.RoleService;
import com.ppdai.infrastructure.radar.biz.service.UserService;
import com.ppdai.infrastructure.radar.biz.bo.UserBo;
import com.ppdai.infrastructure.ui.service.common.UiResponseHelper;
import com.ppdai.infrastructure.ui.vo.AppEntityVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * UiAppService
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Service
public class UiAppService {
    @Autowired
    private UiAppRepository appRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AppService appService;
    @Autowired
    private UserService userService;
    /**
     * 根据实例的candAppId查找app
     * @param instanceId
     * @return
     */
    public UiResponse findByInstanceId(String instanceId) {
        UiResponse uiResponse = new UiResponse();
        Long count;
        List<AppEntity> appList;
        List<AppEntityVo> appEntityVoList=new ArrayList<>();
        count = appRepository.appCount(instanceId);
        appList = appRepository.findByInstanceId(instanceId);
        for(AppEntity appEntity:appList){
            AppEntityVo appEntityVo=new AppEntityVo(appEntity);
            appEntityVo.setRole(roleService.getRole(appEntity));
            appEntityVoList.add(appEntityVo);
        }
        return UiResponseHelper.setUiResponse(uiResponse, count, appEntityVoList);
    }

    public UiResponse findByAppName(Map parameterMap) {
        UiResponse uiResponse = new UiResponse();
        Long count;
        List<AppEntity> appList;
        List<AppEntityVo> appEntityVoList=new ArrayList<>();
        count = appRepository.countByAppName(parameterMap);
        appList = appRepository.findByAppName(parameterMap);
        for(AppEntity appEntity:appList){
            AppEntityVo appEntityVo=new AppEntityVo(appEntity);
            appEntityVo.setRole(roleService.getRole(appEntity));
            appEntityVoList.add(appEntityVo);
        }
        return UiResponseHelper.setUiResponse(uiResponse, count, appEntityVoList);
    }

    public int update(AppEntity updateEntity) {
        int result = 0;
        try {
            appService.updateVersionByIds(Arrays.asList(updateEntity.getId()));
            result = appRepository.update(updateEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public UiResponse update(AppUpdateRequest appUpdateRequest){
        UiResponse uiResponse=new UiResponse();
        checkUpdate(appUpdateRequest,uiResponse);
        if(uiResponse.isSuc()){
            AppEntity appEntity=setUpdateEntity(appUpdateRequest);
            update(appEntity);
            uiResponse.setMsg("更新成功");
        }
        return uiResponse;
    }

    public AppEntity setUpdateEntity(AppUpdateRequest appUpdateRequest) {
        AppEntity appEntity=new AppEntity();
        appEntity.setId(appUpdateRequest.getId());
        if(!StringUtils.isEmpty(appUpdateRequest.getAppName())){
            appEntity.setAppName(appUpdateRequest.getAppName());
        }
        if(!StringUtils.isEmpty(appUpdateRequest.getDepartmentId())){
            OrganizationBo organizationBo = userService.getOrgs().get(appUpdateRequest.getDepartmentId());
            appEntity.setDepartmentId(organizationBo.getOrgId());
            appEntity.setDepartmentName(organizationBo.getOrgName());
        }
        appEntity.setDomain(appUpdateRequest.getDomain());
        if(!StringUtils.isEmpty(appUpdateRequest.getAllowCross())){
            appEntity.setAllowCross(appUpdateRequest.getAllowCross());
        }
        if(!StringUtils.isEmpty(appUpdateRequest.getOwnerId())){
            String[] userInfo = getUserInfo(appUpdateRequest.getOwnerId());
            appEntity.setOwnerId(appUpdateRequest.getOwnerId());
            appEntity.setOwnerName(userInfo[0]);
            appEntity.setOwnerEmail(userInfo[1]);

        }
        if(!StringUtils.isEmpty(appUpdateRequest.getMemberId())){
            String[] userInfo = getUserInfo(appUpdateRequest.getMemberId());
            appEntity.setMemberId(appUpdateRequest.getMemberId());
            appEntity.setMemberName(userInfo[0]);
            appEntity.setMemberEmail(userInfo[1]);
        }
        return appEntity;
    }

    private String[] getUserInfo(String userIds){
        String[] userInfo=new String[]{"",""};
        if(!StringUtils.isEmpty(userIds)){
            String[] userIdArr=userIds.split(",");
            for(int i=0;i<userIdArr.length;i++){
                Map<String, UserBo> users = userService.getUsers();
                UserBo userBo = users.get(userIdArr[i]);
                if(userBo!=null){
                    if(i==0){
                        userInfo[0]+=userBo.getName();
                        userInfo[1]+=userBo.getEmail();

                    }else{
                        userInfo[0]+=","+userBo.getName();
                        userInfo[1]+=","+userBo.getEmail();
                    }
                }
            }
        }
        return userInfo;
    }

    private void checkUpdate(AppUpdateRequest appUpdateRequest,UiResponse uiResponse){
        Map<String, String> appNameIdMap = appService.getAppNameIdCacheData();
        if (appNameIdMap.containsKey(appUpdateRequest.getAppName())) {
            if (!appNameIdMap.get(appUpdateRequest.getAppName()).equals(appUpdateRequest.getCandAppId())) {
                uiResponse.setSuc(false);
                uiResponse.setMsg("更新失败，appName:" + appUpdateRequest.getAppName() + " 已存在");
                return;
            }
        }
        uiResponse.setSuc(true);
    }
}
