package com.ppdai.infrastructure.radar.biz.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ppdai.infrastructure.radar.biz.dal.AppClientRepository;
import com.ppdai.infrastructure.radar.biz.entity.AppClientEntity;

@Service
public class AppClientService {
	@Autowired
	private AppClientRepository appClientRepository;
	private AtomicReference<Map<String, String>> cache = new AtomicReference<Map<String, String>>(
			new ConcurrentHashMap<>(1000));

	private Map<String, String> getCache() {
		return cache.get();
	}

	private String getUq(AppClientEntity t1) {
		return String.format("%s_%s_%s", t1.getProviderCandAppId(), t1.getConsumerClusterName(),
				t1.getConsumerCandAppId());
	}

	public void registerClient(String consumerAppId, String culsterName, List<String> providerAppIds) {
		List<AppClientEntity> rs = new ArrayList<>();
		providerAppIds.forEach(t1 -> {
			AppClientEntity appClientEntity = new AppClientEntity();
			appClientEntity.setConsumerCandAppId(consumerAppId);
			appClientEntity.setConsumerClusterName(culsterName);
			appClientEntity.setProviderCandAppId(t1);
			String uq = getUq(appClientEntity);
			if (!getCache().containsKey(uq)) {
				rs.add(appClientEntity);
			}
		});
		if (rs.size() > 0) {
			List<AppClientEntity> data = appClientRepository.findByConsumerCandAppId(consumerAppId);
			data.forEach(t1 -> {
				String uq = getUq(t1);
				if (!getCache().containsKey(uq)) {
					getCache().put(uq, "");
				}
			});
			List<AppClientEntity> rs1 = new ArrayList<>();
			rs.forEach(t1 -> {
				String uq = getUq(t1);
				if (!getCache().containsKey(uq)) {
					rs1.add(t1);
				}
			});
			if (rs1.size() > 0) {
				rs1.forEach(t1 -> {
					try {
						appClientRepository.insert(t1);
						getCache().put(getUq(t1), "");
					} catch (Exception e) {
						// TODO: handle exception
					}
				});
			}
		}
	}
}
