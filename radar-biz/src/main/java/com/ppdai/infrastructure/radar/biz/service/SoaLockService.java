package com.ppdai.infrastructure.radar.biz.service;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.MetricSingleton;
import com.ppdai.infrastructure.radar.biz.common.util.EmailUtil;
import com.ppdai.infrastructure.radar.biz.common.util.IPUtil;
import com.ppdai.infrastructure.radar.biz.common.util.SpringUtil;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.dal.SoaLockRepository;
import com.ppdai.infrastructure.radar.biz.entity.SoaLockEntity;

//@Service
//支持多master选举
public class SoaLockService {

	private Logger log = LoggerFactory.getLogger(SoaLockService.class);
	private String ip;
	private String key = "soa_clean_sk";
	private volatile boolean flag = false;
	private volatile Object lockObj = new Object();
	private volatile boolean isMaster = false;
	private volatile long id = 0;
	// @Autowired
	private volatile SoaConfig soaConfig;
	private volatile SoaLockRepository soaLockRepository;
	private HeartbeatProperty heartbeatProperty;
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(200), SoaThreadFactory.create("SoaLockService", true),
			new ThreadPoolExecutor.DiscardOldestPolicy());

	private volatile EmailUtil emailUtil;

	public SoaLockService(String key) {
		this.key = key;
		this.heartbeatProperty = new HeartbeatProperty() {
			@Override
			public int getValue() {
				return soaConfig.getSoaLockHeartBeatTime();
			}
		};
	}

	private EmailUtil getEmail() {
		if (emailUtil == null) {
			emailUtil = SpringUtil.getBean(EmailUtil.class);
		}
		return emailUtil;
	}

	public SoaLockService(String key, int soaLockHeartTime) {
		this.key = key;
		this.heartbeatProperty = new HeartbeatProperty() {
			@Override
			public int getValue() {
				if (soaLockHeartTime < 5) {
					return 5;
				}
				return soaLockHeartTime;
			}
		};
	}

	public SoaLockService(String key, HeartbeatProperty heartbeatProperty) {
		this.key = key;
		this.heartbeatProperty = heartbeatProperty;
	}

	// 检查SoaLockRepository和SoaConfig是否注入
	private boolean isLoad() {
		if (soaLockRepository != null && soaConfig != null) {
			return true;
		}
		if (soaConfig == null) {
			soaLockRepository = SpringUtil.getBean(SoaLockRepository.class);
		}
		if (soaConfig == null) {
			soaConfig = SpringUtil.getBean(SoaConfig.class);
		}
		return soaLockRepository != null && soaConfig != null;
	}

	private void start() {
		if (!flag) {
			synchronized (lockObj) {
				if (!flag) {
					flag = true;
					ip = IPUtil.getLocalIP().replaceAll("\\.", "_") + "_" + Util.getProcessId();
					clearAndInit();
					initHeartBeat();
					initMetric();
				}
			}
		}
	}

	private void initMetric() {
		MetricSingleton.getMetricRegistry().register(key + ".Count", new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return isMaster ? 1 : 0;
			}
		});
	}

	private void initHeartBeat() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (isMaster) {
							updateHeatTime();
						}
						// updateHeatTime();
					} catch (Exception e) {
						log.error("doHearBeatError", e);
					}
					Util.sleep(getHeartBeatTime() * 1000);
				}
			}
		});
	}

	private void clearAndInit() {
		// try {
		// clearOld();
		// } catch (Exception e) {
		// // TODO: handle exception
		// }
		try {
			SoaLockEntity entity = soaLockRepository.findByKey1(key);
			if (entity == null) {
				// 保证数据库中有一条记录
				insert();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void clearOld() {
		soaLockRepository.deleteOld(key, getHeartBeatTime() * 2 + 3);
	}

	private void insert() {
		SoaLockEntity entity = new SoaLockEntity();
		entity.setIp(ip);
		entity.setKey1(key);
		soaLockRepository.insert(entity);
	}

	// @Transactional
	public boolean isMaster() {
		try {
			if (!isLoad()) {
				return false;
			}
			start();
			boolean temp = checkMaster();
			if (temp != isMaster) {
				isMaster = temp;
				if (temp) {
					log.info("ip_{}_key_{} 获取到master!", ip, key);
					EmailUtil emailUtil = getEmail();
					if (emailUtil != null) {
						emailUtil.sendWarnMail("锁发生变更" + key, String.format("ip_%s_key_%s 获取到master!", ip, key));
					}
				} else {
					log.info("ip_{}_key_{} 失去master!", ip, key);
					EmailUtil emailUtil = getEmail();
					if (emailUtil != null) {
						emailUtil.sendWarnMail("锁发生变更" + key, String.format("ip_%s_key_%s 失去master!", ip, key));
					}
				}
			}
			isMaster = temp;
			return isMaster;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean checkMaster() {
		SoaLockEntity entity = soaLockRepository.findByKey1(key);
		if (entity == null) {
			clearAndInit();
			entity = soaLockRepository.findByKey1(key);
		}
		// Date dbNow=util.getDbNow();
		Date dbNow = entity.getDbNow();
		id = entity.getId();
		// 注意比较的时候，此时单位是毫秒
		if (entity.getHeartTime().getTime() < dbNow.getTime() - (getHeartBeatTime() * 2 + 3) * 1000) {
			// clearAndInit();
			// entity = soaLockRepository.findByKey1(key);
			// // 根据受影响条数争夺分配锁,单位是秒
			Integer count = soaLockRepository.updateHeartTimeByKey1(ip, key, (getHeartBeatTime() * 2 + 3));
			return count > 0;
			// return checkMaster(entity);
		} else {
			return checkMaster(entity);
		}
	}

	private boolean checkMaster(SoaLockEntity entity) {
		id = entity.getId();
		return entity.getIp().equals(ip);
	}

	// @Transactional
	public void updateHeatTime() {
		if (!isLoad()) {
			return;
		}
		soaLockRepository.updateHeartTimeByIdAndIp(id, ip);
	}

	private int getHeartBeatTime() {
		try {
			return this.heartbeatProperty.getValue();
		} catch (Exception e) {
			return soaConfig.getSoaLockHeartBeatTime();
		}
	}

	public interface HeartbeatProperty {
		int getValue();
	}
}
