package com.ppdai.infrastructure.ui.service;

import com.ppdai.infrastructure.radar.biz.dal.UiInstanceRepository;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustSupperStatusRequest;
import com.ppdai.infrastructure.radar.biz.dto.ui.CombinedInstanceDto;
import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;
import com.ppdai.infrastructure.radar.biz.service.AppService;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;
import com.ppdai.infrastructure.radar.biz.service.RoleService;
import com.ppdai.infrastructure.ui.service.common.Constant;
import com.ppdai.infrastructure.ui.service.common.UiResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UiInstanceService
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Service
public class UiInstanceService{

    @Autowired
    private UiInstanceRepository uiInstanceRepository;
    @Autowired
    private RoleService uiUserService;
    @Autowired
    private UiAuditLogService uiAuditLogService;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private AppService appService;


    /**
     * 根据条件查询实例
     *
     * @param request
     * @param parameterMap
     * @return
     */
    public UiResponse findInstance(HttpServletRequest request, Map parameterMap) {
        Map<String, AppEntity> appMap=appService.getCacheData();
        UiResponse uiResponse = new UiResponse();
        Long count;
        List<CombinedInstanceDto> instanceList;

        count = uiInstanceRepository.countBy(parameterMap);
        instanceList = uiInstanceRepository.findBy(parameterMap);

        //查询每个实例对应的服务数量
        for (CombinedInstanceDto combinedInstanceDto : instanceList) {
            //调用添加角色的方法
            combinedInstanceDto.setRole(uiUserService.getRole(appMap.get(combinedInstanceDto.getCandAppId())));
            setFinalStatus(combinedInstanceDto);
        }
        return UiResponseHelper.setUiResponse(uiResponse, count, instanceList);
    }


    /**
     * 根据条件查询过期实例
     *
     * @param request
     * @param parameterMap
     * @return
     */
    public UiResponse findExpiredInstance(HttpServletRequest request, Map parameterMap) {
        Map<String, AppEntity> appMap=appService.getCacheData();
        UiResponse uiResponse = new UiResponse();
        Long count;
        List<CombinedInstanceDto> instanceList;

        count = uiInstanceRepository.expiredCount(parameterMap);
        instanceList = uiInstanceRepository.findExpiredBy(parameterMap);

        //查询每个实例对应的服务数量
        for (CombinedInstanceDto combinedInstanceDto : instanceList) {
            //调用添加角色的方法
            combinedInstanceDto.setRole(uiUserService.getRole(appMap.get(combinedInstanceDto.getCandAppId())));
            setFinalStatus(combinedInstanceDto);
        }
        return UiResponseHelper.setUiResponse(uiResponse, count, instanceList);
    }

    public UiResponse deleteById(long instanceId){
        UiResponse uiResponse=new UiResponse();
        instanceService.deleteById(instanceId);
        uiResponse.setCode(0+"");
        uiResponse.setMsg("删除成功！");
        return uiResponse;
    }


    /**
     * 根据instanceId查找
     *
     * @param request
     * @param instanceId
     * @return
     */
    public UiResponse findByInstanceId(HttpServletRequest request, String instanceId) {
        Map<String, AppEntity> appMap=appService.getCacheData();
        UiResponse uiResponse = new UiResponse();
        Long count;
        List<CombinedInstanceDto> instanceList;
        count = uiInstanceRepository.instanceCount(instanceId);
        instanceList = uiInstanceRepository.findByInstanceId(instanceId);
        for (CombinedInstanceDto combinedInstanceDto : instanceList) {
            //调用添加角色的方法
            combinedInstanceDto.setRole(uiUserService.getRole(appMap.get(combinedInstanceDto.getCandAppId())));
            setFinalStatus(combinedInstanceDto);

        }
        return UiResponseHelper.setUiResponse(uiResponse, count, instanceList);
    }

    public void setFinalStatus(CombinedInstanceDto combinedInstanceDto) {
        //最终状态的判断
        if (combinedInstanceDto.getSupperStatus() == 1) {
            combinedInstanceDto.setFinalStatus(1);
        } else if (combinedInstanceDto.getSupperStatus() == -1) {
            combinedInstanceDto.setFinalStatus(0);
        } else if (combinedInstanceDto.getSupperStatus() == 0 &&
                combinedInstanceDto.getPubStatus() == 1 &&
                combinedInstanceDto.getInstanceStatus() == 1 &&
                combinedInstanceDto.getHeartStatus() == 1) {
            combinedInstanceDto.setFinalStatus(1);
        } else {
            combinedInstanceDto.setFinalStatus(0);
        }
    }

    /**
     * 修改超级槽位
     *
     * @param request
     * @param originalStatus
     * @param supperStatus
     * @param instanceId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String updateSupperStatus(HttpServletRequest request, String originalStatus, String supperStatus, long instanceId) {
        Map<String, AppEntity> appMap=appService.getCacheData();
        //获取角色，根据角色判断是否有操作权限
        List<CombinedInstanceDto> combinedInstanceDtos=uiInstanceRepository.findByInstanceId(Long.toString(instanceId));
        int role = uiUserService.getRole(appMap.get(combinedInstanceDtos.get(0).getCandAppId()));
        AdjustSupperStatusRequest adjustSupperStatusRequest = new AdjustSupperStatusRequest();
        try {
            adjustSupperStatusRequest.setStatus(Integer.parseInt(supperStatus));
        } catch (Exception e) {
            return "false";
        }
        List<Long> ids = new ArrayList<>();
        ids.add(instanceId);
        adjustSupperStatusRequest.setIds(ids);
        if (role == Constant.getSuperUser() || role == Constant.getAppManager()) {
            try {
                instanceService.adjustSupperStatus(adjustSupperStatusRequest);
                uiAuditLogService.insertLog(request, "instance", instanceId, "修改了超级槽位,从" + originalStatus + "改为" + supperStatus + Constant.getSubDescription());
                return "true";
            } catch (Exception e) {
                return "false";
            }
        } else {
            return "false";
        }
    }

    /**
     * 修改发布槽位
     *
     * @param request
     * @param pubStatus
     * @param instanceId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String updatePublishStatus(HttpServletRequest request, String pubStatus, long instanceId) {
        Map<String, AppEntity> appMap=appService.getCacheData();
        //获取角色，根据角色判断是否有操作权限
        List<CombinedInstanceDto> combinedInstanceDtos=uiInstanceRepository.findByInstanceId(Long.toString(instanceId));
        int role = uiUserService.getRole(appMap.get(combinedInstanceDtos.get(0).getCandAppId()));
        List<Long> instanceIds = new ArrayList<>();
        instanceIds.add(instanceId);
        //发布槽位，被修改以后的值
        char[] arr = pubStatus.toCharArray();
        arr[0] = (arr[0] == '0' ? '1' : '0');
        if (role == Constant.getSuperUser() || role == Constant.getAppManager()) {
            try {
                AdjustRequest adjustRequest = new AdjustRequest();
                adjustRequest.setIds(instanceIds);
                adjustRequest.setUp(!"1".equals(pubStatus));
                instanceService.adjust(adjustRequest);
                uiAuditLogService.insertLog(request, "instance", instanceId, "修改了发布槽位,从" + pubStatus + "改为" + new String(arr, 0, arr.length) + Constant.getPubDescription());
                return "true";
            } catch (Exception e) {
                return "false";
            }
        } else {
            return "false";
        }
    }

    public List<String>getAllSdkVersion(){
        return uiInstanceRepository.getAllSdkVersion();
    }


}
