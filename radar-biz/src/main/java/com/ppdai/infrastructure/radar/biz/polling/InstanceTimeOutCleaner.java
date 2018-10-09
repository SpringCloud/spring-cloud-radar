package com.ppdai.infrastructure.radar.biz.polling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.EmailUtil;
import com.ppdai.infrastructure.radar.biz.common.util.HttpClient;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;
import com.ppdai.infrastructure.radar.biz.service.AppService;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;
import com.ppdai.infrastructure.radar.biz.service.SoaLockService;
import com.ppdai.infrastructure.radar.biz.service.TaskService;

@Component
public class InstanceTimeOutCleaner {
	private Logger log = LoggerFactory.getLogger(InstanceTimeOutCleaner.class);
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(10), SoaThreadFactory.create("InstanceTimeOutCleaner", true),
			new ThreadPoolExecutor.DiscardOldestPolicy());

	private volatile boolean isRunning = false;
	private volatile boolean isMaster = false;
	@Autowired
	private InstanceService instanceService;
	// @Autowired
	private SoaLockService soaLockService = new SoaLockService("soa_clean_sk");
	@Autowired
	private SoaConfig soaConfig;
	@Autowired
	private TaskService taskService;
	@Autowired
	private AppService appService;
	@Autowired
	private Util util;

	@Autowired
	private EmailUtil emailUtil;

	private Date lastDate = new Date();
	private static Object lockObj = new Object();
	private HttpClient client = new HttpClient(3, 3);

	public void start() {
		if (!isRunning) {
			synchronized (lockObj) {
				isRunning = true;
				executor.execute(new Runnable() {
					@Override
					public void run() {
						doCheckHeartTime();
					}
				});
				executor.execute(new Runnable() {
					@Override
					public void run() {
						clearOldTask();
					}
				});
				executor.execute(new Runnable() {
					@Override
					public void run() {
						clearOldInstance();
					}
				});
			}
		}
	}

	private void clearOldInstance() {
		while (isRunning) {
			try {
				if (isMaster) {
					// 删除过期的Instance数据
					clearOldInstanceData();
				}
			} catch (Exception e) {
				log.error("clearOldTaskError", e);
			}
			Util.sleep(soaConfig.getSoaLockHeartBeatTime() * 1000);
		}

	}

	private void clearOldTask() {
		while (isRunning) {
			try {
				if (isMaster) {
					// 删除过期的task数据
					clearOldTaskData();
				}
			} catch (Exception e) {
				log.error("clearOldTaskError", e);
			}
			Util.sleep(soaConfig.getSoaLockHeartBeatTime() * 1000);
		}

	}

	protected void doCheckHeartTime() {
		while (isRunning) {
			try {
				// clearOldTaskData();
				if (soaLockService.isMaster()) {
					isMaster = true;
					Transaction transaction = Tracer.newTransaction("heartbeat", "check");
					try {
						Transaction transaction1 = Tracer.newTransaction("heartbeat", "checkExpiredHeartTime");
						try {
							// 检查心跳过期的，但是心跳状态不匹配的
							checkExpiredHeartTime();
							transaction1.setStatus(Transaction.SUCCESS);
						} catch (Exception e) {
							transaction1.setStatus(e);
						}
						transaction1.complete();

						Transaction transaction2 = Tracer.newTransaction("heartbeat", "checkNormalHeartTime");
						try {
							// 检查正常的，但是心跳状态不匹配的
							checkNormalHeartTime();
							transaction2.setStatus(Transaction.SUCCESS);
						} catch (Exception e) {
							transaction2.setStatus(e);
						}
						transaction2.complete();
						transaction.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						transaction.setStatus(e);
					}
					transaction.complete();

				} else {
					isMaster = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("InstanceTimeOutCleanerfail", e);
			}

			Util.sleep(soaConfig.getInstanceCleanInterval());

		}
	}

	private void clearOldTaskData() {
		Date now = new Date();
		// 定时清除数据
		if (now.getTime() - lastDate.getTime() > 1000 * 60 * soaConfig.getTaskCleanInterval()) {
			lastDate = now;
			long minId = taskService.getMinId();
			if (minId > 0) {
				log.info("clear_old_data_minId_is_{}_and_maxId_is_{}", minId, minId + 500);
				int count = taskService.clearOld(60 * soaConfig.getTaskCleanInterval(), minId + 500);
				while (count > 0) {
					minId = taskService.getMinId();
					log.info("clear_old_data_minId_is_{}_and_maxId_is_{}", minId, minId + 500);
					count = taskService.clearOld(60 * soaConfig.getTaskCleanInterval(), minId + 500);
					Util.sleep(300);
				}
			}
		}
	}

	private void clearOldInstanceData() {
		List<InstanceEntity> expireLst1 = instanceService.findOld(soaConfig.getInstanceClearTime());
		if (expireLst1.size() == 0) {
			return;
		}
		List<InstanceEntity> expireLst = new ArrayList<>();
		String dbNow = Util.formateDate(util.getDbNow());
		expireLst1.forEach(t1 -> {
			if (!doubleCheck(t1, dbNow, 1)) {
				String content = String.format("心跳时间超过过期时间，被删除,json为:%s,and DbTime is %s", JsonUtil.toJsonNull(t1),
						dbNow);
				Util.log(log, t1, "timeout_delete_old", content);
				emailUtil.sendWarnMail(
						"clearOldInstance,appId:" + t1.getCandAppId() + ",ip:" + t1.getIp() + "长时间为发送心跳，即将删除", content,
						getMail(t1.getCandAppId()));
				expireLst.add(t1);
			}
		});

		List<List<InstanceEntity>> rs = Util.split(expireLst, 20);
		rs.forEach(t1 -> {
			try {
				instanceService.deleteInstance(t1);
			} catch (Exception e) {
				// TODO: handle exception
			}
		});
	}

	protected void checkNormalHeartTime() {
		List<InstanceEntity> normalEntity = instanceService.findNoraml(soaConfig.getExpiredTime());
		if (!CollectionUtils.isEmpty(normalEntity)) {
			String dbNow = Util.formateDate(util.getDbNow());
			normalEntity.forEach(t1 -> {
				String content = String.format("心跳正常，心跳状态变更为1,json为:%s,and DbTime is %s", JsonUtil.toJsonNull(t1),
						dbNow);
				Util.log(log, t1, "heartBeatNormal", content);				
				emailUtil.sendWarnMail(
							"checkHeartTime,appId:" + t1.getCandAppId() + ",ip:" + t1.getIp() + "心跳正常，服务可用", content,
							getMail(t1.getCandAppId()));
				
			});
			if (soaLockService.isMaster()) {
				log.info("NormalHeartTime开始更新");
				List<List<InstanceEntity>> rs = Util.split(normalEntity, 50);
				rs.forEach(t1 -> {
					instanceService.updateHeartStatus(t1, true, soaConfig.getExpiredTime());
				});
			}
		}
	}

	private String getMail(String canAppId) {
		if (StringUtils.isEmpty(canAppId)) {
			return "";
		}
		Map<String, AppEntity> appCache = appService.getCacheData();
		if (appCache.containsKey(canAppId)) {
			if (appCache.get(canAppId).getAlarm() == 1) {
				return appCache.get(canAppId).getOwnerEmail();
			}
		}
		return "";
	}

	protected void checkExpiredHeartTime() {
		List<InstanceEntity> expireEntity = instanceService.findExpired(soaConfig.getExpiredTime());
		List<InstanceEntity> expireDCheckEntity = new ArrayList<>(expireEntity.size());
		if (!CollectionUtils.isEmpty(expireEntity)) {
			String dbNow = Util.formateDate(util.getDbNow());
			expireEntity.forEach(t1 -> {
				if (!doubleCheck(t1, dbNow, 0)) {
					String content = String.format("超时，心跳状态变更为0,json为:%s,and DbTime is %s", JsonUtil.toJsonNull(t1),
							dbNow);
					Util.log(log, t1, "heartBeatTimeOut", content);					
					emailUtil.sendWarnMail(
								"checkHeartTime,appId:" + t1.getCandAppId() + ",ip:" + t1.getIp() + "心跳超时，服务可能不可用",
								content, getMail(t1.getCandAppId()));					
					expireDCheckEntity.add(t1);
				}
			});
			if (soaLockService.isMaster()) {
				List<List<InstanceEntity>> rs = Util.split(expireDCheckEntity, 50);
				// log.info("HeartTime开始更新");
				rs.forEach(t1 -> {
					instanceService.updateHeartStatus(t1, false, soaConfig.getExpiredTime());
				});

			}
		}
	}

	private boolean doubleCheck(InstanceEntity t1, String dbNow, int type) {
		if (soaConfig.getDoubleCheck()) {
			if (StringUtils.isEmpty((t1.getIp()+"").trim())) {
				return false;
			}
			String url = String.format("http://%s:%s/radar/client/instance", t1.getIp().trim(), t1.getPort());
			boolean flag = client.check(url);
			if (flag) {
				Util.log(log, t1, "doubleCheck", "服务可用");
				try {
					instanceService.heartBeat(Arrays.asList(t1.getId()));
					log.info(soaConfig.getLogPrefix() + " is update heartbeat double_check", t1.getId());
				} catch (Exception e) {

				}
				if (type == 0) {
					emailUtil.sendInfoMail(
							"doublecheck,appId:" + t1.getCandAppId() + ",ip:" + t1.getIp() + "心跳超时，但是服务可用，请注意",
							"心跳超时，但是服务可用，请注意。json is " + JsonUtil.toJson(t1) + ",dbTime is " + dbNow);
				} else {
					emailUtil.sendInfoMail(
							"clearOldInstance,appId:" + t1.getCandAppId() + ",ip:" + t1.getIp() + "心跳超时，但是服务可用，不删除，请注意",
							"超过" + (soaConfig.getInstanceClearTime() / 60 / 60.0) + "小时无心跳，但是服务可用，不删除，请注意。 json is"
									+ JsonUtil.toJson(t1) + ",dbTime is " + dbNow);
				}
			}
			return flag;
		}
		return false;
	}

	public void stop() {
		isRunning = false;
	}
}