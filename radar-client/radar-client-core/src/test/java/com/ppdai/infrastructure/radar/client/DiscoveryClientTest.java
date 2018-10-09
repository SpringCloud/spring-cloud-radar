package com.ppdai.infrastructure.radar.client;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ppdai.infrastructure.radar.client.config.RadarClientConfig;
import com.ppdai.infrastructure.radar.client.dto.RadarInstance;
import com.ppdai.infrastructure.radar.client.event.AppChangedEvent;
import com.ppdai.infrastructure.radar.client.event.AppChangedListener;
import com.ppdai.infrastructure.radar.client.utils.SoaThreadFactory;

/**
 * Created by zhangyicong on 18-2-8. 因为内置单例的原因，此测试用例需要单个的运行
 */
public class DiscoveryClientTest extends RegistryServerMock {

	// 注意此地址是 RegistryServerMock的截取地址
	private String rgUrl = "http://localhost:1080";
	private static final Logger logger = LoggerFactory.getLogger(DiscoveryClientTest.class);

	@Test(expected = TimeoutException.class)
	public void testRegisterInstanceRetry() throws InterruptedException, ExecutionException, TimeoutException {
		RadarClientConfig config = new RadarClientConfig(rgUrl);

		RadarInstance instanceRadar = RadarInstance.getBuilder()
				// 设置实例ID，可以为空
				.withCandInstanceId("262a15e7-6b60-432d-abdb-e939fff2fd76")
				// 设置环境名称
				.withClusterName("dc1.prd")
				// 设置AppId
				.withCandAppId("10010001")
				// 设置AppName
				.withAppName("RegistryInstanceRetry")
				// 设置IP地址
				.withHost("10.1.30.2")
				// 设置端口
				.withPort(0).build();

		DiscoveryClient client = DiscoveryClient.getInstance();

		ThreadPoolExecutor executors = new ThreadPoolExecutor(1, 1, 3L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
				SoaThreadFactory.create("DiscoveryClientTest", true), new ThreadPoolExecutor.DiscardOldestPolicy());
		Future<Boolean> f = executors.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws NoSuchFieldException, IllegalAccessException {
				// 启动客户端
				client.setConfig(config);
				Field field = client.getClass().getDeclaredField("isStartUp");
				field.setAccessible(true);
				field.set(client, new AtomicBoolean(false));
				// 启动客户端
				client.register(instanceRadar);
				return true;
			}
		});
		f.get(5, TimeUnit.SECONDS);
	}

	@Test
	public void testDiscoveryClient() throws InterruptedException, IllegalAccessException, NoSuchFieldException {
		RadarClientConfig config = new RadarClientConfig(rgUrl);
		RadarInstance instanceRadar = RadarInstance.getBuilder()
				// 设置实例ID，可以为空
				.withCandInstanceId("262a15e7-6b60-432d-abdb-e939fff2fd76")
				// 设置环境名称
				.withClusterName("dc1.prd")
				// 设置AppId
				.withCandAppId("10010001")
				// 设置AppName
				.withAppName("RegistryInstance")
				// 设置IP地址
				.withHost("10.1.30.2")
				// 设置端口
				.withPort(0).build();

		DiscoveryClient client = DiscoveryClient.getInstance();
		client.setConfig(config);
		Field field = client.getClass().getDeclaredField("isStartUp");
		field.setAccessible(true);
		field.set(client, new AtomicBoolean(false));
		// 启动客户端
		client.register(instanceRadar);
		Thread.sleep(10000);
		client.deregister();
	}

	// @Test(expected = RadarException.class)
	@Test
	public void testRegisterInstanceFailNoRetry() throws IllegalAccessException, NoSuchFieldException {
		RadarClientConfig config = new RadarClientConfig(rgUrl);

		RadarInstance instanceRadar = RadarInstance.getBuilder()
				// 设置实例ID，可以为空
				.withCandInstanceId("262a15e7-6b60-432d-abdb-e939fff2fd76")
				// 设置环境名称
				.withClusterName("dc1.prd")
				// 设置AppId
				.withCandAppId("10010001")
				// 设置AppName
				.withAppName("RegistryInstanceFailNoRetry")
				// 设置IP地址
				.withHost("10.1.30.2")
				// 设置端口
				.withPort(0).build();

		DiscoveryClient client = DiscoveryClient.getInstance();
		client.setConfig(config);
		Field field = client.getClass().getDeclaredField("isStartUp");
		field.setAccessible(true);
		field.set(client, new AtomicBoolean(false));
		// 启动客户端
		client.register(instanceRadar);
	}

	@Test
	public void testGetApp() throws InterruptedException, IllegalAccessException, NoSuchFieldException {
		RadarClientConfig config = new RadarClientConfig(rgUrl);
		config.setHost("1.1.1.1");
		DiscoveryClient client = DiscoveryClient.getInstance();
		client.setConfig(config);
		Field field = client.getClass().getDeclaredField("config");
		field.setAccessible(true);
		field.set(client, config);
		Assert.assertEquals(0L, client.getApp("1111").getVersion());
		Thread.sleep(3000);
		Assert.assertNotNull(client.getData().get("1111"));
	}

	@Test
	public void testSubscribeAll() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
		RadarClientConfig config = new RadarClientConfig(rgUrl);
		config.setHost("1.1.1.1");
		DiscoveryClient client = DiscoveryClient.getInstance();
		client.setConfig(config);
		Field field = client.getClass().getDeclaredField("config");
		field.setAccessible(true);
		field.set(client, config);
		//client.subscribeAll();
		// 核心逻辑在异步线程，所以sleep一下
		Thread.sleep(2000);
		// 通过反射去缓存看一下，是否执行成功
		field = client.getClass().getDeclaredField("appVersion");
		field.setAccessible(true);
		Map<String, Long> appVersion = (Map<String, Long>)field.get(client);
		Assert.assertTrue(appVersion.get("121").equals(0L));
	}

	@Test
	public void addAppChangedListener() throws InterruptedException, IllegalAccessException, NoSuchFieldException {
		RadarClientConfig config = new RadarClientConfig(rgUrl);
		config.setHost("1.1.1.1");
		DiscoveryClient client = DiscoveryClient.getInstance();
		client.setConfig(config);
		Field field = client.getClass().getDeclaredField("config");
		field.setAccessible(true);
		field.set(client, config);
		Map<String, String> testMap = new HashMap<>();
		client.addAppChangedListener(new AppChangedListener() {
			@Override
			public void onAppChanged(AppChangedEvent changeEvent) {
				testMap.put("1", "2");
			}
		});
		client.getApp("1111");
		Thread.sleep(1000);
		Assert.assertEquals("2", testMap.get("1"));
	}

	@Test
	public void isRegister() throws IllegalAccessException, NoSuchFieldException {
		RadarClientConfig config = new RadarClientConfig(rgUrl);
		config.setHost("1.1.1.1");
		DiscoveryClient client = DiscoveryClient.getInstance();
		client.setConfig(config);
		Field field = client.getClass().getDeclaredField("config");
		field.setAccessible(true);
		field.set(client, config);
	 	Assert.assertEquals(false, client.isRegistered());
		RadarInstance instanceRadar = RadarInstance.getBuilder()
				// 设置实例ID，可以为空
				.withCandInstanceId("262a15e7-6b60-432d-abdb-e939fff2fd76")
				// 设置环境名称
				.withClusterName("dc1.prd")
				// 设置AppId
				.withCandAppId("10010001")
				// 设置AppName
				.withAppName("RegistryInstanceFailNoRetry")
				// 设置IP地址
				.withHost("10.1.30.2")
				// 设置端口
				.withPort(0).build();
		field = client.getClass().getDeclaredField("isStartUp");
		field.setAccessible(true);
		field.set(client, new AtomicBoolean(false));
	 	client.register(instanceRadar);
	 	Assert.assertEquals(true, client.isRegistered());
	}


	@Test
	public void waitAllShutdown() {
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5,5 , TimeUnit.SECONDS, new LinkedBlockingQueue<>(10),
				SoaThreadFactory.create("DiscoveryClientTest", false), new ThreadPoolExecutor.DiscardOldestPolicy());
		threadPoolExecutor.execute(() -> {
			try {
				RadarClientConfig config = new RadarClientConfig(rgUrl);
				config.setHost("1.1.1.1");
				DiscoveryClient client = DiscoveryClient.getInstance();
				client.setConfig(config);
				Field field = client.getClass().getDeclaredField("config");
				field.setAccessible(true);
				field.set(client, config);
				//client.subscribeAll();
				Thread.sleep(3000);
				SoaThreadFactory.getThreadGroup().destroy();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		});
		Assert.assertEquals(true, SoaThreadFactory.waitAllShutdown(5000));


	}
}
