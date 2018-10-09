package com.ppdai.infrastructure.radar.client.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ppdai.infrastructure.radar.biz.common.util.IPUtil;
import com.ppdai.infrastructure.radar.client.DiscoveryClient;
import com.ppdai.infrastructure.radar.client.config.RadarClientConfig;
import com.ppdai.infrastructure.radar.client.dto.RadarInstance;
import com.ppdai.infrastructure.radar.client.event.PreRegisterListener;
import com.ppdai.infrastructure.radar.client.event.RegisterCompletedListener;

@Component
public class RadarClientBootstrapListener
		implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware, Ordered {
	private static final Logger logger = LoggerFactory.getLogger(RadarClientBootstrapListener.class);

	private RadarClientConfig radarClientConfig;
	@Autowired
	private Environment env;

	@Autowired
	private EnvProp envProp;

	private List<PreRegisterListener> preRegisters;
	private List<RegisterCompletedListener> comletedRegisters;

	private static AtomicBoolean isStartUp = new AtomicBoolean(false);
	private ApplicationContext applicationContext;

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		initConfig();
		if (radarClientConfig.isRegisterSelf()) {
			if (isStartUp.compareAndSet(false, true)) {
				try {
					RadarInstance radarInstance = initRadarInstance();
					DiscoveryClient.getInstance().setConfig(radarClientConfig);
					DiscoveryClient.getInstance().register(radarInstance);
					fireRegisterCompletedEvent();					
					logger.info("register_suc,注册成功");
				} catch (Exception e) {
					logger.error("register_fail,注册失败", e);
					throw e;
				}
				
			}
		} else {
			DiscoveryClient.getInstance().setConfig(radarClientConfig);
		}
	}

	private RadarInstance initRadarInstance() {
		Map<String, String> tagMap = new HashMap<>();
		Map<String, Object> tagsEnv = envProp.getEnv("radar.instance.tag");
		tagsEnv.entrySet().forEach(t1 -> {
			tagMap.put(t1.getKey().replaceAll("radar.instance.tag.", ""), t1.getValue().toString());
		});
		RadarInstance.Builder radarInstanceBuilder = RadarInstance.getBuilder()
				.withAppName(radarClientConfig.getAppName()).withCandAppId(radarClientConfig.getCandAppId())
				.withCandInstanceId(radarClientConfig.getCandInstanceId())
				.withClusterName(radarClientConfig.getClusterName()).withHost(radarClientConfig.getHost())
				.withPort(radarClientConfig.getPort()).withTags(tagMap);
		firePreRegisterEvent(radarInstanceBuilder);
		return radarInstanceBuilder.build();
	}

	private void firePreRegisterEvent(RadarInstance.Builder radarInstanceBuilder) {
		try {
			Map<String, PreRegisterListener> preMap= applicationContext.getBeansOfType(PreRegisterListener.class);
			if(preMap!=null&&preMap.size()>0){
				preRegisters=new ArrayList<>(preMap.values());
			}
		} catch (Exception e) {
			
		}
		if (preRegisters != null) {
			preRegisters.forEach(t1->{
				try{
				   t1.onPreRegister(radarInstanceBuilder);
				}catch (Exception e) {
					logger.error("preRegisters_error",e);
				}
			});			
			synRadarConfig(radarInstanceBuilder.build());
		}
	}
	private void fireRegisterCompletedEvent() {
		try {
			Map<String, RegisterCompletedListener> completedMap= applicationContext.getBeansOfType(RegisterCompletedListener.class);
			if(completedMap!=null&&completedMap.size()>0){
				comletedRegisters=new ArrayList<>(completedMap.values());
			}
		} catch (Exception e) {
			
		}
		if (comletedRegisters != null) {
			comletedRegisters.forEach(t1->{
				try{
				   t1.onCompleted();
				}catch (Exception e) {
					logger.error("RegisterCompleted_error",e);
				}
			});		
			
		}
	}

	private void synRadarConfig(RadarInstance instance) {
		radarClientConfig.setAppName(instance.getAppName());
		radarClientConfig.setCandAppId(instance.getCandAppId());
		radarClientConfig.setCandInstanceId(instance.getCandInstanceId());
		radarClientConfig.setClusterName(instance.getClusterName());
		radarClientConfig.setHost(instance.getHost());
		radarClientConfig.setPort(instance.getPort());
		radarClientConfig.setTags(instance.getTags());
		
	}

	private void initConfig() {
		// 用户也可以自己注入实现配置信息，如果没有自行注册配置，则读取spring Environment 中的配置信息
		if (radarClientConfig == null) {
			try {
				radarClientConfig = applicationContext.getBean(RadarClientConfig.class);
			} catch (Exception e) {
				logger.info("RadarClientConfig is not register as a bean");
			}
			if (radarClientConfig == null) {
				logger.info("radarClientConfig is null");
				String rgUrl = env.getProperty("radar.register.url");				
				radarClientConfig = new RadarClientConfig(rgUrl);
				String connectionTimeOut = env.getProperty("radar.register.connectionTimeOut", "35");
				radarClientConfig.setConnectionTimeout(Integer.parseInt(connectionTimeOut));

				String readTimeOut = env.getProperty("radar.register.readTimeOut", "35");
				radarClientConfig.setReadTimeout(Integer.parseInt(readTimeOut));

				String candInstanceId = env.getProperty("radar.instance.candInstanceId");
				radarClientConfig.setCandInstanceId(candInstanceId);
				String candAppId =env.getProperty("radar.instance.appId",env.getProperty("com.ppdai.appId"));
				radarClientConfig.setCandAppId(candAppId);
				String appName = env.getProperty("radar.instance.name", env.getProperty("spring.application.name"));
				radarClientConfig.setAppName(appName);

				String host = env.getProperty("radar.instance.host", "");
				radarClientConfig.setHost(host);
				
				String port = env.getProperty("server.port", "8080");
				radarClientConfig.setPort(Integer.parseInt(port));
				String clusterName = env.getProperty("radar.instance.clusterName",env.getProperty("com.ppdai.cluster"));
				radarClientConfig.setClusterName(clusterName);
				
				if("false".equals(env.getProperty("radar.instance.registerSelf", "true"))){
					radarClientConfig.setRegisterSelf(false);
				}				
			}			
		}		
		if(StringUtils.isEmpty(radarClientConfig.getRegistryUrl())){
			throw new RuntimeException("radar.register.url is null");
		}
		if (radarClientConfig.isRegisterSelf()) {			
			//String netCard = env.getProperty("radar.netCard", "");
			if (StringUtils.isEmpty(radarClientConfig.getHost())) {
				String netCard=env.getProperty("radar.network.netCard","");
				radarClientConfig.setHost(IPUtil.getLocalIP(netCard));
			}
			if (StringUtils.isEmpty(radarClientConfig.getHost())) {
				throw new RuntimeException("radar.instance.host is null or radar.netCard is null or radarClientConfig.host is null");
			}
			if(StringUtils.isEmpty(radarClientConfig.getCandAppId())){
				throw new RuntimeException("radar.instance.appId is null");
			}
			if(StringUtils.isEmpty(radarClientConfig.getAppName())){
				throw new RuntimeException("radar.instance.name or spring.application.name is null");
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}	
	
}
