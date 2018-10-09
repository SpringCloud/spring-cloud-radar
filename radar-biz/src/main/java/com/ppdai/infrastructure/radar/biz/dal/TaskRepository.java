package com.ppdai.infrastructure.radar.biz.dal;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import com.ppdai.infrastructure.radar.biz.entity.TaskEntity;

@Mapper
public interface TaskRepository {
	// @Query(value="select max(id) from task where id>?1 order by id asc limit
	// 500",nativeQuery=true)
	Long getMaxId(@Param("maxId1") long maxId1,@Param("maxId2") long maxId2);

	Long getMaxId1();

	long insert(TaskEntity entity);

	void insertBatch(@Param("taskLst") List<TaskEntity> taskLst);

	TaskEntity findById(@Param("id") Long id);

	int insertOrUpdate(TaskEntity entity);

	List<AppEntity> findAll();

	int clearOld(@Param("clearOldTime") Long clearOldTime, @Param("id") long id);

	Long getMinId();
}
