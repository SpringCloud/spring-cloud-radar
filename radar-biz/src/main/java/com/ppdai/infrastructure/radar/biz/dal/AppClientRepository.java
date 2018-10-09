package com.ppdai.infrastructure.radar.biz.dal;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ppdai.infrastructure.radar.biz.entity.AppClientEntity;

@Mapper
public interface AppClientRepository {
	List<AppClientEntity> findByConsumerCandAppId(@Param("consumerCandAppId") String consumerCandAppId);	
	int insert(AppClientEntity entity);
}