package com.ppdai.infrastructure.radar.biz.dal;

import com.ppdai.infrastructure.radar.biz.dto.ui.CombinedInstanceDto;
import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UiInstanceRepository {

    public Long countBy(Map parameterMap);

    public List<CombinedInstanceDto> findBy(Map parameterMap);

    public Long expiredCount(Map parameterMap);

    public List<CombinedInstanceDto> findExpiredBy(Map parameterMap);

    public Long instanceCount(@Param("instanceId") String instanceId);

    public List<CombinedInstanceDto> findByInstanceId(@Param("instanceId") String instanceId);

    public void updatePubStatus(@Param("pubStatus") String pubStatus, @Param("insId") long id);

    public void updateSupperStatus(@Param("supperStatus") String supperStatus, @Param("insId") long id);

    public AppEntity findOwnerAndMemberById(@Param("instanceId") long instanceId);

    public List<String> getAllSdkVersion();

}