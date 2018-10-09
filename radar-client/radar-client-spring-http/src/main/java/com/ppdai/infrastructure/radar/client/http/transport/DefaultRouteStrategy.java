package com.ppdai.infrastructure.radar.client.http.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ppdai.infrastructure.radar.client.DiscoveryClient;
import com.ppdai.infrastructure.radar.client.dto.RadarApp;
import com.ppdai.infrastructure.radar.client.dto.RadarCluster;
import com.ppdai.infrastructure.radar.client.dto.RadarInstance;
import com.ppdai.infrastructure.radar.client.http.proxy.ServiceMeta;

@Component
public class DefaultRouteStrategy implements IRouteStrategy {

	@Autowired
	private Environment env;

	@Override
	public String getRoute(ServiceMeta serviceMeta, Map<String, String> header) {
		String clusterName = env.getProperty("radar.instance.clusterName", "default");
		RadarApp radarApp = DiscoveryClient.getInstance().getApp(serviceMeta.getAppId());
		if (radarApp == null || radarApp.getClusters() == null || radarApp.getClusters().size() == 0) {
			throw new RuntimeException(String.format("目标服务%s不存在！", serviceMeta.getAppId()));
		}
		Map<String, RadarCluster> radarClusters = radarApp.getClusters();
		if (radarApp.getAllowCross() == 0) {
			checkCluster(serviceMeta, radarApp, clusterName);
			RadarCluster radarCluster = radarClusters.get(clusterName);
			List<RadarInstance> radarInstances = radarCluster.getInstances();
			if (CollectionUtils.isEmpty(radarInstances)) {
				throw new RuntimeException(
						String.format("目标服务%s，子环境（%s）,没有实例！", serviceMeta.getAppId(), radarCluster.getClusterName()));
			}
			return getRoute(radarInstances);
		} else {
			List<RadarInstance> radarInstances = new ArrayList<>();
			radarClusters.values().forEach(t1 -> {
				if (!CollectionUtils.isEmpty(t1.getInstances())) {
					radarInstances.addAll(t1.getInstances());
				}
			});
			if (CollectionUtils.isEmpty(radarInstances)) {
				throw new RuntimeException(String.format("目标服务%s，没有实例！", serviceMeta.getAppId()));
			}
			return getRoute(radarInstances);

		}

	}

	private void checkCluster(ServiceMeta serviceMeta, RadarApp radarApp, String clusterName) {
		if (!radarApp.getClusters().containsKey(clusterName)) {
			throw new RuntimeException(String.format("目标服务%s，子环境%s不存在！", serviceMeta.getAppId(), clusterName));
		}
	}

	private String getRoute(List<RadarInstance> instanceDtos) {
		int total = 0;
		for (RadarInstance instanceDto : instanceDtos) {
			total += instanceDto.getWeight();
		}
		if (total == 0) {
			int random = RandomUtils.nextInt(instanceDtos.size());
			return "http://" + instanceDtos.get(random).getHost() + ":" + instanceDtos.get(random).getPort();
		} else {
			float random = RandomUtils.nextFloat();
			int temp = 0;
			for (RadarInstance instanceDto : instanceDtos) {
				// temp+=instanceDto.getWeight();
				if (random > temp / total && random <= (temp + instanceDto.getWeight()) / total) {
					return "http://" + instanceDto.getHost() + ":" + instanceDto.getPort();
				}
				temp += instanceDto.getWeight();
			}
		}
		return "";
	}

}
