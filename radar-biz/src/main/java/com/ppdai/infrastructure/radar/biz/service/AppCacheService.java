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
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.codahale.metrics.Counter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.listener.AppReleaseListener;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.MetricSingleton;
import com.ppdai.infrastructure.radar.biz.common.trace.TraceFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.TraceMessage;
import com.ppdai.infrastructure.radar.biz.common.trace.TraceMessageItem;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.dal.AppRepository;
import com.ppdai.infrastructure.radar.biz.dto.base.AppClusterDto;
import com.ppdai.infrastructure.radar.biz.dto.base.AppClusterMetaDto;
import com.ppdai.infrastructure.radar.biz.dto.base.AppDto;
import com.ppdai.infrastructure.radar.biz.dto.base.AppMetaDto;
import com.ppdai.infrastructure.radar.biz.dto.base.InstanceDto;
import com.ppdai.infrastructure.radar.biz.entity.AppClusterEntity;
import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;

@Component
public class AppCacheService {
	private Logger log = LoggerFactory.getLogger(AppCacheService.class);
	private volatile long maxId = 0;
	private volatile boolean stop = true;
	@Autowired
	private TaskService taskService;
	@Autowired
	private AppClusterService appClusterService;
	@Autowired
	private InstanceService instanceService;
	@Autowired
	private AppRepository appRepository;
	@Autowired
	private Util util;
	private volatile Date lastDate = new Date();
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(100), SoaThreadFactory.create("AppCacheService", true),
			new ThreadPoolExecutor.DiscardOldestPolicy());

	private List<AppReleaseListener> lstListener = new ArrayList<>();
	// key为appid
	private AtomicReference<Map<String, AppDto>> appRefMap = new AtomicReference<>(new ConcurrentHashMap<>());

	private Counter initServCounter = null;
	private Counter pollingCounter = null;
	@Autowired
	private SoaConfig soaConfig;
	private final Object lockObj = new Object();
	// private volatile long refreshTime = 0;
	private volatile long currentMaxId = 0;
	private volatile boolean isReinit = false;
	private TraceMessage traceMax = TraceFactory.getInstance("AppCacheMaxId");

	// @PostConstruct
	public synchronized void start() {
		if (stop) {
			initServCounter = MetricSingleton.getMetricRegistry().counter("initServ");
			pollingCounter = MetricSingleton.getMetricRegistry().counter("pollingData");
			stop = false;
			maxId = taskService.getMaxId();
			Transaction transaction = Tracer.newTransaction("AppCache", "start");
			try {
				initApp();
				transaction.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				log.error("AppCacheServiceinitServ异常", e);
				transaction.setStatus(e);
				throw e;
			} finally {
				transaction.complete();
			}
			lastDate = new Date();
			executor.execute(() -> {
				doCheckPollingData();
			});
		}
	}

	@PreDestroy
	private void close() {
		try {
			executor.shutdown();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public synchronized void initApp() {
		synchronized (lockObj) {
			Transaction transaction = Tracer.newTransaction("AppCache", "initApp");
			try {
				isReinit = true;
				List<AppEntity> appEntities = appRepository.findAll();
				Map<String, AppDto> servMap = doUpdateCache(appEntities);
				Map<String, AppDto> servMap1 = new ConcurrentHashMap<>(servMap);
				appRefMap.set(servMap1);
				log.info("App 初始化完成！");
				initServCounter.inc();
				isReinit = false;
				transaction.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				transaction.setStatus(e);
			} finally {
				transaction.complete();
			}
		}
	}

	private Map<String, AppDto> converApp(Map<Long, AppVo> appData) {
		Map<String, AppDto> appMap = new HashMap<>();
		Date now = util.getDbNow();
		Map<String, AppDto> tempMap = new HashMap<>();
		if (isReinit) {
			tempMap = appRefMap.get();
		}
		for (AppVo appVo : appData.values()) {
			AppEntity appEntity = appVo.app;
			AppDto appDto = createAppDto(appMap, now, tempMap, appEntity);
			List<AppClusterDto> clusters = new ArrayList<>();
			appDto.setClusters(clusters);
			for (AppClusterVo appClusterVo : appVo.appClusters.values()) {
				AppClusterEntity appClusterEntity = appClusterVo.appCluster;
				AppClusterDto appClusterInfoVo = new AppClusterDto();
				clusters.add(appClusterInfoVo);
				AppClusterMetaDto appClusterInfoMetaDto = createAppCluster(appClusterEntity, appClusterInfoVo);

				List<InstanceDto> instances = new ArrayList<>();
				appClusterInfoVo.setInstances(instances);
				for (InstanceEntity t1 : appClusterVo.instances) {
					InstanceDto instanceDto = new InstanceDto();
					createInstance(now, appEntity, t1, instanceDto);
					instances.add(instanceDto);
				}
				appClusterInfoMetaDto.setDeleteFlag(CollectionUtils.isEmpty(appClusterInfoVo.getInstances()) ? 1 : 0);
			}
		}
		return appMap;
	}

	private AppClusterMetaDto createAppCluster(AppClusterEntity appClusterEntity, AppClusterDto appClusterInfoVo) {
		AppClusterMetaDto appClusterInfoMetaDto = new AppClusterMetaDto();
		appClusterInfoVo.setAppClusterMeta(appClusterInfoMetaDto);
		appClusterInfoMetaDto.setBlackList(appClusterEntity.getBlackList());
		appClusterInfoMetaDto.setWhiteList(appClusterEntity.getWhiteList());
		appClusterInfoMetaDto.setGatewayVisual(appClusterEntity.getGatewayVisual());
		appClusterInfoMetaDto.setId(appClusterEntity.getId());
		appClusterInfoMetaDto.setLimitQps(appClusterEntity.getLimitQps());
		appClusterInfoMetaDto.setClusterName(appClusterEntity.getClusterName());
		appClusterInfoMetaDto.setEnableSelf(appClusterEntity.getEnableSelf());
		return appClusterInfoMetaDto;
	}

	private AppDto createAppDto(Map<String, AppDto> appMap, Date now, Map<String, AppDto> tempMap,
			AppEntity appEntity) {
		AppDto appDto = new AppDto();
		AppMetaDto appMeta = new AppMetaDto();
		appDto.setAppMeta(appMeta);
		appMeta.setId(appEntity.getId());
		appMeta.setName(appEntity.getAppName());
		appMeta.setUpdateTime(appEntity.getUpdateTime());
		appMeta.setCacheTime(now);
		appMeta.setCandAppId(appEntity.getCandAppId());
		appMeta.setDomain(appEntity.getDomain());
		appMeta.setAllowCross(appEntity.getAllowCross());
		if (isReinit) {
			if (tempMap.containsKey(appEntity.getCandAppId())) {
				// 如果是重建模式，则更新时间取上次的
				appMeta.setCacheTime(tempMap.get(appEntity.getCandAppId()).getAppMeta().getCacheTime());
			}
		}
		appMeta.setMsg("currentMaxId:is " + currentMaxId + ",maxid is " + maxId + ",dbNow is " + Util.formateDate(now)
				+ ",and reinit is " + isReinit);
		appMeta.setVersion(appEntity.getVersion());
		appMap.put(appEntity.getCandAppId(), appDto);
		return appDto;
	}

	private void createInstance(Date now, AppEntity appEntity, InstanceEntity t1, InstanceDto instanceDto) {
		instanceDto.setCandInstanceId(t1.getCandInstanceId());
		instanceDto.setId(t1.getId());
		instanceDto.setIp(t1.getIp());
		instanceDto.setPort(t1.getPort());
		instanceDto.setWeight(t1.getWeight());
		instanceDto.setHeartStatus(t1.getHeartStatus());
		instanceDto.setHeartTime(t1.getHeartTime());
		instanceDto.setServName(t1.getServName());
		instanceDto.setPubStatus(t1.getPubStatus());
		instanceDto.setInstanceStatus(t1.getInstanceStatus());
		instanceDto.setSupperStatus(t1.getSupperStatus());
		boolean status = (t1.getHeartStatus() == 1 && t1.getPubStatus() == 1 && t1.getInstanceStatus() == 1
				&& t1.getExtendStatus1() == 1 && t1.getExtendStatus2() == 1);
		if (t1.getSupperStatus() == 1) {
			status = true;
		} else if (t1.getSupperStatus() == -1) {
			status = false;
		}
		instanceDto.setStatus(status);

		if (soaConfig.isFullLog()) {
			String info = String.format(
					"and status is %s,and radar cache date is %s,and dbupdatetime is %s, and version is %s,current_max_id_%s_last_max_id_%s,and reint is %s",
					status, Util.formateDate(now), Util.formateDate(appEntity.getUpdateTime()), appEntity.getVersion(),
					currentMaxId, maxId, isReinit);
			Util.log(log, t1, "is_changed", info);
		}
		instanceDto.setWeight(t1.getWeight());
		instanceDto.setTag(JsonUtil.parseJson(t1.getTag(), new TypeReference<Map<String, String>>() {
		}));
	}

	public synchronized void addListener(AppReleaseListener listener) {
		if (!lstListener.contains(listener)) {
			lstListener.add(listener);
		}
	}

	private void doCheckPollingData() {
		while (!stop) {
			synchronized (lockObj) {
				TraceMessageItem item = new TraceMessageItem();
				Transaction catTransaction = Tracer.newTransaction("DoCheckPollingData", "DoCheckPollingData");
				item.status = "maxId";
				catTransaction.setStatus(Transaction.SUCCESS);
				try {
					if (reInit()) {
						item.status = "reInit";
						item.msg = "maxId is :" + maxId + ",and currentMaxId is " + currentMaxId;
						continue;
					}
					currentMaxId = taskService.getMaxId(maxId);
					if (currentMaxId > 0 && currentMaxId > maxId) {
						updateCache();
					}
					catTransaction.setStatus(Transaction.SUCCESS);
					item.msg = "maxId is :" + maxId + ",and currentMaxId is " + currentMaxId;
				} catch (Exception e) {
					log.error("doCheckPollingData_error,更新异常", e);
					catTransaction.setStatus(e);
				} finally {
					catTransaction.complete();
					traceMax.add(item);
				}
			}
			Util.sleep(soaConfig.getCheckPollingDataInterval());
		}
	}

	private void updateCache() {
		Transaction catTransaction1 = Tracer.newTransaction("DoCheckPollingData", "AppEntity");		
		List<AppEntity> appLst = new ArrayList<>();
		try {
			pollingCounter.inc();
			appLst = appRepository.getLastServ(maxId, currentMaxId);
			catTransaction1.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {

			catTransaction1.setStatus(e);
		} finally {
			catTransaction1.complete();
		}
		// 更新缓存
		updateCacheData(appLst);
		executor.submit(() -> {
			fireListener();
		});
		maxId = currentMaxId;		
	}

	private boolean reInit() throws Exception {
		// 为了保险起见过30秒重新构建一次
		if (System.currentTimeMillis() - soaConfig.getReinitInterval() * 1000 > lastDate.getTime()) {
			currentMaxId = maxId = taskService.getMaxId();
			try {
				initApp();
				executor.execute(() -> {
					fireListener();
				});
				log.info("reInit_Suc,重新初始化数据");
			} catch (Exception e) {
				log.error("reInit_error,异常", e);
				throw e;
			}
			lastDate = new Date();
			// continue;
			return true;
		}
		return false;
	}

	private void updateCacheData(List<AppEntity> servEntities) {
		Map<String, AppDto> appMap = doUpdateCache(servEntities);
		Transaction catTransaction1 = Tracer.newTransaction("DoCheckPollingData", "updateCacheData");
		try {
			// 因为总体数据量不大in操作不一定会比全表快
			// Map<String, AppDto> rsRef = new
			// ConcurrentHashMap<>(appRefMap.get());
			Map<String, AppDto> rsRef = appRefMap.get();
			for (String key : appMap.keySet()) {
				// 防止出现并发时，出现低版本更新高版本的问题
				if (rsRef.containsKey(key)
						&& rsRef.get(key).getAppMeta().getVersion() < appMap.get(key).getAppMeta().getVersion()) {
					rsRef.put(key, appMap.get(key));
				} else if (!rsRef.containsKey(key)) {
					rsRef.put(key, appMap.get(key));
				}
				// appRefMap.set(rsRef);
			}
			catTransaction1.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			catTransaction1.setStatus(e);
			// TODO: handle exception
		} finally {
			catTransaction1.complete();
		}
	}

	private Map<String, AppDto> doUpdateCache(List<AppEntity> appEntities) {
		Transaction catTransaction1 = Tracer.newTransaction("DoCheckPollingData", "ServClusterEntity");
		Map<Long, AppVo> appData = new HashMap<>();
		appEntities.forEach(t1 -> {
			if (!StringUtils.isEmpty(t1.getAppName()))
				appData.put(t1.getId(), new AppVo(t1));
		});
		List<AppClusterEntity> appClusterEntities = appClusterService.getAll();
		for (AppClusterEntity t1 : appClusterEntities) {
			if (appData.containsKey(t1.getAppId()) && !StringUtils.isEmpty(t1.getClusterName())) {
				appData.get(t1.getAppId()).appClusters.put(t1.getId(), new AppClusterVo(t1));
			}
		}
		catTransaction1.setStatus(Transaction.SUCCESS);
		catTransaction1.complete();
		catTransaction1 = Tracer.newTransaction("DoCheckPollingData", "InstanceEntity");
		List<InstanceEntity> instanceEntities = instanceService.getAll();
		// key为cluserId
		for (InstanceEntity t1 : instanceEntities) {
			if (appData.containsKey(t1.getAppId())
					&& appData.get(t1.getAppId()).appClusters.containsKey(t1.getAppClusterId())) {
				appData.get(t1.getAppId()).appClusters.get(t1.getAppClusterId()).instances.add(t1);
			}

		}
		catTransaction1.setStatus(Transaction.SUCCESS);
		catTransaction1.complete();
		catTransaction1 = Tracer.newTransaction("DoCheckPollingData", "converService");
		Map<String, AppDto> appMap = converApp(appData);
		catTransaction1.setStatus(Transaction.SUCCESS);
		catTransaction1.complete();
		return appMap;
	}

	private void fireListener() {
		for (AppReleaseListener listener : lstListener) {
			try {
				listener.handleService();
			} catch (Exception e) {
			}
		}
	}

	public Map<String, AppDto> getApp() {
		return appRefMap.get();
	}

	@PreDestroy
	public void stop() {
		stop = true;
		try {
			executor.shutdown();
		} catch (Exception e) {
		}
	}

	class AppVo {
		public AppEntity app;
		public Map<Long, AppClusterVo> appClusters = new HashMap<>();

		public AppVo(AppEntity app) {
			this.app = app;
		}
	}

	class AppClusterVo {
		public AppClusterEntity appCluster;
		public List<InstanceEntity> instances = new ArrayList<>();

		public AppClusterVo(AppClusterEntity appCluster) {
			this.appCluster = appCluster;
		}
	}

}
