package com.ppdai.infrastructure.radar.biz.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.common.util.Util;
import com.ppdai.infrastructure.radar.biz.dal.InstanceRepository;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstancesRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstancesResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustSupperStatusRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustSupperStatusResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusInstanceDto;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.PubDeleteRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.PubDeleteResponse;
import com.ppdai.infrastructure.radar.biz.entity.AppClusterEntity;
import com.ppdai.infrastructure.radar.biz.entity.AppEntity;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;

@Service
public class InstanceService {

	private Logger log = LoggerFactory.getLogger(InstanceService.class);
	@Autowired
	private InstanceRepository instanceRepository;
	@Autowired
	private AppService appService;

	@Autowired
	private AppClusterService appClusterService;
	@Autowired
	private SoaConfig soaConfig;

	public List<InstanceEntity> getAll() {
		return instanceRepository.findAll();
	}

	public InstanceEntity findById(long id) {
		return instanceRepository.findById(id);
	}

	public InstanceEntity findByCandInstanceId(String candInstanceId) {
		return instanceRepository.findByCandInstanceId(candInstanceId);
	}

	public List<InstanceEntity> findByCandInstanceId(List<String> candInstanceIds) {
		return instanceRepository.findByCandInstanceIds(candInstanceIds);
	}

	public long getCount() {
		Long count = instanceRepository.getCount();
		if (count == null) {
			return 0;
		}
		return count;
	}

	public void save(InstanceEntity entity) {
		instanceRepository.insert(entity);
		Util.log(log, entity, "instance_save");
	}

	// key为CandInstanceId，value为id
	private Map<String, Long> save(Map<String, InstanceEntity> mapAddRs, Map<String, InstanceEntity> mapUpdateRs) {
		Map<String, Long> rsMap = new HashMap<>();
		if (mapUpdateRs.size() > 0) {
			// 如果存在需要更新
			mapUpdateRs.values().forEach(t1 -> {
				instanceRepository.update(t1);
				rsMap.put(t1.getCandInstanceId(), t1.getId());
			});
		}
		if (mapAddRs.size() > 0) {
			try {
				// 因为CandInstanceId唯一所以不会出现并发插入的问题
				instanceRepository.insertBatch(new ArrayList<>(mapAddRs.values()));
			} catch (Exception e) {
				mapAddRs.values().forEach(t1 -> {
					try {
						instanceRepository.insert(t1);
					} catch (Exception e2) {
					}

				});
			}
			List<InstanceEntity> instanceEntities = instanceRepository
					.findByCandInstanceIds(new ArrayList<>(mapAddRs.keySet()));
			instanceEntities.forEach(t1 -> {
				rsMap.put(t1.getCandInstanceId(), t1.getId());
				Util.log(log, t1, "instance_add");
			});
		}
		return rsMap;
	}

	// @Transactional
	public RegisterInstanceResponse registerInstance(RegisterInstanceRequest request) {
		RegisterInstanceResponse registResponse = new RegisterInstanceResponse();
		registResponse.setSuc(true);
		registResponse.setCode(RadarConstanst.YES);
		// registResponse.setHeartbeatTime(soaConfig.getHeartBeatTime());
		checkRegistVaild(request, registResponse);
		if (!registResponse.isSuc()) {
			log.info("registerInstance_error：" + registResponse.getMsg() + ",输入参数为：" + JsonUtil.toJsonNull(request));
			return registResponse;
		}
		return registerInstance(Arrays.asList(request)).get(request.getCandInstanceId());
	}

	// key为CandInstanceId
	public Map<String, RegisterInstanceResponse> registerInstance(List<RegisterInstanceRequest> requestOk) {
		Map<String, RegisterInstanceResponse> registFaileResponseMap = new HashMap<>();
		// key为CandInstanceId，value为id
		Map<String, Long> mapRs = addOrupdateInstances(requestOk);
		requestOk.forEach(t1 -> {
			if (mapRs.containsKey(t1.getCandInstanceId())) {
				RegisterInstanceResponse registResponse = new RegisterInstanceResponse();
				registResponse.setSuc(true);
				registResponse.setCode(RadarConstanst.YES);
				registResponse.setInstanceId(mapRs.get(t1.getCandInstanceId()));
				registResponse.setCandInstanceId(t1.getCandInstanceId());
				registFaileResponseMap.put(t1.getCandInstanceId(), registResponse);
				log.info("app_{}_instance_{} 注册成功！", t1.getCandAppId(), mapRs.get(t1.getCandInstanceId()),
						JsonUtil.toJsonNull(t1));
			} else {
				RegisterInstanceResponse registResponse = new RegisterInstanceResponse();
				registResponse.setSuc(false);
				registResponse.setCode(RadarConstanst.NO);
				registResponse.setInstanceId(-1);
				registResponse
						.setMsg(String.format("app_%s_instance_%s 注册失败！", t1.getCandAppId(), t1.getCandInstanceId()));
				log.info("registerInstance_error_app_{}_instance_{} 注册失败！", t1.getCandAppId(), t1.getCandInstanceId(),
						JsonUtil.toJsonNull(t1));
				registFaileResponseMap.put(t1.getCandInstanceId(), registResponse);
			}
		});
		return registFaileResponseMap;
	}

	// key为CandInstanceId，value为id
	private Map<String, Long> addOrupdateInstances(List<RegisterInstanceRequest> requests) {
		// 需要新加的实例
		Map<String, InstanceEntity> rsAddMap = new HashMap<>();
		Map<Long, Integer> appIdMap = new HashMap<>();
		prepareData(requests, rsAddMap, appIdMap);
		List<String> candInstanceIds = new ArrayList<>(rsAddMap.keySet());
		// 需要更新的实例
		Map<String, InstanceEntity> rsUpdateMap = new HashMap<>();
		if (!CollectionUtils.isEmpty(candInstanceIds)) {
			List<InstanceEntity> lstDb = instanceRepository.findByCandInstanceIds(candInstanceIds);
			lstDb.forEach(t1 -> {
				if (rsAddMap.containsKey(t1.getCandInstanceId())) {
					// 更新数据库数据
					updateInstanceEntity(rsAddMap.get(t1.getCandInstanceId()), t1);
					rsUpdateMap.put(t1.getCandInstanceId(), t1);
					rsAddMap.remove(t1.getCandInstanceId());
				}
			});
		}
		// key为CandInstanceId，value为id
		Map<String, Long> rsMap1 = save(rsAddMap, rsUpdateMap);
		requests.forEach(t1 -> {
			log.info("app_{}_instance_{}_is_insert_and_update_version", t1.getCandAppId(), t1.getCandInstanceId());
		});

		appService.updateVersionByIds(new ArrayList<>(appIdMap.keySet()));
		// 自动绑定id
		// return entity;
		return rsMap1;
	}

	private void prepareData(List<RegisterInstanceRequest> requests, Map<String, InstanceEntity> mapRs,
			Map<Long, Integer> appIdMap) {
		requests.forEach(request -> {
			AppEntity appEntity = new AppEntity();
			appEntity.setAppName(request.getAppName());
			appEntity.setCandAppId(request.getCandAppId());
			// 注意此时appName可能跟传进来的不一致，这个地址主要是用appId和appName
			appEntity = appService.save(appEntity);
			appIdMap.put(appEntity.getId(), 0);
			AppClusterEntity appClusterEntity = new AppClusterEntity();
			appClusterEntity.setAppId(appEntity.getId());
			appClusterEntity.setCandAppId(request.getCandAppId());
			appClusterEntity.setAppName(appEntity.getAppName());
			appClusterEntity.setClusterName(request.getClusterName());
			if (StringUtils.isEmpty(appClusterEntity.getClusterName())) {
				appClusterEntity.setClusterName("default");
			}
			appClusterEntity.setEnableSelf(0);
			long appCusterId = appClusterService.save(appClusterEntity);
			if (StringUtils.isEmpty(request.getCandInstanceId())) {
				request.setCandInstanceId("uq_" + UUID.randomUUID().toString().replaceAll("-", "_"));
			}
			mapRs.put(request.getCandInstanceId(), createInstance(appEntity, appCusterId, request));
		});
	}

	/*
	 * 此方法表示将request中的实体属性，赋值给数据中的实体属性 newEntity 表示将要保存的实体 dbEntity 表示数据中当前的实体
	 */
	private void updateInstanceEntity(InstanceEntity newEntity, InstanceEntity dbEntity) {
		dbEntity.setAppClusterId(newEntity.getAppClusterId());
		dbEntity.setAppClusterName(newEntity.getAppClusterName());
		dbEntity.setAppId(newEntity.getAppId());
		dbEntity.setAppName(newEntity.getAppName());
		dbEntity.setCandAppId(newEntity.getCandAppId());
		dbEntity.setCandInstanceId(newEntity.getCandInstanceId());
		dbEntity.setHeartStatus(1);
		dbEntity.setInstanceStatus(1);
		dbEntity.setIp(newEntity.getIp());
		dbEntity.setPort(newEntity.getPort());
		dbEntity.setLan(newEntity.getLan());
		dbEntity.setExtendStatus1(1);
		dbEntity.setExtendStatus2(1);
		dbEntity.setSdkVersion(newEntity.getSdkVersion());
		dbEntity.setServName(newEntity.getServName());
		dbEntity.setTag(newEntity.getTag());
	}

	// 注意此时appname 不能更新
	private InstanceEntity createInstance(AppEntity appEntity, long appCusterId, RegisterInstanceRequest request) {
		InstanceEntity entity = new InstanceEntity();
		entity.setAppClusterId(appCusterId);
		entity.setAppClusterName(request.getClusterName());
		if (StringUtils.isEmpty(entity.getAppClusterName())) {
			entity.setAppClusterName("default");
		}
		entity.setAppId(appEntity.getId());
		entity.setAppName(appEntity.getAppName());
		entity.setCandAppId(request.getCandAppId());
		entity.setCandInstanceId(request.getCandInstanceId());
		entity.setExtendStatus1(1);
		entity.setExtendStatus2(1);
		entity.setHeartStatus(1);
		entity.setInstanceStatus(1);
		entity.setIp(request.getClientIp());
		entity.setPort(request.getPort());
		entity.setLan(request.getLan());
		entity.setPubStatus(soaConfig.getPubStatus());
		entity.setSdkVersion(request.getSdkVersion());
		entity.setServName(request.getServName());
		entity.setSupperStatus(0);
		entity.setTag(JsonUtil.toJsonNull(request.getTag()));
		entity.setWeight(100);
		return entity;
	}

	public void checkRegistVaild(RegisterInstanceRequest request, RegisterInstanceResponse registResponse) {
		if (request == null) {
			registResponse.setSuc(false);
			registResponse.setMsg("registerInstanceRequest不能为空！");
			return;
		}
		if (StringUtils.isEmpty(request.getAppName())) {
			registResponse.setSuc(false);
			registResponse.setMsg("appName不能为空！");
			return;
		}
		if (StringUtils.isEmpty(request.getCandAppId())) {
			registResponse.setSuc(false);
			registResponse.setMsg("candAppId不能为空！");
			return;
		}
		if (StringUtils.isEmpty(request.getClientIp())) {
			registResponse.setSuc(false);
			registResponse.setMsg("clientIp不能为空！");
			return;
		}
		if (StringUtils.isEmpty(request.getLan())) {
			registResponse.setSuc(false);
			registResponse.setMsg("lan不能为空！");
			return;
		}
		if (StringUtils.isEmpty(request.getSdkVersion())) {
			registResponse.setSuc(false);
			registResponse.setMsg("sdkVersion不能为空！");
			return;
		}
		Map<String, String> appNameIdMap = appService.getAppNameIdCacheData();
		
		if (appNameIdMap.containsKey(request.getAppName())) {
			if (!appNameIdMap.get(request.getAppName()).equals(request.getCandAppId())) {
				registResponse.setSuc(false);
				registResponse.setMsg("appName:" + request.getAppName() + " 对应的appid不唯一！");
				return;
			}
		}
		Map<String, String> appIdNameMap = appService.getAppIdNameCacheData();
		if (appIdNameMap.containsKey(request.getCandAppId())) {
			if (!appIdNameMap.get(request.getCandAppId()).equals(request.getAppName())) {
				registResponse.setSuc(false);
				registResponse.setMsg("canAppId:" + request.getCandAppId() + " 对应的appName不唯一！");
				return;
			}
		}
		if (StringUtils.isEmpty(request.getClusterName())) {
			request.setClusterName("default");
		}
	}

	// @Transactional
	public PubDeleteResponse pubDelete(PubDeleteRequest request) {
		PubDeleteResponse response = new PubDeleteResponse();
		response.setSuc(true);
		checkVaild(request, response);
		if (!response.isSuc()) {
			log.info("pubDelete_error：" + response.getMsg() + ",输入参数为：" + JsonUtil.toJsonNull(request));
			return response;
		}
		List<InstanceEntity> data = new ArrayList<>();
		if (!CollectionUtils.isEmpty(request.getCandInstanceIds())) {
			data.addAll(findByCandInstanceId(request.getCandInstanceIds()));
		}
		if (!CollectionUtils.isEmpty(request.getIds())) {
			data.addAll(findByIds(request.getIds()));
		}
		if (data.size() > 0) {
			List<Long> ids = deleteInstance(data);
			response.setInstanceIds(ids);
		}
		return response;
	}

	public List<Long> deleteInstance(List<InstanceEntity> data) {
		List<Long> instanceIds = new ArrayList<>(data.size());
		Map<Long, String> appIds = new HashMap<>();
		data.forEach(t1 -> {
			instanceIds.add(t1.getId());
			appIds.put(t1.getAppId(), "");
			Util.log(log, t1, "is_delete_in_deleteInstance_and_update_version");
		});
		instanceRepository.deleteByIds(instanceIds);
		appService.updateVersionByIds(new ArrayList<>(appIds.keySet()));
		return instanceIds;
	}

	private void checkVaild(PubDeleteRequest request, PubDeleteResponse response) {
		if (request == null) {
			response.setSuc(false);
			response.setMsg("PubDeleteRequest不能为空！");
			return;
		}
		if (CollectionUtils.isEmpty(request.getCandInstanceIds()) && CollectionUtils.isEmpty(request.getIds())) {
			response.setSuc(false);
			response.setMsg("PubDeleteRequest CandInstanceIds or ids不能都为空！");
			return;
		}

	}

	//// @Transactional
	public AdjustResponse adjust(AdjustRequest request) {
		AdjustResponse response = new AdjustResponse();
		response.setSuc(true);
		checkVaild(request, response);
		if (!response.isSuc()) {
			log.info("参数校验不通过：" + response.getMsg() + ",输入参数为：" + JsonUtil.toJsonNull(request));
			return response;
		}
		int count = 0;
		if (!CollectionUtils.isEmpty(request.getCandInstanceIds())) {
			count += request.getCandInstanceIds().size();
		}
		if (!CollectionUtils.isEmpty(request.getIds())) {
			count += request.getIds().size();
		}
		List<InstanceEntity> data = new ArrayList<>(count);
		if (!CollectionUtils.isEmpty(request.getCandInstanceIds())) {
			data.addAll(findByCandInstanceId(request.getCandInstanceIds()));
		}
		if (!CollectionUtils.isEmpty(request.getIds())) {
			data.addAll(findByIds(request.getIds()));
		}
		if (data.size() > 0) {
			List<Long> ids = updatePubStatus(data, request.isUp());
			response.setInstanceIds(ids);
		}
		setDeletedCandInstanceIds(data, request, response);
		return response;
	}

	private void setDeletedCandInstanceIds(List<InstanceEntity> data, AdjustRequest request, AdjustResponse response) {
		Map<String, Long> deletedCandMap = new HashMap<>();
		Map<Long, String> deletedIdMap = new HashMap<>();

		Map<String, Long> candIdsMap = new HashMap<>();
		Map<Long, String> idsMap = new HashMap<>();
		for (InstanceEntity temp : data) {
			candIdsMap.put(temp.getCandInstanceId(), temp.getId());
			idsMap.put(temp.getId(), temp.getCandInstanceId());
		}
		boolean flag1 = CollectionUtils.isEmpty(request.getCandInstanceIds());
		boolean flag2 = CollectionUtils.isEmpty(request.getIds());

		if (!flag1) {
			request.getCandInstanceIds().forEach(t1 -> {
				if (!candIdsMap.containsKey(t1)) {
					deletedCandMap.put(t1, 0L);
				}
			});
		}
		if (!flag2) {
			request.getIds().forEach(t1 -> {
				if (!idsMap.containsKey(t1)) {
					deletedIdMap.put(t1, "");
				}
			});
		}
		response.setNoCandInstanceIds(new ArrayList<>(deletedCandMap.keySet()));
		response.setNoIds(new ArrayList<>(deletedIdMap.keySet()));
	}

	public AdjustSupperStatusResponse adjustSupperStatus(AdjustSupperStatusRequest request) {
		AdjustSupperStatusResponse response = new AdjustSupperStatusResponse();
		response.setSuc(true);
		checkVaild(request, response);
		if (!response.isSuc()) {
			log.info("参数校验不通过：" + response.getMsg() + ",输入参数为：" + JsonUtil.toJsonNull(request));
			return response;
		}
		int count = 0;
		if (!CollectionUtils.isEmpty(request.getCandInstanceIds())) {
			count += request.getCandInstanceIds().size();
		}
		if (!CollectionUtils.isEmpty(request.getIds())) {
			count += request.getIds().size();
		}
		List<InstanceEntity> data = new ArrayList<>(count);
		if (!CollectionUtils.isEmpty(request.getCandInstanceIds())) {
			data.addAll(findByCandInstanceId(request.getCandInstanceIds()));
		}
		if (!CollectionUtils.isEmpty(request.getIds())) {
			data.addAll(findByIds(request.getIds()));
		}
		if (data.size() > 0) {
			List<Long> ids = updateSupperStatus(data, request.getStatus());
			response.setInstanceIds(ids);
		}
		return response;
	}

	private void checkVaild(AdjustSupperStatusRequest request, AdjustSupperStatusResponse response) {
		if (request == null) {
			response.setSuc(false);
			response.setMsg("AdjustSupperRequest不能为空！");
			return;
		}
		if (!(request.getStatus() == 0 || request.getStatus() == 1 || request.getStatus() == -1)) {
			response.setSuc(false);
			response.setMsg("AdjustSupperRequest status只能0或者1！");
			return;
		}
		if (CollectionUtils.isEmpty(request.getCandInstanceIds()) && CollectionUtils.isEmpty(request.getIds())) {
			response.setSuc(false);
			response.setMsg("AdjustSupperRequest CandInstanceIds和Ids不能同时为空！");
			return;
		}
	}

	private List<InstanceEntity> findByIds(List<Long> ids) {
		return instanceRepository.findByIds(ids);
	}

	private void checkVaild(AdjustRequest request, AdjustResponse response) {
		if (request == null) {
			response.setSuc(false);
			response.setMsg("AdjustRequest不能为空！");
			return;
		}
		// if (!(request.getSlot() == 0 || request.getSlot() == 1)) {
		// response.setSuc(false);
		// response.setMsg("AdjustRequest Slot只能0或者1！");
		// return;
		// }
		if (CollectionUtils.isEmpty(request.getCandInstanceIds()) && CollectionUtils.isEmpty(request.getIds())) {
			response.setSuc(false);
			response.setMsg("AdjustRequest CandInstanceIds和Ids不能同时为空！");
			return;
		}
	}

	public GetStatusResponse getStatus(GetStatusRequest request) {
		GetStatusResponse response = new GetStatusResponse();
		response.setSuc(true);
		checkVaild(request, response);
		if (!response.isSuc()) {
			log.info("参数校验不通过：" + response.getMsg() + ",输入参数为：" + JsonUtil.toJsonNull(request));
			return response;
		}
		List<InstanceEntity> data = findByCandInstanceId(request.getCandInstanceIds());
		List<GetStatusInstanceDto> lstRs = new ArrayList<>();
		data.forEach(t1 -> {
			GetStatusInstanceDto instanceDto = new GetStatusInstanceDto();
			instanceDto.setCandInstanceId(t1.getCandInstanceId());
			instanceDto.setId(t1.getId());
			instanceDto.setIp(t1.getIp());
			instanceDto.setPort(t1.getPort());
			instanceDto.setHeartStatus(t1.getHeartStatus());
			instanceDto.setPubStatus(t1.getPubStatus());
			instanceDto.setInstanceStatus(t1.getInstanceStatus());
			instanceDto.setSupperStatus(t1.getSupperStatus());
			if (t1.getSupperStatus() == 0) {
				instanceDto.setStatus(
						(t1.getHeartStatus() == 1 && t1.getPubStatus() == 1 && t1.getInstanceStatus() == 1) ? 1 : 0);
			} else {
				instanceDto.setStatus(t1.getSupperStatus() == 1 ? 1 : 0);
			}
			lstRs.add(instanceDto);
		});
		response.setPubInstances(lstRs);
		return response;
	}

	private void checkVaild(GetStatusRequest request, GetStatusResponse response) {
		if (request == null) {
			response.setSuc(false);
			response.setMsg("GetStatusRequest不能为空！");
			return;
		}

		if (CollectionUtils.isEmpty(request.getCandInstanceIds())) {
			response.setSuc(false);
			response.setMsg("GetStatusRequest CandInstanceIds不能为空！");
			return;
		}

	}

	public AddInstancesResponse addInstance(AddInstancesRequest request) {
		AddInstancesResponse response = new AddInstancesResponse();
		response.setSuc(true);
		checkVaild(request, response);
		if (!response.isSuc()) {
			log.info("参数校验不通过：" + response.getMsg() + ",输入参数为：" + JsonUtil.toJsonNull(request));
			return response;
		}
		List<RegisterInstanceRequest> requests = new ArrayList<>();
		request.getCandInstances().stream().forEach(t1 -> {
			RegisterInstanceRequest registRequest = new RegisterInstanceRequest();
			requests.add(registRequest);
			registRequest.setAppName(request.getAppName());
			registRequest.setCandAppId(request.getCandAppId());
			registRequest.setCandInstanceId(t1.getCandInstanceId());
			registRequest.setClusterName(request.getClusterName());
			registRequest.setLan(request.getLan());
			registRequest.setPort(0);
			registRequest.setClientIp(" ");
		});
		// key为CandInstanceId，value为id
		Map<String, Long> mapRs = addOrupdateInstances(requests);
		List<Long> ids = new ArrayList<>(mapRs.values());
		if (ids.size() > 0) {
			instanceRepository.updateHeartStatusDownFoce(ids, soaConfig.getExpiredTime());
			instanceRepository.updateInstanceStatus(ids, 0);
		}
		return response;
	}

	private void checkVaild(AddInstancesRequest request, AddInstancesResponse response) {
		if (request == null) {
			response.setSuc(false);
			response.setMsg("AddInstancesRequest不能为空！");
			return;
		}
		if (StringUtils.isEmpty(request.getCandAppId())) {
			response.setSuc(false);
			response.setMsg("AddInstancesRequest candAppid不能为空！");
			return;
		}
		if (StringUtils.isEmpty(request.getClusterName())) {
			response.setSuc(false);
			response.setMsg("AddInstancesRequest ClusterName不能为空！");
			return;
		}

		if (CollectionUtils.isEmpty(request.getCandInstances())) {
			response.setSuc(false);
			response.setMsg("AddInstancesRequest CandInstanceIds不能为空！");
			return;
		}
	}

	public void heartBeat(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return;
		}
		instanceRepository.heartBeatBatch(ids);
	}

	//// @Transactional
	public DeRegisterInstanceResponse deRegister(DeRegisterInstanceRequest request) {
		DeRegisterInstanceResponse rsp = new DeRegisterInstanceResponse();
		rsp.setSuc(true);
		if (request == null) {
			rsp.setSuc(false);
			rsp.setMsg("UnRegistRequest 参数不能为空！");
			return rsp;
		}
		InstanceEntity instanceEntity = findById(request.getInstanceId());
		if (instanceEntity == null) {
			rsp.setSuc(false);
			rsp.setMsg("InstanceId在数据库中不存在！");
			log.info(soaConfig.getLogPrefix() + "{}在数据库中不存在！", request.getInstanceId());
			return rsp;
		}
		updateInstanceStatus(Arrays.asList(instanceEntity), false);
		// appService.updateVersionByIds(Arrays.asList(instanceEntity.getAppId()));
		log.info("InstanceId: " + request.getInstanceId() + " unRegist 成功！");
		return rsp;
	}

	public List<InstanceEntity> findExpired(int findExpired) {
		return instanceRepository.findExpired(findExpired);
	}

	public List<InstanceEntity> findOld(int findExpired) {
		return instanceRepository.findOld(findExpired);
	}

	// 返回心跳正常，但是心跳槽位不对的数据
	public List<InstanceEntity> findNoraml(int findExpired) {
		return instanceRepository.findNoraml(findExpired);
	}

	public List<Long> updatePubStatus(List<InstanceEntity> entities, boolean isUp) {
		List<Long> ids = new ArrayList<>(entities.size());
		Map<Long, Integer> idsMap = new HashMap<>();
		if (entities.size() > 0) {
			Map<Long, Integer> appIds = new HashMap<>();
			entities.forEach(t1 -> {
				// ids.add(t1.getId());
				idsMap.put(t1.getId(), 0);
				appIds.put(t1.getAppId(), 0);
			});
			// 为了记录完整的日志分开记录
			String idsJson = JsonUtil.toJsonNull(idsMap.keySet());
			entities.forEach(t1 -> {
				Util.log(log, t1, "_is_update_pub_status_" + isUp + ",and ids is " + idsJson + "_and_update_version");
			});
			ids.addAll(idsMap.keySet());
			instanceRepository.updatePubStatus(ids, isUp ? 1 : 0);
			appService.updateVersionByIds(new ArrayList<>(appIds.keySet()));
		}
		return ids;
	}

	public void updateInstanceStatus(List<InstanceEntity> entities, boolean isUp) {
		// instanceRepository.updateInstanceStatus(ids, isUp ? 1 : 0);
		if (entities.size() > 0) {
			List<Long> ids = new ArrayList<>(entities.size());
			Map<Long, Integer> appIds = new HashMap<>();
			entities.forEach(t1 -> {
				ids.add(t1.getId());
				appIds.put(t1.getAppId(), 0);
				// Util.log(log,t1, "_is_update_instance_status_"+isUp);
			});
			// 为了记录完整的日志分开记录
			String idsJson = JsonUtil.toJsonNull(ids);
			entities.forEach(t1 -> {
				Util.log(log, t1,
						"_is_update_instance_status_" + isUp + ",and ids is " + idsJson + "_and_update_version");
			});
			instanceRepository.updateInstanceStatus(ids, isUp ? 1 : 0);
			appService.updateVersionByIds(new ArrayList<>(appIds.keySet()));
		}
	}

	public void updateHeartStatus(List<InstanceEntity> entities, boolean isUp, int expireTime) {
		if (entities.size() > 0) {
			List<Long> ids = new ArrayList<>(entities.size());
			Map<Long, Integer> appIds = new HashMap<>();
			entities.forEach(t1 -> {
				ids.add(t1.getId());
				appIds.put(t1.getAppId(), 0);
				// Util.log(log,t1, "_is_update_heart_status_"+isUp);
			});
			// 为了记录完整的日志分开记录
			String idsJson = JsonUtil.toJsonNull(ids);
			entities.forEach(t1 -> {
				Util.log(log, t1, "_is_update_heart_status_" + isUp + ",and ids is " + idsJson + "_and_update_version");
			});
			if (isUp) {
				instanceRepository.updateHeartStatusUp(ids, expireTime);
			} else {
				instanceRepository.updateHeartStatusDown(ids, expireTime);
			}
			appService.updateVersionByIds(new ArrayList<>(appIds.keySet()));
		}
	}

	public List<Long> updateSupperStatus(List<InstanceEntity> entities, int status) {
		// instanceRepository.updateSupperStatus(ids, status);
		List<Long> ids = new ArrayList<>(entities.size());
		if (entities.size() > 0) {
			Map<Long, Integer> appIds = new HashMap<>();
			entities.forEach(t1 -> {
				ids.add(t1.getId());
				appIds.put(t1.getAppId(), 0);
				// Util.log(log,t1,"_is_update_supper_status_"+status);
			});
			// 为了记录完整的日志分开记录
			String idsJson = JsonUtil.toJsonNull(ids);
			entities.forEach(t1 -> {
				Util.log(log, t1,
						"_is_update_supper_status_" + status + ",and ids is " + idsJson + "_and_update_version");
			});
			instanceRepository.updateSupperStatus(ids, status);
			appService.updateVersionByIds(new ArrayList<>(appIds.keySet()));
		}
		return ids;
	}

	public void updateAppNames(String appName, String candAppId) {
		List<InstanceEntity> instanceEntities = instanceRepository.findByCandAppId(candAppId);
		if (!CollectionUtils.isEmpty(instanceEntities)) {
			for (InstanceEntity instanceEntity : instanceEntities) {
				instanceRepository.updateAppName(appName, instanceEntity.getId());
			}
		}
	}

	public void deleteById(long instanceId){
		List idList=new ArrayList();
		idList.add(instanceId);
		instanceRepository.deleteByIds(idList);
	}
}
