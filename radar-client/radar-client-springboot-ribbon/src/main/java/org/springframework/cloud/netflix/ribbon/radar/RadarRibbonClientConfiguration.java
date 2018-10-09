package org.springframework.cloud.netflix.ribbon.radar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.DefaultServerIntrospector;
import org.springframework.cloud.netflix.ribbon.PropertiesFactory;
import org.springframework.cloud.netflix.ribbon.ServerIntrospector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

@Configuration
public class RadarRibbonClientConfiguration {

	@Value("${ribbon.client.name}")
	private String serviceId = "client";

	@Autowired
	private Environment env;

	@Autowired
	private PropertiesFactory propertiesFactory;

	public RadarRibbonClientConfiguration() {
	}

	@Bean
	@ConditionalOnMissingBean
	public ServerList<RadarServer> radarRibbonServerList(IClientConfig config) {
		String clusterName = env.getProperty("radar.instance.clusterName", "default");
		RadarServerList serverList = new RadarServerList(config, clusterName);
		return serverList;
	}

	@Bean
	public ILoadBalancer createLoadBalancer(ServerList<? extends Server> serverList, IClientConfig config, IRule rule,
			IPing ping) {
		if (this.propertiesFactory.isSet(ILoadBalancer.class, serviceId)) {
			return this.propertiesFactory.get(ILoadBalancer.class, config, serviceId);
		}
		ILoadBalancer iLoadBalancer = new RadarLoadBalancer(serverList, config, rule, ping);
		return iLoadBalancer;
	}

	@Bean
	public IRule createRule(IClientConfig config) {
		if (this.propertiesFactory.isSet(IRule.class, serviceId)) {
			return this.propertiesFactory.get(IRule.class, config, serviceId);
		}
		// return new RadarRandomRule();
		return new RoundRobinRule();
	}

	@Bean
	public IPing healthCheckingRule() {
		return new IPing() {
			@Override
			public boolean isAlive(Server server) {
				// TODO Auto-generated method stub
				return true;
			}
		};
	}

	@Bean
	public ServerIntrospector serverIntrospector() {
		return new DefaultServerIntrospector();
	}

}
