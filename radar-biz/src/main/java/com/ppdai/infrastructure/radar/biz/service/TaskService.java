package com.ppdai.infrastructure.radar.biz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ppdai.infrastructure.radar.biz.dal.TaskRepository;
import com.ppdai.infrastructure.radar.biz.entity.TaskEntity;

@Service
public class TaskService {

	@Autowired
	private TaskRepository taskRepository;

//	public long save(TaskEntity entity) {
//		return taskRepository.insert(entity);
//
//	}

	public void addTask(List<TaskEntity> taskEntities) {
		if (!CollectionUtils.isEmpty(taskEntities)) {
//			List<TaskEntity> taskLst = new ArrayList<>(servEntities.size());
//			servEntities.forEach(t1 -> {
//				TaskEntity taskEntity = new TaskEntity();
//				taskEntity.setServId(t1.getId());
//				// taskRepository.insert(taskEntity);
//				taskLst.add(taskEntity);
//			});			
			taskRepository.insertBatch(taskEntities);
		}
	}

	public long getMaxId(long maxId) {
		Long maxId1 = taskRepository.getMaxId(maxId,maxId+500);
		if (maxId1 == null) {
			return 0;
		}
		return maxId1;
	}

	public long getMaxId() {
		Long maxId1 = taskRepository.getMaxId1();
		if (maxId1 == null) {
			return 0;
		}
		return maxId1;
	}

	public long getMinId() {
		Long maxId1 = taskRepository.getMinId();
		if (maxId1 == null) {
			return 0;
		}
		return maxId1;
	}

	public int clearOld(long clearOldTime, long id) {
		return taskRepository.clearOld(clearOldTime, id);
	}

}
