package com.ppdai.infrastructure.radar.biz.common;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.util.Util;

import javax.annotation.PostConstruct;

@Component
public class SoaConfig {

	private static Logger log = LoggerFactory.getLogger(SoaConfig.class);
	@Autowired
	private Environment env;
	private Map<Runnable, Boolean> changed = new ConcurrentHashMap<>();
	private Runnable proMonitor = null;
	private static final String SERVER_PROPERTIES_LINUX = "/opt/settings/server.properties";
	private static final String SERVER_PROPERTIES_WINDOWS = "C:/opt/settings/server.properties";
	private boolean isProFlag = true;
	private String envName = "";
	private String env_getExpiredTime_defaultValue = "";
	private String env_getPollingSize_defaultValue = "";
	private List<Method> methods = new ArrayList<>();
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(100), SoaThreadFactory.create("SoaConfig-scan", true),
			new ThreadPoolExecutor.DiscardPolicy());

	private SoaConfig() {

	}

	@PostConstruct
	private void init() {
		// 初始化环境
		initEnv();
		// 初始化monitor
		initMonitor();
		// 启动monitor
		startMonitor();
		// 初始化参数
		initProperty();

	}

	private void initProperty() {
		env_getExpiredTime_defaultValue = getHeartBeatTime() * 2 + 3 + "";
		env_getPollingSize_defaultValue = getTomcatAcceptCount() - 200 + "";

	}

	private void startMonitor() {
		executor.execute(() -> {
			try {
				while (true) {
					if (proMonitor != null && env != null) {
						proMonitor.run();
					}
					Util.sleep(1000);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		});
	}

	private void initMonitor() {
		Method[] declaredMethods = this.getClass().getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (method.getModifiers() == 1 && method.getParameterCount() == 0) {
				methods.add(method);
			}
		}
		SoaConfig pThis = this;
		proMonitor = new Runnable() {
			@Override
			public void run() {
				methods.forEach(t1 -> {
					try {
						t1.invoke(pThis);
						// System.out.println(t1.getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

			}
		};
	}

	private void onChange() {
		executor.execute(() -> {
			for (Runnable runnable : changed.keySet()) {
				try {
					runnable.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public String getEnvName() {
		return env.getProperty("radar.env", envName);
	}

	public boolean isPro() {
		return isProFlag;
	}

	// 获取当前环境
	private void initEnv() {
		FileInputStream in = null;
		try {
			File file = new File(SERVER_PROPERTIES_LINUX);
			if (!file.exists()) {
				file = new File(SERVER_PROPERTIES_WINDOWS);
			}
			Properties properties = new Properties();
			if (file.canRead()) {
				try {
					in = new FileInputStream(file);
					properties.load(in);
					envName = properties.getProperty("env", "").toLowerCase();
					isProFlag = "pro".equalsIgnoreCase(envName);
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					try {
						if (in != null) {
							in.close();
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}

		} catch (Exception e) {
			log.error("initEnv_SoaConfig_error", e);
		}
	}

	public void registerChanged(Runnable runnable) {
		changed.put(runnable, true);
	}

	private volatile String _pubStatus = "";
	private volatile int pubStatus = 0;

	private final String env_getPubStatus_key = "radar.pub.status";
	private final String env_getPubStatus_defaultValue = "0";
	private final String env_getPubStatus_des = "默认发布槽位初始状态";

	// 默认发布槽位初始状态
	public int getPubStatus() {
		try {
			if (!_pubStatus.equals(env.getProperty(env_getPubStatus_key, env_getPubStatus_defaultValue))) {
				_pubStatus = env.getProperty(env_getPubStatus_key, env_getPubStatus_defaultValue);
				pubStatus = Integer.parseInt(_pubStatus);
				onChange();
			}
		} catch (Exception e) {
			pubStatus = 0;
			onChange();
			log.error("getPubStatus_SoaConfig_error", e);
		}
		return pubStatus;
	}

	private volatile String _heartBeatTime = "";
	private volatile int heartBeatTime = 0;

	private final String env_getHeartBeatTime_key = "radar.heartbeat.time";
	private final String env_getHeartBeatTime_defaultValue = "5";
	private final String env_getHeartBeatTime_des = "客户端默认心跳时间";

	// 客户端默认心跳时间
	public int getHeartBeatTime() {
		try {
			if (!_heartBeatTime.equals(env.getProperty(env_getHeartBeatTime_key, "5"))) {
				_heartBeatTime = env.getProperty(env_getHeartBeatTime_key, "5");
				heartBeatTime = Integer.parseInt(_heartBeatTime);
				onChange();
			}
		} catch (Exception e) {
			heartBeatTime = 5;
			onChange();
			log.error("getHeartBeatTime_SoaConfig_error", e);
		}
		return heartBeatTime;
	}

	private volatile String _expiredTime = "";
	private volatile int expiredTime = 0;

	private final String env_getExpiredTime_key = "radar.expired.time";

	private final String env_getExpiredTime_des = "客户端默认心跳过期时间";

	// 单位是秒
	public int getExpiredTime() {
		try {
			if (!_expiredTime.equals(env.getProperty(env_getExpiredTime_key, env_getExpiredTime_defaultValue))) {
				_expiredTime = env.getProperty(env_getExpiredTime_key, env_getExpiredTime_defaultValue);
				expiredTime = Integer.parseInt(_expiredTime);
				if (expiredTime < getHeartBeatTime() * 2 + 3) {
					expiredTime = getHeartBeatTime() * 2 + 3;
				}
				onChange();
			}
		} catch (Exception e) {
			expiredTime = getHeartBeatTime() * 2 + 3;
			onChange();
			log.error("getExpiredTime_SoaConfig_error", e);
		}
		return expiredTime;
	}

	private volatile String _instanceClearTime = "";
	private volatile int instanceClearTime = 0;

	private final String env_getInstanceClearTime_key = "radar.instance.clear.time";
	private final String env_getInstanceClearTime_defaultValue = 60 * 60 * 6 + "";
	private final String env_getInstanceClearTime_des = "服务超过这么长时间没有心跳自动删除实例";

	// 服务超过这么长时间没有心跳自动删除实例
	public int getInstanceClearTime() {
		try {
			if (!_instanceClearTime
					.equals(env.getProperty(env_getInstanceClearTime_key, env_getInstanceClearTime_defaultValue))) {
				_instanceClearTime = env.getProperty(env_getInstanceClearTime_key,
						env_getInstanceClearTime_defaultValue);
				instanceClearTime = Integer.parseInt(_instanceClearTime);
				if (instanceClearTime < 60 * 60 * 6) {
					instanceClearTime = 60 * 60 * 6;
				}
				onChange();
			}
		} catch (Exception e) {
			instanceClearTime = 60 * 60 * 6;
			onChange();
			log.error("getInstanceClearTime_SoaConfig_error", e);
		}
		return instanceClearTime;
	}

	private volatile String _soaLockHeartBeatTime = "";
	private volatile int soaLockHeartBeatTime = 0;

	private final String env_getSoaLockHeartBeatTime_key = "radar.lock.heartbeat.time";
	private final String env_getSoaLockHeartBeatTime_defaultValue = "5";
	private final String env_getSoaLockHeartBeatTime_des = "锁心跳发送时间间隔，不能超过此值";

	// 锁心跳发送时间间隔，不能超过此值
	public int getSoaLockHeartBeatTime() {
		try {
			if (!_soaLockHeartBeatTime.equals(
					env.getProperty(env_getSoaLockHeartBeatTime_key, env_getSoaLockHeartBeatTime_defaultValue))) {
				_soaLockHeartBeatTime = env.getProperty(env_getSoaLockHeartBeatTime_key,
						env_getSoaLockHeartBeatTime_defaultValue);
				soaLockHeartBeatTime = Integer.parseInt(_soaLockHeartBeatTime);
				if (soaLockHeartBeatTime < 5) {
					soaLockHeartBeatTime = 5;
				}
				onChange();
			}
		} catch (Exception e) {
			soaLockHeartBeatTime = 5;
			onChange();
			log.error("getSoaLockHeartBeatTime_SoaConfig_error", e);
		}
		return soaLockHeartBeatTime;

	}

	private volatile String _reinitInterval = "";
	private volatile int reinitInterval = 0;

	private final String env_getReinitInterval_key = "radar.cache.reinit.interval";
	private final String env_getReinitInterval_defaultValue = "30";
	private final String env_getReinitInterval_des = "定时重构时间,单位秒";

	// 定时重构时间,单位秒
	public int getReinitInterval() {
		try {
			if (!_reinitInterval
					.equals(env.getProperty(env_getReinitInterval_key, env_getReinitInterval_defaultValue))) {
				_reinitInterval = env.getProperty(env_getReinitInterval_key, env_getReinitInterval_defaultValue);
				reinitInterval = Integer.parseInt(_reinitInterval);
				if (reinitInterval < 30) {
					reinitInterval = 30;
				}
				onChange();
			}
		} catch (Exception e) {
			reinitInterval = 30;
			onChange();
			log.error("isEnableClear_SoaConfig_error", e);
		}
		return reinitInterval;
		// return Integer.parseInt(env.getProperty("ReinitInterval", "30"));
	}

	private volatile String _checkPollingDataInterval = "";
	private volatile int checkPollingDataInterval = 0;

	private final String env_getCheckPollingDataInterval_key = "radar.check.polling.data.interval";
	private final String env_getCheckPollingDataInterval_defaultValue = "1000";
	private final String env_getCheckPollingDataInterval_des = "检查轮询数据间隔";

	public int getCheckPollingDataInterval() {
		try {
			if (!_checkPollingDataInterval.equals(env.getProperty(env_getCheckPollingDataInterval_key,
					env_getCheckPollingDataInterval_defaultValue))) {
				_checkPollingDataInterval = env.getProperty(env_getCheckPollingDataInterval_key,
						env_getCheckPollingDataInterval_defaultValue);
				checkPollingDataInterval = Integer.parseInt(_checkPollingDataInterval);
				if (checkPollingDataInterval < 1000) {
					checkPollingDataInterval = 1000;
				}
				onChange();
			}
		} catch (Exception e) {
			checkPollingDataInterval = 1000;
			onChange();
			log.error("getCheckPollingDataInterval_SoaConfig_error", e);
		}
		return checkPollingDataInterval;
	}

	private volatile String _notifyBatchSize = "";
	private volatile int notifyBatchSize = 0;

	private final String env_getNotifyBatchSize_key = "radar.client.notifyBatchSize";
	private final String env_getNotifyBatchSize_defaultValue = "500";
	private final String env_getNotifyBatchSize_des = "批量通知大小";

	// 批量通知大小
	public int getNotifyBatchSize() {
		try {
			if (!_notifyBatchSize
					.equals(env.getProperty(env_getNotifyBatchSize_key, env_getNotifyBatchSize_defaultValue))) {
				_notifyBatchSize = env.getProperty(env_getNotifyBatchSize_key, env_getNotifyBatchSize_defaultValue);
				notifyBatchSize = Integer.parseInt(_notifyBatchSize);
				if (notifyBatchSize < 500) {
					notifyBatchSize = 500;
				}
				onChange();
			}
		} catch (Exception e) {
			notifyBatchSize = 500;
			onChange();
			log.error("getNotifyBatchSize_SoaConfig_error", e);
		}
		return notifyBatchSize;
		// return Integer.parseInt(env.getProperty("NotifyBatchSize", "500"));
	}

	private volatile String _notifyWaitTime = "";
	private volatile int notifyWaitTime = 0;

	private final String env_getNotifyWaitTime_key = "radar.client.notifyWaitTime";
	private final String env_getNotifyWaitTime_defaultValue = "50";
	private final String env_getNotifyWaitTime_des = "当连接数过多时，等待时间";

	// 当连接数过多时，等待时间
	public int getNotifyWaitTime() {
		try {
			if (!_notifyWaitTime
					.equals(env.getProperty(env_getNotifyWaitTime_key, env_getNotifyWaitTime_defaultValue))) {
				_notifyWaitTime = env.getProperty(env_getNotifyWaitTime_key, env_getNotifyWaitTime_defaultValue);
				notifyWaitTime = Integer.parseInt(_notifyWaitTime);
				if (notifyWaitTime < 50) {
					notifyWaitTime = 50;
				}
				onChange();
			}
		} catch (Exception e) {
			notifyWaitTime = 50;
			onChange();
			log.error("getNotifyWaitTime_SoaConfig_error", e);
		}
		return notifyWaitTime;
		// return Integer.parseInt(env.getProperty("NotifyWaitTime", "50"));
	}

	private volatile String _instanceCleanInterval = "";
	private volatile int instanceCleanInterval = 0;

	private final String env_getInstanceCleanInterval_key = "radar.instance.clean.interval";
	private final String env_getInstanceCleanInterval_defaultValue = "2000";
	private final String env_getInstanceCleanInterval_des = "定时清理的时间间隔";

	// 定时清理的时间间隔
	public int getInstanceCleanInterval() {
		try {
			if (!_instanceCleanInterval.equals(
					env.getProperty(env_getInstanceCleanInterval_key, env_getInstanceCleanInterval_defaultValue))) {
				_instanceCleanInterval = env.getProperty("radar.instance.clean.interval", "2000");
				instanceCleanInterval = Integer.parseInt(_instanceCleanInterval);
				if (instanceCleanInterval < 2000) {
					instanceCleanInterval = 2000;
				}
				onChange();
			}
		} catch (Exception e) {
			instanceCleanInterval = 2000;
			onChange();
			log.error("getInstanceCleanInterval_SoaConfig_error", e);
		}
		return instanceCleanInterval;
		// return Integer.parseInt(env.getProperty("InstanceCleanInterval",
		// "2"));
	}

	private volatile String _taskCleanInterval = "";
	private volatile int taskCleanInterval = 0;

	private final String env_getTaskCleanInterval_key = "radar.task.clean.interval";
	private final String env_getTaskCleanInterval_defaultValue = "30";
	private final String env_getTaskCleanInterval_des = "定时清理task的时间间隔，单位分钟";

	// 定时清理task的时间间隔，单位分钟
	public int getTaskCleanInterval() {
		try {
			if (!_taskCleanInterval
					.equals(env.getProperty(env_getTaskCleanInterval_key, env_getTaskCleanInterval_defaultValue))) {
				_taskCleanInterval = env.getProperty(env_getTaskCleanInterval_key,
						env_getTaskCleanInterval_defaultValue);
				taskCleanInterval = Integer.parseInt(_taskCleanInterval);
				if (taskCleanInterval < 30) {
					taskCleanInterval = 30;
				}
				onChange();
			}
		} catch (Exception e) {
			taskCleanInterval = 30;
			onChange();
			log.error("getTaskCleanInterval_SoaConfig_error", e);
		}
		return taskCleanInterval;
		// return Integer.parseInt(env.getProperty("InstanceCleanInterval",
		// "2"));
	}

	private volatile String _pollingSize = "";
	private volatile int pollingSize = 0;

	private final String env_getPollingSize_key = "radar.polling.size";

	private final String env_getPollingSize_des = "最多允许长连接个数";

	// 最多允许长连接个数
	public int getPollingSize() {
		try {
			if (!_pollingSize.equals(env.getProperty(env_getPollingSize_key, env_getPollingSize_defaultValue))) {
				_pollingSize = env.getProperty(env_getPollingSize_key, env_getPollingSize_defaultValue);
				pollingSize = Integer.parseInt(_pollingSize);
				if (pollingSize < 500) {
					pollingSize = 500;
				}
				onChange();
			}
		} catch (Exception e) {
			pollingSize = 500;
			onChange();
			log.error("getPollingSize_SoaConfig_error", e);
		}
		return pollingSize;
		// return Integer.parseInt(env.getProperty("polling.size", "5000"));
	}

	private final String env_getLogPrefix_key = "radar.log.prefix";
	private final String env_getLogPrefix_defaultValue = "instance_{}";
	private final String env_getLogPrefix_des = "日志前缀";

	public String getLogPrefix() {
		return env.getProperty(env_getLogPrefix_key, env_getLogPrefix_defaultValue);
	}

	private volatile String _registerInstanceThreadSize = "";
	private volatile int registerInstanceThreadSize = 0;

	private final String env_getRegisterInstanceThreadSize_key = "radar.register.instance.thread.size";
	private final String env_getRegisterInstanceThreadSize_defaultValue = "5";
	private final String env_getRegisterInstanceThreadSize_des = "批量注册线程数";

	// 批量注册线程数
	public int getRegisterInstanceThreadSize() {
		try {
			if (!_registerInstanceThreadSize.equals(env.getProperty(env_getRegisterInstanceThreadSize_key,
					env_getRegisterInstanceThreadSize_defaultValue))) {
				_registerInstanceThreadSize = env.getProperty(env_getRegisterInstanceThreadSize_key,
						env_getRegisterInstanceThreadSize_defaultValue);
				registerInstanceThreadSize = Integer.parseInt(_registerInstanceThreadSize);
				if (registerInstanceThreadSize < 5) {
					registerInstanceThreadSize = 5;
				}
				onChange();
			}
		} catch (Exception e) {
			registerInstanceThreadSize = 5;
			onChange();
			log.error("getRegisterInstanceThreadSize_SoaConfig_error", e);
		}
		return registerInstanceThreadSize;
		// return Integer.parseInt(env.getProperty("EnableCacheRebuild", "0"));
	}

	private volatile String _registerInstanceSleepTime = "";
	private volatile int registerInstanceSleepTime = 0;

	private final String env_getRegisterInstanceSleepTime_key = "radar.register.instance.sleepTime";
	private final String env_getRegisterInstanceSleepTime_defaultValue = "10";
	private final String env_getRegisterInstanceSleepTime_des = "异步注册间隔时间";

	// 异步注册间隔时间
	public int getRegisterInstanceSleepTime() {
		try {
			if (!_registerInstanceSleepTime.equals(env.getProperty(env_getRegisterInstanceSleepTime_key,
					env_getRegisterInstanceSleepTime_defaultValue))) {
				_registerInstanceSleepTime = env.getProperty(env_getRegisterInstanceSleepTime_key,
						env_getRegisterInstanceSleepTime_defaultValue);
				registerInstanceSleepTime = Integer.parseInt(_registerInstanceSleepTime);
				if (registerInstanceSleepTime < 10) {
					registerInstanceSleepTime = 10;
				}
				onChange();
			}
		} catch (Exception e) {
			registerInstanceSleepTime = 10;
			onChange();
			log.error("getRegisterInstanceThreadSize_SoaConfig_error", e);
		}
		return registerInstanceSleepTime;
		// return Integer.parseInt(env.getProperty("polling.size", "5000"));
	}

	private volatile String _registerClientSleepTime = "";
	private volatile int registerClientSleepTime = 0;

	private final String env_getRegisterClientSleepTime_key = "register.client.sleepTime";
	private final String env_getRegisterClientSleepTime_defaultValue = "1000";
	private final String env_getRegisterClientSleepTime_des = "最多允许长连接个数";

	// 最多允许长连接个数
	public int getRegisterClientSleepTime() {
		try {
			if (!_registerClientSleepTime.equals(
					env.getProperty(env_getRegisterClientSleepTime_key, env_getRegisterClientSleepTime_defaultValue))) {
				_registerClientSleepTime = env.getProperty(env_getRegisterClientSleepTime_key,
						env_getRegisterClientSleepTime_defaultValue);
				registerClientSleepTime = Integer.parseInt(_registerClientSleepTime);
				if (registerClientSleepTime < 800) {
					registerClientSleepTime = 800;
				}
				onChange();
			}
		} catch (Exception e) {
			registerClientSleepTime = 800;
			onChange();
			log.error("getRegisterClientSleepTime_SoaConfig_error", e);
		}
		return registerClientSleepTime;
	}

	private volatile String _heartbeatSleepTime = "";
	private volatile int heartbeatSleepTime = 0;

	private final String env_getHeartbeatSleepTime_key = "radar.exec.heartbeat.interval";
	private final String env_getHeartbeatSleepTime_defaultValue = "2000";
	private final String env_getHeartbeatSleepTime_des = "批量执行心跳间隔时间";

	// 批量执行心跳间隔时间
	public int getHeartbeatSleepTime() {
		try {
			if (!_heartbeatSleepTime
					.equals(env.getProperty(env_getHeartbeatSleepTime_key, env_getHeartbeatSleepTime_defaultValue))) {
				_heartbeatSleepTime = env.getProperty(env_getHeartbeatSleepTime_key,
						env_getHeartbeatSleepTime_defaultValue);
				heartbeatSleepTime = Integer.parseInt(_heartbeatSleepTime);
				if (heartbeatSleepTime > getHeartBeatTime()*1000) {
					heartbeatSleepTime = 2000;
				}
				onChange();
			}
		} catch (Exception e) {
			heartbeatSleepTime = 2000;
			onChange();
			log.error("getHeartbeatSleepTime_SoaConfig_error", e);
		}
		return heartbeatSleepTime;
	}

	private volatile String _registerClientThreadSize = "";
	private volatile int registerClientThreadSize = 0;

	private final String env_getRegisterClientThreadSize_key = "radar.register.client.threadSize";
	private final String env_getRegisterClientThreadSize_defaultValue = "1000";
	private final String env_getRegisterClientThreadSize_des = "最多允许长连接个数";

	// 最多允许长连接个数
	public int getRegisterClientThreadSize() {
		try {
			if (!_registerClientThreadSize.equals(env.getProperty(env_getRegisterClientThreadSize_key,
					env_getRegisterClientThreadSize_defaultValue))) {
				_registerClientThreadSize = env.getProperty(env_getRegisterClientThreadSize_key,
						env_getRegisterClientThreadSize_defaultValue);
				registerClientThreadSize = Integer.parseInt(_registerClientThreadSize);
				if (registerClientThreadSize < 5) {
					registerClientThreadSize = 5;
				}
				onChange();
			}
		} catch (Exception e) {
			registerClientThreadSize = 5;
			onChange();
			log.error("getRegisterClientThreadSize_SoaConfig_error", e);
		}
		return registerClientThreadSize;
	}

	private volatile String _heartbeatBatchSize = "";
	private volatile int heartbeatBatchSize = 0;

	private final String env_getHeartbeatBatchSize_key = "radar.heartbeat.batch.size";
	private final String env_getHeartbeatBatchSize_defaultValue = "20";
	private final String env_getHeartbeatBatchSize_des = "批量执行心跳个数";

	// 批量执行心跳个数
	public int getHeartbeatBatchSize() {
		try {
			if (!_heartbeatBatchSize
					.equals(env.getProperty(env_getHeartbeatBatchSize_key, env_getHeartbeatBatchSize_defaultValue))) {
				_heartbeatBatchSize = env.getProperty(env_getHeartbeatBatchSize_key,
						env_getHeartbeatBatchSize_defaultValue);
				heartbeatBatchSize = Integer.parseInt(_heartbeatBatchSize);
				if (heartbeatBatchSize < 5 && heartBeatTime > 50) {
					heartbeatBatchSize = 20;
				}
				onChange();
			}
		} catch (Exception e) {
			heartbeatBatchSize = 20;
			onChange();
			log.error("getHeartbeatBatchSize_SoaConfig_error", e);
		}
		return heartbeatBatchSize;
	}

	private final String env_isFullLog_key = "radar.log.full";
	private final String env_isFullLog_defaultValue = "0";
	private final String env_isFullLog_des = "开启全日志模式";

	// 开启全日志模式
	public boolean isFullLog() {
		return "1".equals(env.getProperty(env_isFullLog_key, env_isFullLog_defaultValue));
	}

	private final String env_isAsyn_key = "radar.register.instance.asyn";
	private final String env_isAsyn_defaultValue = "1";
	private final String env_isAsyn_des = "开启异步执行模式";

	// 开启异步执行模式
	public boolean isAsyn() {
		return "1".equals(env.getProperty(env_isAsyn_key, env_isAsyn_defaultValue));
	}

	private final String env_enableTest_key = "radar.enableTest";
	private final String env_enableTest_defaultValue = "1";
	private final String env_enableTest_des = "是否允许测试";

	// 是否允许测试
	public boolean enableTest() {
		return "1".equals(env.getProperty(env_enableTest_key, env_enableTest_defaultValue));
	}

	private volatile String soaLogLevel = "";
	private final String env_getSoaLogLevel_key = "radar.log.level";
	private final String env_getSoaLogLevel_defaultValue = "";
	private final String env_getSoaLogLevel_des = "logLevel";

	public String getSoaLogLevel() {
		if (!soaLogLevel.equals(env.getProperty(env_getSoaLogLevel_key, env_getSoaLogLevel_defaultValue))) {
			soaLogLevel = env.getProperty(env_getSoaLogLevel_key, env_getSoaLogLevel_defaultValue);
			onChange();
		}
		return soaLogLevel;
	}

	private volatile String _tomcatAcceptCount = "";
	private volatile int tomcatAcceptCount = 0;

	private final String env_getTomcatAcceptCount_key = "server.tomcat.accept-count";
	private final String env_getTomcatAcceptCount_defaultValue = "1000";
	private final String env_getTomcatAcceptCount_des = "最多允许长连接个数";

	// 最多允许长连接个数
	public int getTomcatAcceptCount() {
		try {
			if (!_tomcatAcceptCount
					.equals(env.getProperty(env_getTomcatAcceptCount_key, env_getTomcatAcceptCount_defaultValue))) {
				_tomcatAcceptCount = env.getProperty(env_getTomcatAcceptCount_key,
						env_getTomcatAcceptCount_defaultValue);
				tomcatAcceptCount = Integer
						.parseInt(env.getProperty(env_getTomcatAcceptCount_key, env_getTomcatAcceptCount_defaultValue));
				if (tomcatAcceptCount < 500) {
					tomcatAcceptCount = 500;
				}
				onChange();
			}
		} catch (Exception e) {
			tomcatAcceptCount = 500;
			onChange();
			log.error("getTomcatAcceptCount_SoaConfig_error", e);
		}
		return tomcatAcceptCount;
	}

	private volatile String _enableSynAtlas = "";
	private volatile boolean enableSynAtlas = false;

	private final String env_getEnableSynAtlas_key = "radar.atlas.syn.enable";
	private final String env_getEnableSynAtlas_defaultValue = "true";
	private final String env_getEnableSynAtlas_des = "是否使用SynAtlas";

	public boolean getEnableSynAtlas() {
		try {
			if (!_enableSynAtlas
					.equals(env.getProperty(env_getEnableSynAtlas_key, env_getEnableSynAtlas_defaultValue))) {
				_enableSynAtlas = env.getProperty(env_getEnableSynAtlas_key, env_getEnableSynAtlas_defaultValue);
				enableSynAtlas = Boolean.parseBoolean(_enableSynAtlas);
			}
		} catch (Exception e) {
			enableSynAtlas = false;
		}
		return enableSynAtlas;
	}

	private volatile String _synTime = "";
	private volatile long synTime = 60 * 1000;

	private final String env_getSynTime_key = "radar.atlas.syn.time";
	private final String env_getSynTime_defaultValue = 60 * 1000 + "";
	private final String env_getSynTime_des = "";

	public long getSynTime() {
		try {
			if (!_synTime.equals(env.getProperty(env_getSynTime_key, env_getSynTime_defaultValue))) {
				_synTime = env.getProperty(env_getSynTime_key, env_getSynTime_defaultValue);
				synTime = Long.parseLong(_synTime);
			}
		} catch (Exception e) {
			synTime = 60 * 1000;
		}
		return synTime;
	}

	private final String env_getEmailHost_key = "email.host";
	private final String env_getEmailHost_defaultValue = "";
	private final String env_getEmailHost_des = "";

	public String getEmailHost() {
		return env.getProperty(env_getEmailHost_key, env_getEmailHost_defaultValue);
	}

	private volatile String _emailPort = "";
	private volatile int emailPort = 0;

	private final String env_getEmailPort_key = "email.port";
	private final String env_getEmailPort_defaultValue = "25";
	private final String env_getEmailPort_des = "";

	public int getEmailPort() {
		try {
			if (!_emailPort.equals(env.getProperty(env_getEmailPort_key, env_getEmailPort_defaultValue))) {
				_emailPort = env.getProperty(env_getEmailPort_key, env_getEmailPort_defaultValue);
				emailPort = Integer.parseInt(_emailPort);
			}
		} catch (Exception e) {
			emailPort = 0;
		}
		return emailPort;
	}

	private final String env_getEmailAuName_key = "email.auName";
	private final String env_getEmailAuName_defaultValue = "";
	private final String env_getEmailAuName_des = "";

	public String getEmailAuName() {
		return env.getProperty(env_getEmailAuName_key, env_getEmailAuName_defaultValue);
	}

	private final String env_getEmailAuPass_key = "email.auPass";
	private final String env_getEmailAuPass_defaultValue = "";
	private final String env_getEmailAuPass_des = "";

	public String getEmailAuPass() {
		return env.getProperty(env_getEmailAuPass_key, env_getEmailAuPass_defaultValue);
	}

	private final String env_getAdminEmail_key = "admin.email";
	private final String env_getAdminEmail_defaultValue = "";
	private final String env_getAdminEmail_des = "";

	public String getAdminEmail() {
		return env.getProperty(env_getAdminEmail_key, env_getAdminEmail_defaultValue);
	}

	private final String env_getAdminName_key = "admin.username";
	private final String env_getAdminName_defaultValue = "";
	private final String env_getAdminName_des = "";

	public String getAdminName() {
		return env.getProperty(env_getAdminName_key, env_getAdminName_defaultValue);
	}

	private final String env_isEmailEnable_key = "email.enable";
	private final String env_isEmailEnable_defaultValue = "false";
	private final String env_isEmailEnable_des = "";

	public boolean isEmailEnable() {
		return "true".equals(env.getProperty(env_isEmailEnable_key, env_isEmailEnable_defaultValue));
	}

	private final String env_getPauthSpringFilterType_keyInner = "pauth.spring.filter.type";
	private final String env_getPauthSpringFilterType_defaultValue = "";
	private final String env_getPauthSpringFilterType_des = "內部版本的配置，开源版本无需配置";

	public String getPauthSpringFilterType() {
		return env.getProperty(env_getPauthSpringFilterType_keyInner, env_getPauthSpringFilterType_defaultValue);
	}

	private final String env_getPauthApiUrl_keyInner = "pauth.api.url";
	private final String env_getPauthApiUrl_defaultValue = "";
	private final String env_getPauthApiUrl_des = "內部版本的配置，开源版本无需配置";

	public String getPauthApiUrl() {
		return env.getProperty(env_getPauthApiUrl_keyInner, env_getPauthApiUrl_defaultValue);
	}

	private final String env_getPauthApiAuthorization_keyInner = "pauth.api.authorization";
	private final String env_getPauthApiAuthorization_defaultValue = "";
	private final String env_getPauthApiAuthorization_des = "內部版本的配置，开源版本无需配置";

	public String getPauthApiAuthorization() {
		return env.getProperty(env_getPauthApiAuthorization_keyInner, env_getPauthApiAuthorization_defaultValue);
	}

	private final String env_getPauthApiRedirectUri_keyInner = "pauth.api.redirectUri";
	private final String env_getPauthApiRedirectUri_defaultValue = "";
	private final String env_getPauthApiRedirectUri_des = "內部版本的配置，开源版本无需配置";

	public String getPauthApiRedirectUri() {
		return env.getProperty(env_getPauthApiRedirectUri_keyInner, env_getPauthApiRedirectUri_defaultValue);
	}

	private final String env_getPauthQuery1_keyInner = "pauth.query1";
	private final String env_getPauthQuery1_defaultValue = "";
	private final String env_getPauthQuery1_des = "內部版本的配置，开源版本无需配置";

	public String getPauthQuery1() {
		return env.getProperty(env_getPauthQuery1_keyInner, env_getPauthQuery1_defaultValue);
	}

	private final String env_getPauthQuery2_keyInner = "pauth.query2";
	private final String env_getPauthQuery2_defaultValue = "";
	private final String env_getPauthQuery2_des = "內部版本的配置，开源版本无需配置";

	public String getPauthQuery2() {
		return env.getProperty(env_getPauthQuery2_keyInner, env_getPauthQuery2_defaultValue);
	}

	private final String env_getRadarUrl_key = "radar.url";
	private final String env_getRadarUrl_defaultValue = "";
	private final String env_getRadarUrl_des = "";

	public String getRadarUrl() {
		return env.getProperty(env_getRadarUrl_key, env_getRadarUrl_defaultValue);
	}

	private final String env_getRdportalUrl_key = "rdportal.url";
	private final String env_getRdportalUrl_defaultValue = "";
	private final String env_getRdportalUrl_des = "";

	public String getRdportalUrl() {
		return env.getProperty(env_getRdportalUrl_key, env_getRdportalUrl_defaultValue);
	}

	private final String env_getAtlasUrl_keyInner = "api.atlas.url";
	private final String env_getAtlasUrl_defaultValue = "";
	private final String env_getAtlasUrl_des = "內部版本的配置，开源版本无需配置";

	public String getAtlasUrl() {
		return env.getProperty(env_getAtlasUrl_keyInner, env_getAtlasUrl_defaultValue);
	}

	private volatile String _maxTestCount = "";
	private volatile int maxTestCount = 60 * 1000;

	private final String env_getMaxTestCount_key = "maxTestCount";
	private final String env_getMaxTestCount_defaultValue = "1000";
	private final String env_getMaxTestCount_des = "";

	public int getMaxTestCount() {
		try {
			if (!_maxTestCount.equals(env.getProperty(env_getMaxTestCount_key, env_getMaxTestCount_defaultValue))) {
				_maxTestCount = env.getProperty(env_getMaxTestCount_key, env_getMaxTestCount_defaultValue);
				maxTestCount = Integer
						.valueOf(env.getProperty(env_getMaxTestCount_key, env_getMaxTestCount_defaultValue));
			}
		} catch (Exception e) {
			maxTestCount = 1000;
		}
		return maxTestCount;
	}

	private final String env_getADServer_key = "ADServer";
	private final String env_getADServer_defaultValue = "";
	private final String env_getADServer_des = "ldap地址";

	public String getADServer() {
		return env.getProperty(env_getADServer_key, env_getADServer_defaultValue);
	}

	private final String env_getSearchBase_key = "SearchBase";
	private final String env_getSearchBase_defaultValue = "";
	private final String env_getSearchBase_des = "ldap域控搜索路径 以 '|' 隔开";

	public String getSearchBase() {
		return env.getProperty(env_getSearchBase_key, env_getSearchBase_defaultValue);
	}

	private final String env_getRadarLdapUser_key = "radar.ldapUser";
	private final String env_getRadarLdapUser_defaultValue = "";
	private final String env_getRadarLdapUser_des = "访问ldap的用户名";

	public String getRadarLdapUser() {
		return env.getProperty(env_getRadarLdapUser_key, env_getRadarLdapUser_defaultValue);
	}

	private final String env_getRadarLdapPass_key = "radar.ldapPass";
	private final String env_getRadarLdapPass_defaultValue = "";
	private final String env_getRadarLdapPass_des = "访问ldap的密码";

	public String getRadarLdapPass() {
		return env.getProperty(env_getRadarLdapPass_key, env_getRadarLdapPass_defaultValue);
	}

	private final String env_getRadarAdminUser_key = "radar.adminUser";
	private final String env_getRadarAdminUser_defaultValue = "radar";
	private final String env_getRadarAdminUser_des = "默认管理员账号";

	public String getRadarAdminUser() {
		return env.getProperty(env_getRadarAdminUser_key, env_getRadarAdminUser_defaultValue);
	}

	private final String env_getRadarAdminPass_key = "radar.adminPass";
	private final String env_getRadarAdminPass_defaultValue = "admin";
	private final String env_getRadarAdminPass_des = "默认管理员密码";

	public String getRadarAdminPass() {
		return env.getProperty(env_getRadarAdminPass_key, env_getRadarAdminPass_defaultValue);
	}

	private final String env_getAdminUsers_key = "radar.admin.username";
	private final String env_getAdminUsers_defaultValue = "";
	private final String env_getAdminUsers_des = "管理员";

	public String getAdminUsers() {
		return env.getProperty(env_getAdminUsers_key, env_getAdminUsers_defaultValue);
	}

	private volatile String _heartBeatThreadSize = "5";
	private volatile int heartBeatThreadSize = 5;

	private final String env_getHeartBeatThreadSize_key = "radar.heartbeat.thread.size";
	private final String env_getHeartBeatThreadSize_defaultValue = "5";
	private final String env_getHeartBeatThreadSize_des = "批量执行心跳线程数";

	public int getHeartBeatThreadSize() {
		try {
			if (!_heartBeatThreadSize
					.equals(env.getProperty(env_getHeartBeatThreadSize_key, env_getHeartBeatThreadSize_defaultValue))) {
				_heartBeatThreadSize = env.getProperty(env_getHeartBeatThreadSize_key,
						env_getHeartBeatThreadSize_defaultValue);
				heartBeatThreadSize = Integer.parseInt(_heartBeatThreadSize);
				onChange();
			}
		} catch (Exception e) {
			heartBeatThreadSize = 5;
			onChange();
		}
		return heartBeatThreadSize;
	}

	private volatile String _doubleCheck = "true";
	private volatile boolean doubleCheck = true;

	private final String env_getDoubleCheck_key = "radar.heartbeat.doubleCheck";
	private final String env_getDoubleCheck_defaultValue = "true";
	private final String env_getDoubleCheck_des = "是否开启doublecheck";

	public boolean getDoubleCheck() {
		try {
			if (!_doubleCheck.equals(env.getProperty(env_getDoubleCheck_key, env_getDoubleCheck_defaultValue))) {
				_doubleCheck = env.getProperty(env_getDoubleCheck_key, env_getDoubleCheck_defaultValue);
				doubleCheck = "true".equals(_doubleCheck);
				onChange();
			}
		} catch (Exception e) {
			doubleCheck = true;
			onChange();
		}
		return doubleCheck;
	}
	
	private volatile String _heartBeatAsyn = "true";
	private volatile boolean heartBeatAsyn = true;

	private final String env_getHeartBeatAsyn_key = "radar.heartbeat.asyn";
	private final String env_getHeartBeatAsyn_defaultValue = "true";
	private final String env_getHeartBeatAsyn_des = "是否异步发心跳";

	public boolean getHeartBeatAsyn() {
		try {
			if (!_heartBeatAsyn.equals(env.getProperty(env_getHeartBeatAsyn_key, env_getHeartBeatAsyn_defaultValue))) {
				_heartBeatAsyn = env.getProperty(env_getHeartBeatAsyn_key, env_getHeartBeatAsyn_defaultValue);
				heartBeatAsyn = "true".equals(_heartBeatAsyn);
				onChange();
			}
		} catch (Exception e) {
			heartBeatAsyn = true;
			onChange();
		}
		return heartBeatAsyn;
	}
}
