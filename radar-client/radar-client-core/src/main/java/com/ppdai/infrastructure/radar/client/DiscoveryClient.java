package com.ppdai.infrastructure.radar.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ppdai.infrastructure.radar.biz.common.trace.TraceFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.TraceMessage;
import com.ppdai.infrastructure.radar.biz.common.trace.TraceMessageItem;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.dto.base.AppClusterDto;
import com.ppdai.infrastructure.radar.biz.dto.base.AppDto;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppMetaRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppMetaResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.HeartBeatRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustResponse;
import com.ppdai.infrastructure.radar.client.config.RadarClientConfig;
import com.ppdai.infrastructure.radar.client.dto.RadarApp;
import com.ppdai.infrastructure.radar.client.dto.RadarCluster;
import com.ppdai.infrastructure.radar.client.dto.RadarInstance;
import com.ppdai.infrastructure.radar.client.event.AppChangedEvent;
import com.ppdai.infrastructure.radar.client.event.AppChangedListener;
import com.ppdai.infrastructure.radar.client.exception.RadarException;
import com.ppdai.infrastructure.radar.client.resource.JsonHttpClientImpl;
import com.ppdai.infrastructure.radar.client.resource.RadarResource;
import com.ppdai.infrastructure.radar.client.resource.RadarResourceIml;
import com.ppdai.infrastructure.radar.client.utils.SoaThreadFactory;

/**
 * 注意此类只能实例化一次，实例化多次无效
 */
public class DiscoveryClient {

	private static final Logger logger = LoggerFactory.getLogger(DiscoveryClient.class);
	private TraceMessage traceMessage = TraceFactory.getInstance("DiscoveryClient");

	private Lock lock = new ReentrantLock();

	// 和注册中心服务端交互的客户端
	private RadarResource radarClient;

	// 和注册中心服务端交互的客户端
	private RadarResource radarHeartbeatClient;
	// 当前服务信息
	private RadarInstance instance;
	// 注册中心服务端返回的实例id
	private long id;
	// 注册中心客户端的配置
	private RadarClientConfig config;
	// 心跳和数据刷新的调度器和线程池
	private ExecutorService executor = new ThreadPoolExecutor(3, 3, 3L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
			SoaThreadFactory.create("DiscoveryClient", true), new ThreadPoolExecutor.DiscardOldestPolicy());

	private ScheduledExecutorService heatBeatService = Executors.newScheduledThreadPool(1);
	// 用于控制只启动一次
	private AtomicBoolean isStartUp = new AtomicBoolean(false);
	// 用于控制只设置一次
	private AtomicBoolean isSetConfig = new AtomicBoolean(false);

	// 表示是否初始化完成
	private AtomicBoolean isInited = new AtomicBoolean(false);
	// 用于控制只设置长连接一次
	private AtomicBoolean isPollingUp = new AtomicBoolean(false);

	// 是否订阅
	private AtomicBoolean isSub = new AtomicBoolean(false);
	// 用于控制只设置同步appmeta一次
	private AtomicBoolean isSynAppUp = new AtomicBoolean(false);
	// key为appid， value为版本号
	private Map<String, Long> appVersion = new ConcurrentHashMap<>();
	// key为appid， value为radarapp
	private Map<String, RadarApp> appCache = new ConcurrentHashMap<>();
	// 通知事件列表
	private List<AppChangedListener> changeListeners = new ArrayList<>();

	private DiscoveryClient() {
		pollCacheApp();
	}

	/**
	 * 懒加载单例帮助类
	 */
	private static class SingletonHelper {
		private static final DiscoveryClient INSTANCE = new DiscoveryClient();
	}

	/**
	 * 获取单例
	 */
	public static DiscoveryClient getInstance() {
		return SingletonHelper.INSTANCE;
	}

	/**
	 * 获取注册实例信息，注意只有provider端才能查看当前注册实例信息，如果是consumer则可能为null
	 * 
	 * @return
	 */
	public RadarInstance getInstanceRadar() {
		return this.instance;
	}

	/**
	 * 获取配置
	 * 
	 * @return
	 */
	public RadarClientConfig getConfig() {
		return config;
	}

	/**
	 * 设置配置，注意只能设置一次
	 * 
	 * @return
	 */
	public void setConfig(RadarClientConfig config) {
		if (isSetConfig.compareAndSet(false, true)) {
			if (config != null && config.getCandAppId() != null) {
				// 将appid小写化
				config.setCandAppId(config.getCandAppId().toLowerCase());
			}
			this.config = config;
			this.radarClient = new RadarResourceIml(config.getRegistryUrl(),
					new JsonHttpClientImpl(config.getConnectionTimeout()*1000L, config.getReadTimeout()*1000L));
			this.radarHeartbeatClient = new RadarResourceIml(config.getRegistryUrl(),
					new JsonHttpClientImpl(1500L, 1500L));
			isInited.set(true);
			// 预加热一下
			this.radarClient.hs();
			this.radarHeartbeatClient.hs();

		}
	}

	/**
	 * 是否初始化
	 */
	public boolean isConfiged() {
		return isInited.get();
	}

	/**
	 * 是否已注册
	 */
	public boolean isRegistered() {
		return isStartUp.get();
	}

	/**
	 * 获取当前内存数据
	 */
	public Map<String, RadarApp> getData() {
		return appCache;
	}

	/**
	 * 启动客户端: 注册实例、初始化调度器和线程池、开启心跳
	 */
	public void register(RadarInstance instanceRadar) {
		if (isStartUp.compareAndSet(false, true)) {
			init(instanceRadar);
			// 初始化配置
			initProps();
			boolean flag = false;
			for (int i = 0; i < 10; i++) {
				try {
					flag = doRegister();
					break;
				} catch (Exception e) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
				}
			}
			if (flag) {
				// 开启心跳
				startHeartbeat();
				logger.info("DiscoveryClient started up, config: {}, instance: {}", JsonUtil.toJsonNull(config),
						JsonUtil.toJsonNull(instance));
			} else {
				isStartUp.compareAndSet(true, false);
				throw new RadarException("failed_to_register_instance!");
			}
		}
	}

	/**
	 * 添加实例变更时间
	 */
	public synchronized void addAppChangedListener(AppChangedListener changeListener) {
		if (changeListener != null) {
			subscribeAll();
			changeListeners.add(changeListener);
		}
	}

	/**
	 * 触发实例变更事件 key为appid
	 */
	private void fireEvent(Map<String, RadarApp> changedValue) {
		if (changedValue == null || changedValue.size() == 0) {
			return;
		}
		executor.execute(() -> {
			AppChangedEvent changeEvent = new AppChangedEvent();
			changeEvent.setChangedEvent(changedValue);
			TraceMessageItem item = new TraceMessageItem();
			// item.start();
			item.status = "changed--" + System.currentTimeMillis();
			// item.msg = JsonUtil.toJsonNull(changeEvent);
			item.msg = "changed";
			item.end();
			traceMessage.add(item);
			changeListeners.forEach(t1 -> {
				try {
					t1.onAppChanged(changeEvent);
				} catch (Exception e) {
					logger.error("instance_change_event_error", e);
				} finally {
				}
			});
		});
	}

	private void init(RadarInstance instanceRadar) {
		this.instance = instanceRadar;
	}

	/**
	 * 开始长连接监控
	 */
	private void pollCacheApp() {
		if (isPollingUp.compareAndSet(false, true)) {
			executor.execute(() -> {
				while (true) {
					try {
						if (isConfiged() && appVersion != null && appVersion.size() > 0) {
							doPollingApp();
						} else {
							Thread.sleep(500);
						}
					} catch (Exception e) {
						e.printStackTrace();
						try {
							Thread.sleep(1000);
						} catch (Exception e2) {
							// TODO: handle exception
						}

					}
				}
			});
		}
	}

	/**
	 * 执行长连接监控
	 */
	private void doPollingApp() throws Exception {
		GetAppRequest request = new GetAppRequest();
		request.setAppVersion(appVersion);
		request.setContainOffline(false);
		request.setIp(config.getHost());
		GetAppResponse response = radarClient.getAppPolling(request);
		if (response == null) {
			Thread.sleep(1000);
		} else if (response.getSleepTime() > 0) {
			Thread.sleep(response.getSleepTime());
		} else if (response.getApp() != null) {
			Map<String, RadarApp> rsMap = processQueryResults(response.getApp());
			if (rsMap != null) {
				rsMap.entrySet().forEach(t1 -> {
					appCache.put(t1.getKey(), t1.getValue());
					appVersion.put(t1.getKey(), t1.getValue().getVersion());
				});
				// 触发订阅事件
				fireEvent(rsMap);
			}
		}
	}

	/**
	 * 根据appIds 列表查询实例，如果参数为空则返回空， 否则查询当前对应的实例，注意此方法返回的是注册中心的缓存实例，不是直接从数据库查询，
	 * 可能会存在时间差 。 注意才方法返回结果是一次性的，不会进入监控列表，意味着不会主动更新当前内存信息。
	 * 
	 * @param appIds
	 * @return
	 */
	public Map<String, RadarApp> getApps(List<String> appIds) {
		try {
			if (appIds == null || appIds.size() < 1) {
				return new HashMap<>();
			}
			return queryApps(appIds);
		} catch (Exception ex) {
			throw new RadarException("failed_to_getApps,and appid is " + JsonUtil.toJsonNull(appIds), ex);
		}
	}

	/**
	 * 否则查询当前对应的实例，注意此方法返回的是注册中心的缓存实例，不是直接从数据库查询，可能会存在时间差
	 * 注意才方法返回结果是一次性的，不会进入监控列表，意味着不会主动更新当前内存信息。
	 * 
	 * @return
	 */
	public Map<String, RadarApp> getApps() {
		try {
			return queryApps(null);
		} catch (Exception ex) {
			throw new RadarException("failed_to_getApps_all", ex);
		}
	}

	/**
	 * 查询执行appid实例 否则查询当前对应的实例，注意此方法返回的是当前进程中缓存的实例，如果当前进程缓存中没有数据，
	 * 则尝试从注册中心拿取，拿取成功后，则进入引用监控队列，如果注册中心有更新，会主动更新当前进程中内存数据
	 * 
	 * @return
	 */
	public RadarApp getApp(String appId) {
		if (appId == null || appId.length() == 0 || appId.trim().length() == 0) {
			return null;
		}
		if (!appCache.containsKey(appId)) {
			lock.lock();
			try {
				if (!appCache.containsKey(appId)) {
					try {
						Map<String, RadarApp> appMap = queryApps(Arrays.asList(appId));
						if (appMap != null && appMap.containsKey(appId)) {
							appCache.put(appId, appMap.get(appId));
							appVersion.put(appId, appMap.get(appId).getVersion());
							fireEvent(appMap);
							return appMap.get(appId);
						}
					} catch (Exception e) {

					}
					return null;
				}
			} finally {
				lock.unlock();
			}

		}
		return appCache.get(appId);
	}

	/**
	 * 下线当前实例
	 */
	public void deregister() {
		try {
			if (isStartUp.compareAndSet(true, false)) {
				try {
					doDeregister();
				} catch (Exception e) {
					Thread.sleep(1000);
				}
				if (executor != null) {
					executor.shutdown();
				}

				logger.info("DiscoveryClient shutdowned");
			}
		} catch (Throwable e) {
			logger.error("DiscoveryClient was unable to shutdown, error: " + e.getMessage(), e);
		}
	}

	/**
	 * 初始化版本和语言信息
	 */
	private void initProps() {
	}

	/**
	 * 注册实例
	 */
	private boolean doRegister() throws Exception {
		RegisterInstanceRequest request = new RegisterInstanceRequest();
		request.setClientIp(instance.getHost());
		request.setPort(instance.getPort());
		request.setCandAppId(instance.getCandAppId());
		request.setAppName(instance.getAppName());
		request.setClusterName(instance.getClusterName());
		request.setCandInstanceId(instance.getCandInstanceId());
		request.setTag(instance.getTags());

		try {
			RegisterInstanceResponse response = radarClient.registerInstance(request);
			id = response.getInstanceId();
			if (response.isSuc()) {
				logger.info(
						"DiscoveryClient register instance successfully, instance: " + JsonUtil.toJsonNull(instance));
			} else {
				logger.warn("DiscoveryClient register instance faild, and err msg is : " + response.getMsg());

			}
			return response.isSuc();
		} catch (Throwable e) {
			logger.error("DiscoveryClient was unable to register instance, error: " + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 下线实例
	 */
	private void doDeregister() throws Exception {
		DeRegisterInstanceRequest request = new DeRegisterInstanceRequest();
		request.setInstanceId(id);
		try {
			radarClient.deRegisterInstance(request);
		} catch (Throwable e) {
			logger.error("DiscoveryClient was unable to register instance, error: " + e.getMessage(), e);
			throw e;
		}

		logger.info("DiscoveryClient deregister instance successfully, instance: " + JsonUtil.toJsonNull(instance));
	}

	// private void consumeServices(List<String> appIds) throws Exception {
	// try {
	// RegisterClientRequest request = new RegisterClientRequest();
	// request.setConsumerCandAppId(instance.getCandAppId());
	// request.setConsumerClusterName(instance.getClusterName());
	// request.setProviderCandAppIds(appIds);
	//
	// radarClient.registerClient(request);
	// } catch (Throwable e) {
	// logger.error("DiscoveryClient was unable to register client, error: " +
	// e.getMessage(), e);
	// throw e;
	// }
	// }

	/**
	 * 开始心跳，注意只有provider才会发送心跳
	 */
	private void startHeartbeat() {
		heatBeatService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					logger.debug("client_heartBeat_start");
					doHeartbeat();
					logger.debug("client_heartBeat_end");
				} catch (Throwable t) {
				}
			}
		}, 1, 5, TimeUnit.SECONDS);
	}

	/**
	 * 发送心跳
	 */
	private void doHeartbeat() {
		HeartBeatRequest request = new HeartBeatRequest();
		request.setInstanceId(id);
		for (int i = 0; i < 2; i++) {
			try {
				this.radarHeartbeatClient.heartbeat(request);
				break;
			} catch (Throwable e) {
				logger.warn("DiscoveryClient was unable to send heartbeat_error: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * 发送心跳，返回值key为appid
	 */
	private Map<String, RadarApp> queryApps(List<String> appIds) throws Exception {
		GetAppRequest request = new GetAppRequest();
		Map<String, Long> appVersionMap = new HashMap<>();
		if (appIds != null) {
			for (String appId : appIds) {
				appVersionMap.put(appId, 0L);
			}
		}
		request.setAppVersion(appVersionMap);
		request.setContainOffline(false);
		request.setIp(config.getHost());

		GetAppResponse response = radarClient.getApp(request);
		return processQueryResults(response.getApp());
	}

	/**
	 * 类型转换
	 */
	private Map<String, RadarApp> processQueryResults(Map<String, AppDto> result) {
		if (result == null) {
			return new HashMap<>();
		}
		Map<String, RadarApp> appRadarMap = new HashMap<>();
		// List<RadarAppDto> appRadarList = new ArrayList<>();
		for (Map.Entry<String, AppDto> entry : result.entrySet()) {
			AppDto appDto = entry.getValue();
			// 生成应用、集群和实例的关系
			Map<String, RadarCluster> clusterRadarMap = new HashMap<>();
			if (entry.getValue().getClusters() != null) {
				for (AppClusterDto clusterInfoDto : entry.getValue().getClusters()) {
					List<RadarInstance> instanceRadars = new ArrayList<>();
					if (clusterInfoDto.getInstances() != null) {
						instanceRadars = clusterInfoDto.getInstances().stream()
								.filter(appInstance -> appInstance.isStatus()).map(appInstance -> {
									return RadarInstance.getBuilder()
											.withCandInstanceId(appInstance.getCandInstanceId())
											.withHost(appInstance.getIp()).withPort(appInstance.getPort())
											.withCandAppId(appDto.getAppMeta().getCandAppId())
											.withAppName(appDto.getAppMeta().getName())
											.withClusterName(clusterInfoDto.getAppClusterMeta().getClusterName())
											.withUp(appInstance.isStatus()).withWeight(appInstance.getWeight())
											.withTags(appInstance.getTag()).build();

								}).collect(Collectors.toList());
					}
					clusterRadarMap.put(clusterInfoDto.getAppClusterMeta().getClusterName(),
							RadarCluster.getBuilder()
									.withClusterName(clusterInfoDto.getAppClusterMeta().getClusterName())
									.withDeleteFlag(clusterInfoDto.getAppClusterMeta().getDeleteFlag())
									.withInstances(instanceRadars).build());
				}
			}
			appRadarMap.put(entry.getKey(), RadarApp.getBuilder().withCandAppId(appDto.getAppMeta().getCandAppId())
					.withId(appDto.getAppMeta().getId()).withAppName(appDto.getAppMeta().getName())
					.withVersion(appDto.getAppMeta().getVersion()).withAllowCross(appDto.getAppMeta().getAllowCross())
					.withDomain(appDto.getAppMeta().getDomain()).withClusters(clusterRadarMap).build());
		}

		logger.info("DiscoveryClient current services: {}", JsonUtil.toJsonNull(appRadarMap));
		return appRadarMap;
	}

	/**
	 * 当前实例拉入拉出，true是拉入，false是拉出
	 */
	public AdjustResponse adjust(Boolean isUp) {
		try {
			AdjustRequest adjustRequest = new AdjustRequest();
			adjustRequest.setIds(Arrays.asList(id));
			adjustRequest.setUp(isUp);

			return radarClient.adjust(adjustRequest);
		} catch (Exception e) {
			// e.printStackTrace();
			AdjustResponse adjustResponse = new AdjustResponse();
			adjustResponse.setSuc(false);
			adjustResponse.setMsg(e.getMessage());
			return adjustResponse;
		}
	}

	/**
	 * 对于某些消费需要全量订阅应用，则需要调用此方法，只执行一次，无法取消
	 */
	private void subscribeAll() {
		if (isSub.compareAndSet(false, true)) {
			Transaction transaction = Tracer.newTransaction("radar-client", "subscribeAll");
			try {
				synAppMeta();
				transaction.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				transaction.setStatus(e);
				throw e;
			} finally {
				transaction.complete();
			}
		}
	}

	/**
	 * 执行appMeta同步
	 */
	private void synAppMeta() {
		if (isSynAppUp.compareAndSet(false, true)) {
			executor.execute(() -> {
				while (true) {
					if (isConfiged()) {
						Transaction transaction = Tracer.newTransaction("radar-client", "synAppMeta");
						transaction.setStatus("false");
						try {
							GetAppMetaRequest getAppMetaRequest = new GetAppMetaRequest();
							GetAppMetaResponse response = radarClient.getAppMeta(getAppMetaRequest);
							if (response != null && response.isSuc() && response.getAppMetas() != null) {
								response.getAppMetas().forEach(t1 -> {
									if (!appVersion.containsKey(t1.getCandAppId())) {
										appVersion.put(t1.getCandAppId(), 0L);
									}
								});
								transaction.setStatus(Transaction.SUCCESS);
							}
						} catch (Exception e) {
							transaction.setStatus(e);
							logger.error(e.getMessage(), e);
						} finally {
							transaction.complete();
						}
					} else {
						logger.info("radar config 配置文件还未准备就绪！");
					}
					try {
						Thread.sleep(5000L);
					} catch (InterruptedException e) {
					}
				}
			});
		}
	}
}