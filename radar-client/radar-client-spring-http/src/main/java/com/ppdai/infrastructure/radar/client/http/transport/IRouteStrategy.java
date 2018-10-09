package com.ppdai.infrastructure.radar.client.http.transport;

import java.util.Map;

import com.ppdai.infrastructure.radar.client.http.proxy.ServiceMeta;

public interface IRouteStrategy {
	String getRoute(ServiceMeta serviceMeta, Map<String, String> header);
}
