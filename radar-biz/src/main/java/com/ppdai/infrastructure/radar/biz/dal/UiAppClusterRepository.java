package com.ppdai.infrastructure.radar.biz.dal;

import com.ppdai.infrastructure.radar.biz.entity.AppClusterEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UiAppClusterRepository {
    public Long appClusterCount(@Param("instanceId") String instanceId);

    public List<AppClusterEntity> findByInstanceId(@Param("instanceId") String instanceId);

    public Long countByAppIdOrClusterName(Map parameterMap);

    public List<AppClusterEntity> findByAppIdOrClusterName(Map parameterMap);
}