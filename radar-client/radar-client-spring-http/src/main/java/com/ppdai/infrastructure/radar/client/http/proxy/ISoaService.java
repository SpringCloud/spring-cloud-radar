package com.ppdai.infrastructure.radar.client.http.proxy;

import java.util.Map;

public interface ISoaService {
	<TResponse> TResponse request(Class<TResponse> resp, String path, Object data,Map<String, String> header);
	<TResponse> TResponse request(Class<TResponse> resp, String path, Object data);
}
