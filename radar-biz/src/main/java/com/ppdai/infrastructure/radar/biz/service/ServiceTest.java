package com.ppdai.infrastructure.radar.biz.service;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.exception.TestException;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.HeartBeatRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstanceDto;
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstancesRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstancesResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustSupperStatusRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.PubDeleteRequest;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class ServiceTest {

	private static Logger log = LoggerFactory.getLogger(ServiceTest.class);
	// @Autowired
	// private Environment environment;
	@Autowired
	private SoaConfig soaConfig;
	private static SoaConfig soaConfigProxy;
	private static ConnectionPool connectionPool = new ConnectionPool(1000, 20, TimeUnit.SECONDS);
	private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS)
			.readTimeout(35, TimeUnit.SECONDS).connectionPool(connectionPool).build();
	private static MediaType medialtype = MediaType.parse("application/json; charset=utf-8");
	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), SoaThreadFactory.create("registerInstance", true),
			new ThreadPoolExecutor.DiscardOldestPolicy());

	private static AtomicInteger counter = new AtomicInteger(0);
	private static String baseUrl = "http://localhost:8080";

	@PostConstruct
	private void init() {
		soaConfigProxy = soaConfig;
	}

	// 注意这个是用来进行功能测试的接口地址
	private static String getUrl() {		
		return baseUrl;
	}

	private static RegisterInstanceResponse registerInstance(RegisterInstanceRequest registerInstanceRequest) {
		String url = getUrl() + RadarConstanst.INSTPRE + "/registerInstance";
		Response response = null;
		String rs = "";
		try {
			response = call(url, registerInstanceRequest);
			try {
				rs = response.body().string();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			log.info("registerInstance_注册成功_" + rs);
			return JsonUtil.parseJson(rs, RegisterInstanceResponse.class);

		} finally {
			response.body().close();
		}
	}

	private static void heartBeat(List<Long> ids) {
		executor.submit(() -> {
			HeartBeatRequest beatRequest = new HeartBeatRequest();
			beatRequest.setInstanceIds(ids);
			String url = getUrl() + RadarConstanst.INSTPRE + "/heartbeat";
			Response response = null;
			try {
				response = call(url, beatRequest);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					response.body().close();
				} catch (Exception e) {
				}
			}
		});

	}

	private static GetAppResponse getApp(Map<String, Long> appVersion, boolean containOffLine, boolean isLong) {
		GetAppRequest getAppRequest = new GetAppRequest();
		getAppRequest.setAppVersion(appVersion);
		getAppRequest.setContainOffline(containOffLine);
		Response response = null;
		String url = getUrl() + RadarConstanst.APPPRE + "/" + (isLong ? "getAppPolling" : "getApp");
		try {
			response = call(url, getAppRequest);
			return JsonUtil.parseJson(response.body().string(), GetAppResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.body().close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return null;
	}

	private static GetAppResponse getApp(Map<String, Long> appVersion) {
		return getApp(appVersion, false, true);
	}

	private static void adjust(AdjustRequest request) {
		String url = getUrl() + RadarConstanst.PUBPRE + "/adjust";
		Response response = null;
		try {
			response = call(url, request);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null) {
					response.body().close();
				}
			} catch (Exception e1) {
			}
		}
	}

	private static AddInstancesResponse add(AddInstancesRequest request) {
		String url = getUrl() + RadarConstanst.PUBPRE + "/addInstance";
		Response response = null;
		try {
			response = call(url, request);
			return JsonUtil.parseJson(response.body().string(), AddInstancesResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null) {
					response.body().close();
				}
			} catch (Exception e1) {
			}
		}
		return null;
	}

	private static void supperAdjust(AdjustSupperStatusRequest request) {
		String url = getUrl() + RadarConstanst.PUBPRE + "/adjustSupperStatus";
		Response response = null;
		try {
			response = call(url, request);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.body().close();
			} catch (Exception e1) {
				// TODO: handle exception
			}
		}
	}

	private static void deregister(DeRegisterInstanceRequest request) {
		String url = getUrl() + RadarConstanst.INSTPRE + "/deRegisterInstance";
		Response response = null;
		try {
			response = call(url, request);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.body().close();
			} catch (Exception e1) {
				// TODO: handle exception
			}
		}
	}

	private static Response call(String url, Object request) {
		Response response = null;
		String turl = url.replaceAll(getUrl(), "");
		Transaction transaction = Tracer.newTransaction("Service-Test1", turl);
		try {
			response = okHttpClient.newCall(new Request.Builder().url(url)
					.post(RequestBody.create(medialtype, JsonUtil.toJson(request))).build()).execute();
			transaction.setStatus(Transaction.SUCCESS);
		} catch (SocketTimeoutException e) {
			transaction.setStatus(e);
			log.error("SocketTimeoutException_call_" + url.substring(url.indexOf('/')), e);
			// Util.sleep(1500);
			// return call(url,request);
		} catch (Exception e) {
			transaction.setStatus(e);
			log.error("Exception_call_" + url.substring(url.indexOf('/')), e);
		} finally {
			transaction.complete();
		}
		return response;
	}

	private static void delete(PubDeleteRequest request) {
		String url = getUrl() + RadarConstanst.PUBPRE + "/pubDel";
		Response response = null;
		try {
			response = call(url, request);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.body().close();
			} catch (Exception e1) {
				// TODO: handle exception
			}
		}
	}

	public void test1(int count) throws Exception {
		// 生产环境不能压测
		if (soaConfig.isPro()) {
			return;
		}
		Transaction transaction = Tracer.newTransaction("Service-Test", "Test");
		try {
			counter.set(0);
			HeartBeatSevice.start();
			doTest1(count);
			transaction.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			transaction.setStatus(e);
			throw e;
		} finally {
			transaction.complete();
		}
		throw new TestException();
	}

	private void doTest1(int count) {
		// String appName = "soa_rest_test1_" + count;
		// String candAppId = "soa_rest_test1_" + count;
		String appName = "soa_rest_test1_" + UUID.randomUUID().toString().replaceAll("-", "");
		String candAppId = "soa_rest_test1_" + UUID.randomUUID().toString().replaceAll("-", "");
		clearOldTestData(candAppId);
		Util.sleep(2000);
		RegisterInstanceRequest[] fails = getFailRq(candAppId, appName);

		for (RegisterInstanceRequest registerInstanceRequest : fails) {
			RegisterInstanceResponse resp = registerInstance(registerInstanceRequest);
			if (resp.isSuc()) {
				throw new RuntimeException("ServiceTest_1_参数校验不通过！");
			}
		}
		log.info("ServiceTest_2_参数校验通过！");
		Map<String, RegisterInstanceRequest> mapRegisterInstanceRequest = new ConcurrentHashMap<>();
		PubDeleteRequest deleteRequest = new PubDeleteRequest();
		List<Long> ids = new ArrayList<>();
		try {

			List<RegisterInstanceResponse> registerInstanceResponses = registInstanceTest(10,
					mapRegisterInstanceRequest, candAppId, appName);

			for (RegisterInstanceResponse t1 : registerInstanceResponses) {
				ids.add(t1.getInstanceId());
			}
			HeartBeatSevice.add(ids);
			Map<String, Long> rsMap = new HashMap<>();
			mapRegisterInstanceRequest.values().forEach(t1 -> {
				rsMap.put(t1.getCandAppId(), 0L);
			});

			// List<Integer> resultIds = new ArrayList<>();
			adjustAllUp(ids, rsMap);
			RegisterInstanceResponse resp = registerInstance(createTestDataAppIdFaild(appName));
			if (resp.isSuc()) {
				throw new RuntimeException("ServiceTest_1_参数校验不通过！");
			}
			executor.execute(() -> {
				int testCount = 0;
				while (testCount < soaConfig.getMaxTestCount()) {
					Util.sleep(1000);
					Random random = new Random();
					int i = random.nextInt(5) + 1;
					if (i == 1) {
						adjustAllUp(ids, rsMap);
					} else if (i == 2) {
						pullTest(ids.subList(0, 2), rsMap, ids.size());
					} else if (i == 3) {
						adjustSuperTest(ids, rsMap);
					} else if (i == 4) {
						hearBeatActiveTest(ids, rsMap, ids.size());
					} else if (i == 5) {
						deregisterTest(mapRegisterInstanceRequest, registerInstanceResponses, rsMap);
					}
					testCount++;
				}
			});
		} catch (Exception e) {
			throw e;
		} finally {
			deleteRequest.setIds(ids);
		}
	}

	/**
	 * Test No.0
	 */
	private void getAppTest(Map<String, Long> rsMap) {
		GetAppResponse getAppResponse = getApp(rsMap);
		if (getAppResponse != null && getAppResponse.getApp() != null && getAppResponse.getApp().size() > 0) {
			throw new RuntimeException("ServiceTest_No.0_获取app数量不对!,真实数量为" + getAppResponse.getApp().size()
					+ ",json 参数为：" + JsonUtil.toJsonNull(rsMap) + ",结果为" + JsonUtil.toJsonNull(getAppResponse));
		}
	}

	/**
	 * Test No.1
	 */
	private List<Long> adjustAllUp(List<Long> ids, Map<String, Long> rsMap) {
		AdjustRequest adjustRequest = new AdjustRequest();
		adjustRequest.setIds(ids);
		adjustRequest.setUp(true);
		adjust(adjustRequest);
		List<Long> resultIds = checkResult(rsMap, 10, "adjust_10_true");
		log.info("ServiceTest_No.1_状态调节成功！");
		return resultIds;
	}

	/**
	 * Test No.2
	 */
	private List<Long> pullTest(List<Long> ids, Map<String, Long> rsMap, Integer initSize) {
		AdjustRequest adjustRequest = new AdjustRequest();
		adjustRequest.setIds(ids);
		adjustRequest.setUp(false);
		adjust(adjustRequest);
		checkResult(rsMap, 8, "adjust_" + ids.size() + "_false");
		log.info("ServiceTest_No.2_拉出" + ids.size() + "个测试成功！");
		adjustRequest.setIds(ids);
		adjustRequest.setUp(true);
		adjust(adjustRequest);
		List<Long> resultIds;
		resultIds = checkResult(rsMap, 10, "adjust_" + ids.size() + "_true");
		log.info("ServiceTest_No.3_拉入" + ids.size() + "个测试成功！");
		return resultIds;
	}

	/**
	 * Test No.3
	 */
	private List<Long> adjustSuperTest(List<Long> ids, Map<String, Long> rsMap) {
		AdjustRequest adjustRequest = new AdjustRequest();
		AdjustSupperStatusRequest adjustSupperStatusRequest = new AdjustSupperStatusRequest();
		adjustRequest.setIds(ids);
		adjustRequest.setUp(false);
		adjust(adjustRequest);
		adjustSupperStatusRequest.setIds(ids);
		adjustSupperStatusRequest.setStatus(1);
		supperAdjust(adjustSupperStatusRequest);
		List<Long> resultIds = checkResult(rsMap, 10, "adjustSupper_true");
		log.info("ServiceTest_No.3_全部拉出，超级槽位拉入测试成功！");

		adjustSupperStatusRequest.setStatus(-1);
		supperAdjust(adjustSupperStatusRequest);
		adjustRequest.setIds(ids);
		adjustRequest.setUp(true);
		adjust(adjustRequest);
		resultIds = checkResult(rsMap, 0, "adjustSupper_false");
		log.info("ServiceTest_No.3_全部拉入，超级槽位拉出测试成功！");

		adjustRequest.setIds(ids);
		adjustRequest.setUp(true);
		adjust(adjustRequest);
		adjustSupperStatusRequest.setStatus(0);
		supperAdjust(adjustSupperStatusRequest);
		resultIds = checkResult(rsMap, 10, "adjustSupper_0");
		log.info("ServiceTest_No.3_全部拉入，超级槽位关闭测试成功！");
		return resultIds;
	}

	/**
	 * Test No.4
	 */
	private List<Long> hearBeatActiveTest(List<Long> ids, Map<String, Long> rsMap, Integer initSize) {
		HeartBeatSevice.clear(ids);
		// 特别需要注意心跳clear需要等待过期才行
		List<Long> resultIds = checkResult(rsMap, 0, "heartbeat_clear");
		log.info("ServiceTest_No.4_心跳测试正常！");
		HeartBeatSevice.add(ids);
		resultIds = checkResult(rsMap, 10, "heartbeat_add");
		log.info("ServiceTest_No.4_心跳测试正常！");

		return resultIds;
	}

	/**
	 * Test No.5
	 */
	private List<Long> deregisterTest(Map<String, RegisterInstanceRequest> mapRegisterInstanceRequest,
			List<RegisterInstanceResponse> registerInstanceResponses, Map<String, Long> rsMap) {
		AdjustRequest adjustRequest;
		adjustRequest = new AdjustRequest();

		DeRegisterInstanceRequest deRegisterInstanceRequest = new DeRegisterInstanceRequest();
		String canInstanceId = registerInstanceResponses.get(0).getCandInstanceId();
		deRegisterInstanceRequest.setInstanceId(registerInstanceResponses.get(0).getInstanceId());
		deregister(deRegisterInstanceRequest);
		checkResult(rsMap, 9, "deregister_1");
		log.info("ServiceTest_No.5_下线一个测试成功！");

		registerInstance(mapRegisterInstanceRequest.get(canInstanceId));
		adjustRequest.setIds(new ArrayList<>());
		adjustRequest.setCandInstanceIds(Arrays.asList(canInstanceId));
		adjustRequest.setUp(true);
		adjust(adjustRequest);
		// Util.sleep(2 * 1000);
		List<Long> resultIds = checkResult(rsMap, 10, "adjust_all");
		log.info("ServiceTest_No.5_心跳测试正常！");
		return resultIds;
	}

	public int clear() {
		HeartBeatSevice.stop();
		return 0;
	}

	public Object getHt() {
		return HeartBeatSevice.ht();
	}

	public void test(int count) throws Exception {
		Transaction transaction = Tracer.newTransaction("Service-Test", "Test");
		try {
			counter.set(0);
			HeartBeatSevice.start();
			doTest(count);
			transaction.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			transaction.setStatus(e);
			throw e;
		} finally {
			transaction.complete();
			HeartBeatSevice.stop();
		}
		throw new TestException();
	}

	private RegisterInstanceRequest[] getFailRq(String appId, String appName) {
		return new RegisterInstanceRequest[] { createTestDataCandAppIdFaild(appName),
				createTestDataAppNameFaild(appId) };
	}

	private void doTest(int count) {
		String appName = "soa_rest_test_new_" + count;
		String candAppId = "1000002070";
		clearOldTestData(candAppId);
		Util.sleep(2000);
		RegisterInstanceRequest[] fails = getFailRq(candAppId, appName);

		for (RegisterInstanceRequest registerInstanceRequest : fails) {
			RegisterInstanceResponse resp = registerInstance(registerInstanceRequest);
			if (resp.isSuc()) {
				throw new RuntimeException("ServiceTest_1_参数校验不通过！");
			}
		}
		log.info("ServiceTest_2_参数校验通过！");
		Map<String, RegisterInstanceRequest> mapRegisterInstanceRequest = new ConcurrentHashMap<>();
		PubDeleteRequest deleteRequest = new PubDeleteRequest();
		List<Long> ids = new ArrayList<>();
		Map<String, Long> rsMap = new HashMap<>();
		try {
			List<RegisterInstanceResponse> registerInstanceResponses = registInstanceTest(10,
					mapRegisterInstanceRequest, candAppId, appName);

			AdjustRequest adjustRequest = new AdjustRequest();
			for (RegisterInstanceResponse t1 : registerInstanceResponses) {
				ids.add(t1.getInstanceId());
			}
			HeartBeatSevice.add(ids);
			adjustRequest.setIds(ids);
			adjustRequest.setUp(true);
			adjust(adjustRequest);
			Util.sleep(2000);
			//注册成功后，校验参数
			RegisterInstanceResponse resp = registerInstance(createTestDataAppIdFaild(appName));
			if (resp.isSuc()) {
				throw new RuntimeException("ServiceTest_1_参数校验不通过！");
			}

			mapRegisterInstanceRequest.values().forEach(t1 -> {
				rsMap.put(t1.getCandAppId(), 0L);
			});
			checkResult(rsMap, 10, "adjust_10_true");
			log.info("ServiceTest_3_状态调节成功！");
			getAppTest(rsMap);
			log.info("ServiceTest_5_超时测试成功！");
			adjustRequest = new AdjustRequest();
			adjustRequest.setIds(Arrays.asList(registerInstanceResponses.get(0).getInstanceId(),
					registerInstanceResponses.get(1).getInstanceId()));
			adjustRequest.setUp(false);
			adjust(adjustRequest);
			// Util.sleep(2000);
			checkResult(rsMap, 8, "adjust_2_false");
			log.info("ServiceTest_6_拉出2个测试成功！");
			adjustRequest.setUp(true);
			adjust(adjustRequest);
			// Util.sleep(2000);
			checkResult(rsMap, 10, "adjust_10_true");
			log.info("ServiceTest_7_拉入2个测试成功！");
			adjustRequest = new AdjustRequest();
			adjustRequest.setIds(ids);
			adjustRequest.setUp(false);
			adjust(adjustRequest);
			AdjustSupperStatusRequest adjustSupperStatusRequest = new AdjustSupperStatusRequest();
			adjustSupperStatusRequest.setIds(ids);
			adjustSupperStatusRequest.setStatus(1);
			supperAdjust(adjustSupperStatusRequest);
			// Util.sleep(2000);
			checkResult(rsMap, 10, "adjustSupper_true");
			log.info("ServiceTest_8_全部拉出，超级槽位拉入测试成功！");
			adjustSupperStatusRequest.setStatus(-1);
			supperAdjust(adjustSupperStatusRequest);

			adjustRequest.setIds(ids);
			adjustRequest.setUp(true);
			adjust(adjustRequest);
			// Util.sleep(2000);
			checkResult(rsMap, 0, "adjustSupper_false");
			log.info("ServiceTest_9_全部拉入，超级槽位拉出测试成功！");
			adjustSupperStatusRequest.setStatus(0);
			supperAdjust(adjustSupperStatusRequest);
			// Util.sleep(2000);
			checkResult(rsMap, 10, "adjustSupper_0");
			log.info("ServiceTest_10_全部拉入，超级槽位关闭测试成功！");
			DeRegisterInstanceRequest deRegisterInstanceRequest = new DeRegisterInstanceRequest();
			String canInstanceId = registerInstanceResponses.get(0).getCandInstanceId();
			deRegisterInstanceRequest.setInstanceId(registerInstanceResponses.get(0).getInstanceId());
			deregister(deRegisterInstanceRequest);
			// Util.sleep(2000);
			checkResult(rsMap, 9, "deregister_1");
			log.info("ServiceTest_11_下线一个测试成功！");

			HeartBeatSevice.clear(ids);
			// Util.sleep((soaConfig.getExpiredTime()+2) * 1000);
			checkResult(rsMap, 0, "heartbeat_clear");
			log.info("ServiceTest_12_心跳测试正常！");

			HeartBeatSevice.add(ids);
			// Util.sleep((soaConfig.getExpiredTime()+2) * 1000);
			checkResult(rsMap, 9, "heartbeat_add");
			log.info("ServiceTest_13_心跳测试正常！");

			registerInstance(mapRegisterInstanceRequest.get(canInstanceId));
			adjustRequest.setIds(new ArrayList<>());
			adjustRequest.setCandInstanceIds(Arrays.asList(canInstanceId));
			adjustRequest.setUp(true);
			adjust(adjustRequest);
			// Util.sleep(2 * 1000);
			checkResult(rsMap, 10, "adjust_all");
			log.info("ServiceTest_14_心跳测试正常！");

			deleteRequest.setIds(ids.subList(0, 5));
			delete(deleteRequest);
			Util.sleep(2000);
			checkResult(rsMap, 5, "delete");
			log.info("ServiceTest_15_删除部分测试正常！");

			List<AddInstanceDto> instancesRadar = new ArrayList<>();

			AddInstanceDto instanceRadar = new AddInstanceDto();
			instanceRadar.setCandInstanceId("111111111111111111");
			instancesRadar.add(instanceRadar);

			AddInstancesRequest instancesRequest = new AddInstancesRequest();
			instancesRequest.setCandAppId(candAppId);
			instancesRequest.setClusterName("default");
			instancesRequest.setCandInstances(instancesRadar);
			pubAdd(instancesRequest);

		} catch (Exception e) {
			throw e;
		} finally {
			deleteRequest.setIds(ids);
			deleteRequest.setCandInstanceIds(Arrays.asList("111111111111111111"));
			delete(deleteRequest);
			checkResult(rsMap, 0, "deleteAll");
			HeartBeatSevice.clear(ids);
		}
		log.info("ServiceTest_16_删除全部测试正常！");
		log.info("ServiceTest_17_测试完成正常！");
	}

	private void pubAdd(AddInstancesRequest instancesRequest) {
		try {
			AddInstancesResponse addInstancesResponse = add(instancesRequest);
			if (addInstancesResponse == null || !addInstancesResponse.isSuc()) {
				String errorString = "pubadd 测试异常！";
				RuntimeException error = new RuntimeException(errorString);
				log.error("Service_Test_error", error);
				throw error;
			}
		} catch (Exception e) {

		}

	}

	private void clearOldTestData(String candAppId) {
		Map<String, Long> appMap = new HashMap<>();
		appMap.put(candAppId, -1L);
		GetAppResponse getAppResponse = getApp(appMap, true, false);
		// String rs = JsonUtil.toJsonNull(getAppResponse);
		// System.out.println(rs);
		List<Long> ids = new ArrayList<>();
		if (getAppResponse != null && getAppResponse.getApp() != null) {
			getAppResponse.getApp().values().forEach(t1 -> {
				if (t1.getClusters() != null) {
					t1.getClusters().forEach(t2 -> {
						if (t2.getInstances() != null) {
							t2.getInstances().forEach(t3 -> {
								ids.add(t3.getId());
							});
						}
					});
				}
			});
		}
		if (ids.size() != 0) {
			HeartBeatSevice.clear(ids);
			PubDeleteRequest deleteRequest = new PubDeleteRequest();
			deleteRequest.setIds(ids);
			delete(deleteRequest);
		}
	}

	private List<Long> checkResult(Map<String, Long> rsMap, Integer count, String info) {
		if ("heartbeat_clear".equals(info)) {
			Util.sleep(soaConfig.getExpiredTime() * 1000);
		} else {
			Util.sleep(1500);
		}
		int counter = 0;
		String log1 = "ServiceTest_checkResult_" + count + "_" + info;
		Transaction transaction = Tracer.newTransaction("Service-Test", "checkResult_" + count + "_" + info);
		try {
			List<Long> resultIds = doCheckResult(rsMap, count, info);
			while (counter < 4 && !count.equals(resultIds.size())) {
				resultIds = doCheckResult(rsMap, count, info);
				Util.sleep(1500);
				counter++;
			}
			if (count.equals(resultIds.size())) {
				transaction.setStatus(Transaction.SUCCESS);
			} else {
				String errorString = "获取app数量" + count + "不对,当前数量为" + resultIds.size() + "," + log1 + "！";
				RuntimeException error = new RuntimeException(errorString);
				log.error("Service_Test_error", error);
				transaction.setStatus(error);
				throw error;
			}
			return resultIds;
		} catch (Exception e) {
			transaction.setStatus(e);
			throw e;
		} finally {
			transaction.complete();
		}

	}

	private List<Long> doCheckResult(Map<String, Long> rsMap, Integer count, String info) {
		// String rs = "";
		GetAppResponse getAppResponse;
		// String log1 = "ServiceTest_checkResult_" + count +
		// JsonUtil.toJsonNull(rsMap).replaceAll("\\n", "");
		Util.sleep(1500);
		getAppResponse = getApp(rsMap);

		log.info("ServiceTest_checkcheckResult_" + count + "_end");
		if (getAppResponse == null || !getAppResponse.isSuc()) {
			RuntimeException exception = new RuntimeException("获取app异常！");
			throw exception;
		}
		List<Long> rsList = new ArrayList<>();
		if (getAppResponse.getApp() != null) {
			getAppResponse.getApp().values().forEach(t1 -> {
				rsMap.put(t1.getAppMeta().getCandAppId(), t1.getAppMeta().getVersion());
				if (!CollectionUtils.isEmpty(t1.getClusters())) {

					t1.getClusters().forEach(t2 -> {
						if ("deleteAll".equals(info) && t2.getAppClusterMeta() != null) {
							if (t2.getAppClusterMeta().getDeleteFlag() != 1) {
								RuntimeException exception = new RuntimeException("测试DeleteFlag异常！");
								throw exception;
							}
						}
						if (!CollectionUtils.isEmpty(t2.getInstances())) {
							t2.getInstances().forEach(t3 -> {
								rsList.add(t3.getId());
							});
						}
					});
				}
			});
		}
		return rsList;
	}

	private List<RegisterInstanceResponse> registInstanceTest(int count,
			Map<String, RegisterInstanceRequest> mapRegisterInstanceRequest, String candAppId, String appName) {
		List<RegisterInstanceResponse> registerInstanceResponses = new ArrayList<>();
		CountDownLatch countDownLatch = new CountDownLatch(count);
		Transaction transaction = Tracer.newTransaction("Service-Test",
				"/api/client/app/instance/registerInstance-" + count);
		Lock lock = new ReentrantLock();
		try {
			log.info("ServiceTest_0_{}开始注册！", count);
			for (int i = 0; i < count; i++) {
				executor.submit(() -> {
					RegisterInstanceRequest registerInstanceRequest = createTestDataOk(candAppId, appName);
					mapRegisterInstanceRequest.put(registerInstanceRequest.getCandInstanceId(),
							registerInstanceRequest);
					RegisterInstanceResponse response = registerInstance(registerInstanceRequest);
					lock.lock();
					registerInstanceResponses.add(response);
					System.out.println("count--" + counter.incrementAndGet());
					lock.unlock();
					countDownLatch.countDown();
				});
			}
			try {
				countDownLatch.await();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			transaction.setStatus(Transaction.SUCCESS);
			log.info("ServiceTest_0_{}注册成功！", count);
			if (registerInstanceResponses.size() < 10) {
				throw new RuntimeException("注册服务超时！");
			}
			registerInstanceResponses.forEach(t1 -> {
				if (!t1.isSuc()) {
					throw new RuntimeException("注册服务异常," + t1.getMsg());
				}
			});
			return registerInstanceResponses;
		} catch (Exception e) {
			transaction.setStatus(e);
			throw e;
		} finally {
			transaction.complete();
		}
	}

	private RegisterInstanceRequest createTestDataOk(String candAppId, String appName) {
		RegisterInstanceRequest testData = new RegisterInstanceRequest();
		testData.setCandAppId(candAppId);
		testData.setAppName(appName);
		testData.setClusterName("radar-test");
		testData.setCandInstanceId(UUID.randomUUID().toString().replaceAll("-", ""));
		// testData.getClientIp(IPUtil.getLocalIP());
		testData.setPort(80);
		testData.setLan("java");
		testData.setSdkVersion("0.0.1");
		testData.setServName("test1,test2,test3");
		return testData;
	}

	private RegisterInstanceRequest createTestDataCandAppIdFaild(String appName) {
		RegisterInstanceRequest testData = new RegisterInstanceRequest();
		testData.setAppName(appName);
		testData.setClusterName("radar-test");
		testData.setCandInstanceId(UUID.randomUUID().toString());
		testData.setClientIp("127.0.0.1");
		testData.setPort(80);
		testData.setLan("java");
		testData.setSdkVersion("0.0.1");
		testData.setServName("test1,test2,test3");
		return testData;
	}

	private RegisterInstanceRequest createTestDataAppNameFaild(String candAppId) {
		RegisterInstanceRequest testData = new RegisterInstanceRequest();
		testData.setCandAppId(candAppId);
		// testData.setAppName(appName);
		testData.setClusterName("radar-test");
		testData.setCandInstanceId(UUID.randomUUID().toString());
		testData.setClientIp("127.0.0.1");
		testData.setPort(80);
		testData.setLan("java");
		testData.setSdkVersion("0.0.1");
		testData.setServName("test1,test2,test3");
		return testData;
	}

	// 测试appid与appname 不唯一匹配
	private RegisterInstanceRequest createTestDataAppIdFaild(String appName) {
		RegisterInstanceRequest testData = new RegisterInstanceRequest();
		testData.setCandAppId("fdasfaff");
		testData.setAppName(appName);
		testData.setClusterName("radar-test");
		testData.setCandInstanceId(UUID.randomUUID().toString());
		testData.setClientIp("127.0.0.1");
		testData.setPort(80);
		testData.setLan("java");
		testData.setSdkVersion("0.0.1");
		testData.setServName("test1,test2,test3");
		return testData;
	}

	static class HeartBeatSevice {
		// private ThreadPoolExecutor executor1 = new ThreadPoolExecutor(50,
		// 2000, 0L, TimeUnit.MILLISECONDS,
		// new LinkedBlockingQueue<Runnable>(),
		// SoaThreadFactory.create("registerInstance", true));
		private static Map<Long, Date> heartMap = new ConcurrentHashMap<>();
		private static ThreadPoolExecutor executor = null;

		private static void doHt() {
			if (executor.getActiveCount() == 0) {
				executor.execute(() -> {
					while (true && flag) {
						// log.info("start-heartbeat");
						heartBeat(new ArrayList<>(heartMap.keySet()));
						// heartMap.keySet().forEach(t1 -> {
						// heartMap.put(t1, new Date());
						// heartBeat(Arrays.asList(t1));
						// });
						// log.info("end-heartbeat-"+heartMap.size());
						Util.sleep(1000);
					}
				});
			}
		}

		private static volatile boolean flag = false;
		private static Object lockObj = new Object();

		public static void stop() {
			flag = false;
			try {
				executor.shutdown();
				executor = null;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		public static void start() {
			if (!flag) {
				synchronized (lockObj) {
					if (!flag) {
						flag = true;
						executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
								new LinkedBlockingQueue<Runnable>(), SoaThreadFactory.create("HeartBeatSevice", true));
						doHt();
					}
				}
			}
		}

		public static void clear(List<Long> ids) {
			ids.forEach(id -> {
				heartMap.remove(id);
			});
		}

		public static void add(List<Long> ids) {
			ids.forEach(id -> {
				heartMap.put(id, new Date());
			});
		}

		public static Object ht() {
			return heartMap;
		}
	}
}
