package org.springframework.cloud.netflix.ribbon.radar;

import java.util.ArrayList;
import java.util.List;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

public class RadarLoadBalancer extends BaseLoadBalancer {
	ServerList<? extends Server> serverList;

	public RadarLoadBalancer(ServerList<? extends Server> serverList, IClientConfig config, IRule rule, IPing ping) {
		super(config, rule, ping);
		this.serverList = serverList;
	}

	@Override
	public List<Server> getReachableServers() {
		List<? extends Server> list = serverList.getUpdatedListOfServers();
		List<Server> rServers = new ArrayList<>(list);
		return rServers;
	}

	@Override
	public List<Server> getAllServers() {
		return getReachableServers();
	}
}
