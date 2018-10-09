package com.ppdai.infrastructure.radar;

import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.PubDeleteRequest;
import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;
import com.ppdai.infrastructure.radar.biz.service.AppClusterService;
import com.ppdai.infrastructure.radar.biz.service.AppService;
import com.ppdai.infrastructure.radar.biz.service.InstanceService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RadarBizApplicationTests extends AbstractTest {

	@Autowired
	private InstanceService instanceService;
	@Autowired
	private AppService appService;
	@Autowired
	private AppClusterService appClusterService;


	@Ignore
	@Test
	public void testRegisterInstance() throws Exception {
		//单个注册实例
		InstanceEntity testData=createTestData();
		//封装请求实例
		InstanceEntity instanceEntity=registerInstance(testData);
		//比较操作数据是否正确
		Assert.assertEquals(testData.getAppName(),instanceEntity.getAppName());
		Assert.assertEquals(testData.getAppClusterName(),instanceEntity.getAppClusterName());
		Assert.assertEquals(testData.getCandInstanceId(),instanceEntity.getCandInstanceId());
		//等待1s，获取app的缓存数据
		appService.startCache();
		Thread.sleep(1000);
		Map<String, AppEntity> cacheData = appService.getCacheData();
		Assert.assertEquals(testData.getCandAppId(),cacheData.get(testData.getCandAppId()).getCandAppId());
		Assert.assertEquals(testData.getAppName(),cacheData.get(testData.getCandAppId()).getAppName());
		Thread.sleep(1000);
		Map<String, Long> appClusterCache = appClusterService.getCache();
		Assert.assertEquals(appClusterCache.containsKey(testData.getCandAppId()+"_"+testData.getAppClusterName()),true);
		testDeRegister(cacheData.get(testData.getCandAppId()).getId());
	}

	@Test
	public void testRegisterInstances() throws Exception {
		//批量注册实例
		List<InstanceEntity> testDataList=new ArrayList<>();
		List<RegisterInstanceRequest> registerList=new ArrayList<>();
		for(int i=0;i<5;i++){
			InstanceEntity tempEntity=createTestData();
			testDataList.add(tempEntity);
			RegisterInstanceRequest registerInstanceRequest=setRegisterInstanceRequest(tempEntity);
			registerList.add(registerInstanceRequest);
		}
		instanceService.registerInstance(registerList);

		for(int i=0;i<5;i++){
			InstanceEntity tempInstanceEntity = instanceService.findByCandInstanceId(testDataList.get(i).getCandInstanceId());
			//compareData(testDataList.get(i),tempInstanceEntity);
			Assert.assertEquals(testDataList.get(i).getAppName(),tempInstanceEntity.getAppName());
			Assert.assertEquals(testDataList.get(i).getAppClusterName(),tempInstanceEntity.getAppClusterName());
			Assert.assertEquals(testDataList.get(i).getCandInstanceId(),tempInstanceEntity.getCandInstanceId());
			//等待1s，获取app的缓存数据
			appService.startCache();
			Thread.sleep(1000);
			Map<String, AppEntity> cacheData = appService.getCacheData();
			Assert.assertEquals(testDataList.get(i).getCandAppId(),cacheData.get(testDataList.get(i).getCandAppId()).getCandAppId());
			Assert.assertEquals(testDataList.get(i).getAppName(),cacheData.get(testDataList.get(i).getCandAppId()).getAppName());
			Thread.sleep(1000);
			Map<String, Long> appClusterCache = appClusterService.getCache();
			Assert.assertEquals(appClusterCache.containsKey(testDataList.get(i).getCandAppId()+"_"+testDataList.get(i).getAppClusterName()),true);
		}

		//批量删除实例
		pubDeleteInstance(testDataList);
		for(InstanceEntity instanceEntity1:testDataList){
			InstanceEntity testDeleteInstance = instanceService.findByCandInstanceId(instanceEntity1.getCandInstanceId());
			Assert.assertEquals(testDeleteInstance,null);
		}
	}


	private void testDeRegister(long instanceId){
		DeRegisterInstanceRequest deRegisterInstanceRequest = new DeRegisterInstanceRequest();
		deRegisterInstanceRequest.setInstanceId(instanceId);
		instanceService.deRegister(deRegisterInstanceRequest);
	}

	private void pubDeleteInstance(List<InstanceEntity> instanceEntitys) throws Exception{
		PubDeleteRequest pubDeleteRequest=new PubDeleteRequest();
		List<Long> ids=new ArrayList<>();
		List<String> candInstanceIds=new ArrayList<>();
		if(instanceEntitys!=null){
			for(InstanceEntity tempEmtity:instanceEntitys){
				ids.add(tempEmtity.getId());
				candInstanceIds.add(tempEmtity.getCandInstanceId());
			}
		}
		pubDeleteRequest.setIds(ids);
		pubDeleteRequest.setCandInstanceIds(candInstanceIds);
		instanceService.pubDelete(pubDeleteRequest);
	}

	private InstanceEntity registerInstance(InstanceEntity testData) throws InterruptedException {
		RegisterInstanceRequest registerInstanceRequest=setRegisterInstanceRequest(testData);
		instanceService.registerInstance(registerInstanceRequest);
		//比较操作结果是否正确
		InstanceEntity instanceEntity = instanceService.findByCandInstanceId(testData.getCandInstanceId());
		return instanceEntity;
	}

	private void compareData(InstanceEntity testData,InstanceEntity instanceEntity) throws InterruptedException {
		Assert.assertEquals(testData.getAppName(),instanceEntity.getAppName());
		Assert.assertEquals(testData.getAppClusterName(),instanceEntity.getAppClusterName());
		Assert.assertEquals(testData.getCandInstanceId(),instanceEntity.getCandInstanceId());
		//等待1s，获取app的缓存数据
		appService.startCache();
		Thread.sleep(1000);
		Map<String, AppEntity> cacheData = appService.getCacheData();
		Assert.assertEquals(testData.getCandAppId(),cacheData.get(testData.getCandAppId()).getCandAppId());
		Assert.assertEquals(testData.getAppName(),cacheData.get(testData.getCandAppId()).getAppName());
		Thread.sleep(1000);
		Map<String, Long> appClusterCache = appClusterService.getCache();
		Assert.assertEquals(appClusterCache.containsKey(testData.getCandAppId()+"_"+testData.getAppClusterName()),true);
	}

	private InstanceEntity createTestData() {
		InstanceEntity instanceEntity=new InstanceEntity();
		instanceEntity.setCandAppId(UUID.randomUUID().toString().replaceAll("-", ""));
		instanceEntity.setAppName(UUID.randomUUID().toString().replaceAll("-", ""));
		instanceEntity.setAppClusterName("radar-test");
		instanceEntity.setCandInstanceId(UUID.randomUUID().toString().replaceAll("-", ""));
		instanceEntity.setIp("127.0.0.1");
		instanceEntity.setPort(8080);
		instanceEntity.setLan("java");
		instanceEntity.setSdkVersion("0.0.1");
		instanceEntity.setServName("test1,test2,test3");
		return  instanceEntity;
	}

	private RegisterInstanceRequest setRegisterInstanceRequest(InstanceEntity instanceEntity){
		RegisterInstanceRequest registerInstanceRequest = new RegisterInstanceRequest();
		registerInstanceRequest.setAppName(instanceEntity.getAppName());
		registerInstanceRequest.setCandInstanceId(instanceEntity.getCandInstanceId());
		registerInstanceRequest.setCandAppId(instanceEntity.getCandAppId());
		registerInstanceRequest.setClusterName(instanceEntity.getAppClusterName());
		registerInstanceRequest.setServName(instanceEntity.getServName());
		registerInstanceRequest.setClientIp(instanceEntity.getIp());
		registerInstanceRequest.setLan(instanceEntity.getLan());
		registerInstanceRequest.setSdkVersion(instanceEntity.getSdkVersion());
		return  registerInstanceRequest;
	}

}
