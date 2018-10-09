package org.springframework.cloud.netflix.ribbon.radar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;
import com.ppdai.infrastructure.radar.client.DiscoveryClient;
import com.ppdai.infrastructure.radar.client.dto.RadarApp;
import com.ppdai.infrastructure.radar.client.dto.RadarCluster;
import com.ppdai.infrastructure.radar.client.event.AppChangedEvent;
import com.ppdai.infrastructure.radar.client.event.AppChangedListener;

public class RadarServerList implements ServerList<RadarServer> {

	AtomicReference<List<RadarServer>> radarRef = new AtomicReference<>(new ArrayList<>());
	// private IClientConfig config;
	private String appId = "";
	private String clusterName = "";

	public RadarServerList(IClientConfig config, String clusterName) {
		// this.config = config;
		this.appId = config.getClientName().toLowerCase();
		this.clusterName = clusterName;
		DiscoveryClient.getInstance().addAppChangedListener(new AppChangedListener() {
			@Override
			public void onAppChanged(AppChangedEvent changeEvent) {
				updateRadar(changeEvent.getChangedEvent());
			}
		});
	}

	private void updateRadar(Map<String, RadarApp> radarMap) {
		if (radarMap != null && radarMap.containsKey(appId)) {
			RadarApp radarApp = radarMap.get(appId);
			updateRadar(radarApp);
		}

	}

	private void updateRadar(RadarApp radarApp) {
		List<RadarServer> radarServers = new ArrayList<>();
		if (radarApp != null && radarApp.getClusters() != null) {
			if (radarApp.getAllowCross() == 0) {
				if (radarApp.getClusters().containsKey(clusterName)) {
					RadarCluster radarCluster = radarApp.getClusters().get(clusterName);
					if (radarCluster.getInstances() != null) {
						radarCluster.getInstances().forEach(t2 -> {
							RadarServer radarServer = new RadarServer(t2.getHost(), t2.getPort(), t2);
							radarServer.setAlive(true);
							radarServers.add(radarServer);
						});
					}
				}

			} else {
				radarApp.getClusters().entrySet().forEach(t1 -> {
					if (t1.getValue().getInstances() != null) {
						t1.getValue().getInstances().forEach(t2 -> {
							RadarServer radarServer = new RadarServer(t2.getHost(), t2.getPort(), t2);
							radarServer.setAlive(true);
							radarServers.add(radarServer);
						});
					}
				});
			}
		}
		radarRef.set(radarServers);
	}

	@Override
	public List<RadarServer> getInitialListOfServers() {
		return getRadarServer();
	}

	private List<RadarServer> getRadarServer() {
		if (radarRef.get().isEmpty()) {
			RadarApp radarApp = DiscoveryClient.getInstance().getApp(appId);
			updateRadar(radarApp);
		}
		return radarRef.get();
	}

	@Override
	public List<RadarServer> getUpdatedListOfServers() {
		return getRadarServer();
	}

}
