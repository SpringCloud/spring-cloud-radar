package org.springframework.cloud.netflix.ribbon.radar;

import com.netflix.loadbalancer.Server;
import com.ppdai.infrastructure.radar.client.dto.RadarInstance;

public class RadarServer extends Server {

	private MetaInfo metaInfo;
	private RadarInstance radarInstance;

	public RadarServer(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public RadarServer(String id, int port, RadarInstance radarInstance) {
		super(id, port);
		this.radarInstance = radarInstance;
		this.metaInfo = new MetaInfo() {

			@Override
			public String getServiceIdForDiscovery() {
				// TODO Auto-generated method stub
				return radarInstance.getCandAppId();
			}

			@Override
			public String getServerGroup() {
				// TODO Auto-generated method stub
				return radarInstance.getClusterName();
			}

			@Override
			public String getInstanceId() {
				// TODO Auto-generated method stub
				return radarInstance.getCandInstanceId();
			}

			@Override
			public String getAppName() {
				// TODO Auto-generated method stub
				return radarInstance.getAppName();
			}
		};
	}

	public MetaInfo getMetaInfo() {
		return metaInfo;
	}

	public RadarInstance getRadarInstance() {
		return radarInstance;
	}
}
