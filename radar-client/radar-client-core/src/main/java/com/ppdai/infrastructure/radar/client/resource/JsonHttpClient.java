package com.ppdai.infrastructure.radar.client.resource;

import java.io.IOException;

/**
 * Created by zhangyicong on 17-12-12.
 */
public interface JsonHttpClient {
	String post(String url, Object request) throws IOException;

	String get(String url) throws IOException;

	<T> T post(String url, Object request, Class<T> class1) throws IOException;
}
