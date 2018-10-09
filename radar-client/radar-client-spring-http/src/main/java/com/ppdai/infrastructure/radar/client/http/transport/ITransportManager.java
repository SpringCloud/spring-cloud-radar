package com.ppdai.infrastructure.radar.client.http.transport;

import java.util.Map;

import com.ppdai.infrastructure.radar.client.http.proxy.ServiceMeta;

public interface ITransportManager {
	<TResponse> TResponse request(Class<TResponse> resp, String path, Object data, Map<String, String> header,
			ServiceMeta serviceMeta);
}
