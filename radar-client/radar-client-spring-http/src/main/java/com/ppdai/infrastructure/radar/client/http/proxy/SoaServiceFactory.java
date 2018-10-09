package com.ppdai.infrastructure.radar.client.http.proxy;

import java.util.ArrayList;
import java.util.List;

import com.ppdai.infrastructure.radar.client.http.common.annotation.SoaService;

public class SoaServiceFactory {
	private static List<String> appLst=new ArrayList<>();
	public static ISoaService create(Class<?> serviceInterface) {
		SoaService soaService = serviceInterface.getAnnotation(SoaService.class);
		if(soaService==null){
			throw new RuntimeException("此对象"+serviceInterface.getName()+"不含SoaService注解");
		}
		ServiceMeta serviceMeta=new ServiceMeta(soaService.appId(), soaService.appName());
		appLst.add(serviceMeta.getAppId());
		//appLst.add();
	    return new ServiceProxyImpl(serviceMeta);
	}
	public static List<String> getProviderApp(){
		return appLst;
	}
}
