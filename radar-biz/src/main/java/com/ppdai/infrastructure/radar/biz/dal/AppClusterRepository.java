package com.ppdai.infrastructure.radar.biz.dal;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ppdai.infrastructure.radar.biz.entity.AppClusterEntity;

@Mapper
public interface AppClusterRepository {

	AppClusterEntity findByUq(@Param("candAppId") String candAppId,@Param("clusterName") String clusterName );

	int insert(AppClusterEntity entity);
	List<AppClusterEntity> findAll();

	Long getClusterCount();

	Date getMaxUpdateDate();

	List<AppClusterEntity> getUpdateData(Date lastDate);

	int updateAppName(@Param("appName") String appName, @Param("id") long id);

	List<AppClusterEntity> findByCandAppId(@Param("candAppId")String candAppId);
}