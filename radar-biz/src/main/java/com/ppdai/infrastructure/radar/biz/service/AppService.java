package com.ppdai.infrastructure.radar.biz.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.listener.AppReleaseListener;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.dal.AppRepository;
import com.ppdai.infrastructure.radar.biz.dto.base.AppClusterDto;
import com.ppdai.infrastructure.radar.biz.dto.base.AppDto;
import com.ppdai.infrastructure.radar.biz.dto.base.AppMetaDto;
import com.ppdai.infrastructure.radar.biz.dto.base.InstanceDto;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppMetaRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppMetaResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppResponse;
import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import com.ppdai.infrastructure.radar.biz.entity.TaskEntity;

@Service
public class AppService implements AppReleaseListener {

	private Logger log = LoggerFactory.getLogger(AppService.class);
	@Autowired
	private AppRepository appRepository;
	@Autowired
	private AppCacheService appCacheService;
	private static volatile Lock lock = new ReentrantLock(); // 注意这个地方
	@Autowired
	private TaskService taskService;
	@Autowired
	private SoaConfig soaConfig;
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(50), SoaThreadFactory.create("AppService", true),
			new ThreadPoolExecutor.DiscardOldestPolicy());

	private AtomicBoolean startFlag = new AtomicBoolean(false);
	private AtomicBoolean updateFlag = new AtomicBoolean(false);

	public long getCount() {
		return getCacheData().size();
	}

	public void updateVersionByIds(List<Long> ids) {
		if (ids.size() > 0) {
			appRepository.updateVersionByIds(ids);
		}
		List<TaskEntity> taskLst = new ArrayList<>();
		ids.forEach(t1 -> {
			log.info("app_{}_version_updated", t1);
			TaskEntity taskEntity = new TaskEntity();
			taskEntity.setAppId(t1);
			taskLst.add(taskEntity);
		});
		taskService.addTask(taskLst);
	}

	public AppEntity save(AppEntity t1) {
		if (!getCacheData().containsKey(t1.getCandAppId())) {
			lock.lock();
			try {
				if (!getCacheData().containsKey(t1.getCandAppId())) {
					appRepository.insert(t1);
					getCacheData().put(t1.getCandAppId(), t1);
					log.info("app_{}_has_insert", t1.getCandAppId());
				}
			} catch (Exception e) {
				try {
					t1 = appRepository.findByCandAppId(t1.getCandAppId());
					getCacheData().put(t1.getCandAppId(), t1);
					log.warn("appEntity 不唯一", e);
				} catch (Exception e1) {
					log.error("appSaveError", e1);
				}
			}
			lock.unlock();
		}
		return getCacheData().get(t1.getCandAppId());
	}

	public GetAppResponse getApp(GetAppRequest request) {
		GetAppResponse response = new GetAppResponse();
		response.setSuc(true);
		Map<String, AppDto> rs = new HashMap<>();
		Map<String, AppDto> data = new HashMap<>();
		Map<String, AppDto> cache = appCacheService.getApp();
		// log.info("当前参数为:{},当前缓存数据为：[{}],结束,", JsonUtil.toJsonNull(request),
		// JsonUtil.toJsonNull(cache));
		if (request != null && request.getAppVersion() != null && request.getAppVersion().size() > 0) {
			for (String name : request.getAppVersion().keySet()) {
				if (cache.containsKey(name)
						&& cache.get(name).getAppMeta().getVersion() > request.getAppVersion().get(name)) {
					data.put(name, cache.get(name));
				}
			}
		} else {
			data = cache;
		}
		for (Map.Entry<String, AppDto> entry : data.entrySet()) {
			AppDto appDto = new AppDto();
			appDto.setAppMeta(entry.getValue().getAppMeta());
			rs.put(entry.getKey(), appDto);
			if (!CollectionUtils.isEmpty(entry.getValue().getClusters())) {
				List<AppClusterDto> appClusterInfoDtos = new ArrayList<>(entry.getValue().getClusters().size());
				appDto.setClusters(appClusterInfoDtos);
				for (AppClusterDto appClusterInfoDto1 : entry.getValue().getClusters()) {
					if (!blackWhiteListCheck(request, appClusterInfoDto1)) {
						continue;
					}
					AppClusterDto appClusterInfoDto = new AppClusterDto();
					appClusterInfoDto.setAppClusterMeta(appClusterInfoDto1.getAppClusterMeta());
					if (request.isContainOffline()) {
						appClusterInfoDto.setInstances(appClusterInfoDto1.getInstances());
					} else {
						appClusterInfoDto.setInstances(filterOnlyOnline(appClusterInfoDto1));
					}
					appClusterInfoDtos.add(appClusterInfoDto);
				}
			}
		}
		response.setApp(rs);
		return response;
	}

	private List<InstanceDto> filterOnlyOnline(AppClusterDto appClusterInfoDto1) {
		if (!CollectionUtils.isEmpty(appClusterInfoDto1.getInstances())) {
			List<InstanceDto> instances = new ArrayList<>(appClusterInfoDto1.getInstances().size());
			for (InstanceDto temp : appClusterInfoDto1.getInstances()) {
				if (temp.isStatus()) {
					instances.add(temp);
				}
			}
			return instances;
		}
		return null;
	}

	private boolean blackWhiteListCheck(GetAppRequest request, AppClusterDto appClusterInfoDto1) {
		String ip = request.getIp();
		if (StringUtils.isEmpty(ip)) {
			return true;
		}
		if (inBlackListCheck(ip, appClusterInfoDto1.getAppClusterMeta().getBlackList())) {
			return false;
		}
		return inWhiteListCheck(ip, appClusterInfoDto1.getAppClusterMeta().getWhiteList());
	}

	private boolean inBlackListCheck(String ip, String blackList) {
		if (StringUtils.isEmpty(blackList)) {
			return false;
		}
		return (blackList + "").contains(ip + "");
	}

	private boolean inWhiteListCheck(String ip, String whiteList) {
		if (StringUtils.isEmpty(whiteList)) {
			return true;
		}
		return (whiteList + "").contains(ip + "");
	}

	private AtomicReference<Map<String, AppEntity>> cacheDataMap = new AtomicReference<>(new ConcurrentHashMap<>(2000));
	private AtomicReference<Map<String, String>> appNameIdCacheMap = new AtomicReference<>(new ConcurrentHashMap<>(2000));
	private AtomicReference<Map<String, String>> appIdNameCacheMap = new AtomicReference<>(new ConcurrentHashMap<>(2000));
	private volatile Date lastUpdateData = new Date();
	private volatile Date lastDate = new Date();

	public void startCache() {
		if (startFlag.compareAndSet(false, true)) {
			executor.execute(() -> {
				while (!initCache()) {
					Util.sleep(1000);
				}
				while (true) {
					updateCache();
					Util.sleep(1000);
				}
			});
		}
	}

	private void updateCache() {
		if (updateFlag.compareAndSet(false, true)) {
			doUpdateCache();
			updateFlag.set(false);
		}
	}

	private void doUpdateCache() {
		try {
			reInit();
			Date now = appRepository.getMaxUpdateDate();
			if (now != null) {
				if (lastUpdateData.getTime() < now.getTime()) {
					Map<String, AppEntity> dataMap = cacheDataMap.get();
					Map<String, String> appNameId = appNameIdCacheMap.get();
					Map<String, String> appIdName = appNameIdCacheMap.get();
					List<AppEntity> data = appRepository.getUpdateData(lastUpdateData);
					data.forEach(t1 -> {
						if (!StringUtils.isEmpty(t1.getCandAppId())) {
							dataMap.put(t1.getCandAppId(), t1);
							if (!StringUtils.isEmpty(t1.getAppName())) {
								appNameId.put(t1.getAppName(), t1.getCandAppId());
								appIdName.put(t1.getCandAppId(),t1.getAppName());
							}
						}
					});
				}
			} else {
				cacheDataMap.set(new ConcurrentHashMap<>());
				reInit();
			}
		} catch (Exception e) {
			log.error("servcache", e);
		}
	}

	private void reInit() {
		if (System.currentTimeMillis() - soaConfig.getReinitInterval() * 1000 > lastDate.getTime()) {
			initCache();
			lastDate = new Date();
		}
	}

	private boolean initCache() {
		List<AppEntity> data = appRepository.findAll();
		if (CollectionUtils.isEmpty(data)) {
			// log.info("servInitCache is null");
			return false;
		}
		lastUpdateData = appRepository.getMaxUpdateDate();
		// Map<String, Long> cacheMap1 = new ConcurrentHashMap<>();
		Map<String, AppEntity> cacheMap2 = new ConcurrentHashMap<>();
		Map<String, String> cacheMap3 = new ConcurrentHashMap<>();
		Map<String, String> cacheMap4 = new ConcurrentHashMap<>();
		data.forEach(t1 -> {
			if (!StringUtils.isEmpty(t1.getCandAppId())) {
				// cacheMap1.put(t1.getCandAppId(), t1.getId());
				cacheMap2.put(t1.getCandAppId(), t1);
				if (!StringUtils.isEmpty(t1.getAppName())) {					
					cacheMap3.put(t1.getAppName(), t1.getCandAppId());
					cacheMap4.put(t1.getCandAppId(),t1.getAppName());
				}
			}

		});
		appNameIdCacheMap.set(cacheMap3);
		appIdNameCacheMap.set(cacheMap4);
		cacheDataMap.set(cacheMap2);
		return true;
	}

	public Map<String, AppEntity> getCacheData() {
		return cacheDataMap.get();
	}
	public Map<String, String> getAppNameIdCacheData() {
		return appNameIdCacheMap.get();
	}
	
	public Map<String, String> getAppIdNameCacheData() {
		return appIdNameCacheMap.get();
	}
	@Override
	public void handleService() {
		updateCache();
	}

	public GetAppMetaResponse getAppMeta(GetAppMetaRequest request) {
		Map<String, AppDto> appMap = appCacheService.getApp();
		List<AppMetaDto> rs = new ArrayList<>();
		appMap.values().forEach(t1 -> rs.add(t1.getAppMeta()));
		GetAppMetaResponse getAppMetaResponse = new GetAppMetaResponse();
		getAppMetaResponse.setSuc(true);
		getAppMetaResponse.setAppMetas(rs);
		return getAppMetaResponse;
	}

	@PreDestroy
	private void close() {
		try {
			executor.shutdown();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public AppEntity findByCandAppId(String candAppId) {
		AppEntity appEntity = null;
		appEntity = appRepository.findByCandAppId(candAppId);
		return appEntity;
	}

}
