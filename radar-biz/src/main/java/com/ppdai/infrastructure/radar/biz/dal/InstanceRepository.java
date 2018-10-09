package com.ppdai.infrastructure.radar.biz.dal;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;

@Mapper
public interface InstanceRepository {
	//对于单个参数，同时是基本类型可以不用@param，否则需要指定@Param
	InstanceEntity findByCandInstanceId(String candInstanceId);
	InstanceEntity findById(Long id);
	void insert(InstanceEntity entity);
	void insertBatch(@Param("instanceLst")List<InstanceEntity> appLst);
	void update(InstanceEntity entity);
	List<InstanceEntity> findAll();
	List<InstanceEntity> findByCandInstanceIds(@Param("candInstanceIds") List<String> candInstanceIds);
	List<InstanceEntity> findByIds(@Param("ids") List<Long> ids);
	
	void heartBeatBatch(@Param("ids") List<Long> ids);
	void updateHeartStatusUp(@Param("ids") List<Long> ids,@Param("expireTime") int expireTime);
	void updateHeartStatusDown(@Param("ids") List<Long> ids,@Param("expireTime") int expireTime);
	void updateHeartStatusDownFoce(@Param("ids") List<Long> ids,@Param("expireTime") int expireTime);
	
	void deleteByIds(@Param("ids") List<Long> ids);
	List<InstanceEntity> findExpired(int findExpired);
	List<InstanceEntity> findOld(int findExpired);	
	List<InstanceEntity> findNoraml(int findExpired);
	void updateInstanceStatus(@Param("ids") List<Long> ids,@Param("status")int status);
	void updatePubStatus(@Param("ids") List<Long> ids,@Param("status")int status);
	void updateSupperStatus(@Param("ids") List<Long> ids,@Param("status")int status);
	Long getCount();
	int updateAppName(@Param("appName") String appName, @Param("id") long id);
	List<InstanceEntity> findByCandAppId(@Param("candAppId") String candAppId);
}
