package com.ppdai.infrastructure.radar.client.http.transport;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.client.DiscoveryClient;
import com.ppdai.infrastructure.radar.client.http.proxy.ServiceMeta;
import com.ppdai.infrastructure.radar.client.http.util.HttpUtil;

@Component
public class DefaultTransportManager implements ITransportManager {
	private Logger logger = LoggerFactory.getLogger(DefaultTransportManager.class);

	@Autowired
	private IRouteStrategy routeStrategyManager;

	@Override
	public <TResponse> TResponse request(Class<TResponse> resp, String path, Object data, Map<String, String> header,
										 ServiceMeta serviceMeta) {
		RequestConfig requestConfig = HttpUtil
				.buildRequestConfig(DiscoveryClient.getInstance().getConfig().getAppName(), path);
		String url = routeStrategyManager.getRoute(serviceMeta, header) + path;
		HttpPost httpUriRequest = new HttpPost(url);
		httpUriRequest.setConfig(requestConfig);
		StringEntity entity = null;
		try {
			entity = new StringEntity(JsonUtil.toJson(data), "utf-8");
			entity.setContentEncoding("UTF-8");
			entity.setContentType("application/json");
			httpUriRequest.setEntity(entity);
		} catch (UnsupportedCharsetException e1) {
			logger.error("UnsupportedCharsetException", e1);
		}
		if (header != null) {
			for (Map.Entry<String, String> entry : header.entrySet()) {
				httpUriRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}
		try {
			CloseableHttpResponse response = forward(httpUriRequest, serviceMeta, path);// HttpUtil.getClient().execute(httpUriRequest);
			if (response != null && response.getStatusLine().getStatusCode() == 200) {
				HttpEntity he = response.getEntity();
				String respContent = EntityUtils.toString(he, "UTF-8");
				TResponse result = JsonUtil.parseJson(respContent, resp);
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	

	private CloseableHttpResponse forward(HttpPost httpUriRequest, ServiceMeta serviceMeta, String path)
			throws ClientProtocolException, IOException {
		// if
		// (SoaDiscoveryClient.getMapApp(serviceMeta.getAppId()).containsKey(serviceMeta.getAppId()))
		// {
		// ApplicationDto applicationDto =
		// SoaDiscoveryClient.getMapApp(serviceMeta.getAppId()).get(serviceMeta.getAppId());
		// if (applicationDto.getApplicationClient()!=null) {
		// if (applicationDto.getApplicationClient().isOpenBreak()) {
		// String key = path.replace('/', '.');
		// return new SoaCommandForSemaphoreIsolation(HttpUtil.getClient(),
		// httpUriRequest,
		// defaultServerProvider.getServiceName(),
		// defaultServerProvider.getServiceName()+key).execute();
		// }
		// }
		// }
		return HttpUtil.getClient().execute(httpUriRequest);
	}

}
