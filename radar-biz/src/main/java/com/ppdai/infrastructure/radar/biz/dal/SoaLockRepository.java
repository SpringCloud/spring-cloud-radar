package com.ppdai.infrastructure.radar.biz.dal;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ppdai.infrastructure.radar.biz.entity.SoaLockEntity;

@Mapper
public interface SoaLockRepository {
	int insert(SoaLockEntity entity);

	SoaLockEntity findByKey1(String key);

	int updateHeartTimeByKey1(@Param("ip") String ip, @Param("key1") String key1,@Param("lockInterval") int lockInterval);
	
	int updateHeartTimeByIdAndIp(@Param("id") long id,@Param("ip") String ip);

	Date getDbNow();

	int deleteOld(@Param("key1") String key1,@Param("lockInterval") int lockInterval);

	Long countBy(Map parameterMap);

	List<SoaLockEntity> findBy(Map parameterMap);
}
