package com.ppdai.infrastructure.radar.biz.service;

import java.util.Date;
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

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.listener.AppReleaseListener;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.dal.AppClusterRepository;
import com.ppdai.infrastructure.radar.biz.entity.AppClusterEntity;

@Service
public class AppClusterService implements AppReleaseListener {

	private Logger log = LoggerFactory.getLogger(AppClusterService.class);
	@Autowired
	private AppClusterRepository appClusterRepository;
	@Autowired
	private SoaConfig soaConfig;
	private static volatile Lock lock = new ReentrantLock(); // 注意这个地方
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(50), SoaThreadFactory.create("AppClusterService", true),new ThreadPoolExecutor.DiscardOldestPolicy());

	private AtomicBoolean startFlag = new AtomicBoolean(false);
	private AtomicBoolean updateFlag = new AtomicBoolean(false);

	public long getClusterCount() {
		Long count = appClusterRepository.getClusterCount();
		if (count == null) {
			return 0;
		}
		return count;
	}

	public String getUq(AppClusterEntity t1) {
		return getUq(t1.getCandAppId(), t1.getClusterName());
	}

	public String getUq(String candAppId, String clusterName) {
		return String.format("%s_%s", candAppId, clusterName);
	}

	public long save(AppClusterEntity t1) {
		String key = getUq(t1);
		if (!getCache().containsKey(key)) {
			lock.lock();
			if (!getCache().containsKey(key)) {
				try {
					appClusterRepository.insert(t1);
					getCache().put(key, t1.getId());
					log.info("serv_cluster_{}_has_insert, id is {}", key, t1.getId());
				} catch (Exception e) {
					try {
						t1 = appClusterRepository.findByUq(t1.getCandAppId(), t1.getClusterName());
						getCache().put(key, t1.getId());
						log.warn("ServClusterEntity 不唯一", e);
					} catch (Exception e1) {
						log.error("appClusterSaveError", e1);
					}

				}
			}
			lock.unlock();
		}
		return getCache().get(key);
	}

	public List<AppClusterEntity> getAll() {
		return appClusterRepository.findAll();
	}

	private AtomicReference<Map<String, Long>> cacheMap = new AtomicReference<>(new ConcurrentHashMap<>(2000));
	private volatile Date lastUpdateData = new Date();
	private volatile Date lastDate = new Date();

	public void startCache() {
		if (startFlag.compareAndSet(false, true)) {
			executor.execute(() -> {
				while (!initCache()) {
					Util.sleep(1000);
				}
				while (true) {
					try {
						updateCache();
					} catch (Exception e) {
						log.error("servclustercache", e);
					}
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
			Date now = appClusterRepository.getMaxUpdateDate();
			if (now != null) {
				if (lastUpdateData.getTime() < now.getTime()) {
					List<AppClusterEntity> data = appClusterRepository.getUpdateData(lastUpdateData);
					data.forEach(t1 -> getCache().put(getUq(t1), t1.getId()));
				}
			} else {
				cacheMap.set(new ConcurrentHashMap<>());
				reInit();
			}
		} catch (Exception e) {
			log.error("servclustercache", e);
		}
	}

	private void reInit() {
		if (System.currentTimeMillis() - soaConfig.getReinitInterval() * 1000 > lastDate.getTime()) {
			initCache();
			lastDate = new Date();
		}
	}

	private boolean initCache() {
		List<AppClusterEntity> data = appClusterRepository.findAll();
		if (CollectionUtils.isEmpty(data)) {
			return false;
		}
		lastUpdateData = appClusterRepository.getMaxUpdateDate();
		Map<String, Long> cacheMap1 = new ConcurrentHashMap<>();
		data.forEach(t1 -> cacheMap1.put(getUq(t1), t1.getId()));
		cacheMap.set(cacheMap1);
		return true;
	}

	public Map<String, Long> getCache() {
		return cacheMap.get();
	}

	@Override
	public void handleService() {
		updateCache();
	}

	@PreDestroy
	private void close() {
		try {
			if (executor != null) {
				executor.shutdown();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void updateAppNames(String appName,String candAppId){
		List<AppClusterEntity> appClusterEntities = appClusterRepository.findByCandAppId(candAppId);
		if(!CollectionUtils.isEmpty(appClusterEntities)){
			for(AppClusterEntity appClusterEntity:appClusterEntities){
				appClusterRepository.updateAppName(appName,appClusterEntity.getId());
			}
		}
	}
}
