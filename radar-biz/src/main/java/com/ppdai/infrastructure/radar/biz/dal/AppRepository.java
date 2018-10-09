package com.ppdai.infrastructure.radar.biz.dal;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ppdai.infrastructure.radar.biz.entity.AppEntity;

@Mapper
public interface AppRepository {
	//AppEntity findById(Long id);

	int insert(AppEntity entity);

	//void insertBatch(@Param("appLst") List<AppEntity> appLst);

	//int update(AppEntity entity);

	List<AppEntity> findAll();

	void updateVersionByIds(@Param("ids") List<Long> ids);	

	//List<AppEntity> findByNameIn(@Param("names") List<String> names);

	AppEntity findByCandAppId(String candAppId);

	List<AppEntity> getLastServ(@Param("minTaskId") long minTaskId, @Param("maxTaskId") long maxTaskId);

	Date getMaxUpdateDate();

	List<AppEntity> getUpdateData(Date lastDate);
}
