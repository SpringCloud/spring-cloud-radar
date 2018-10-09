package com.ppdai.infrastructure.radar.biz.dal;

import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface UiAppRepository {

    public Long appCount(@Param("instanceId") String instanceId);

    public List<AppEntity> findByInstanceId(@Param("instanceId") String instanceId);

    public Long countByAppName(Map parameterMap);

    public List<AppEntity> findByAppName(Map parameterMap);

    public List<AppEntity> findAll();

    public Date getMaxUpdateDate();

    public List<AppEntity> getUpdateData(Date lastDate);

    public int update(AppEntity appEntity);


}