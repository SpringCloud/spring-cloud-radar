package com.ppdai.infrastructure.radar.client.resource;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;
import com.ppdai.infrastructure.radar.biz.dto.BaseResponse;
import com.ppdai.infrastructure.radar.biz.dto.RadarConstanst;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.DeRegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppMetaRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppMetaResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.GetAppResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.HeartBeatRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.HeartBeatResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterClientRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterClientResponse;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceRequest;
import com.ppdai.infrastructure.radar.biz.dto.client.RegisterInstanceResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstancesRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AddInstancesResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.AdjustResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.GetStatusResponse;
import com.ppdai.infrastructure.radar.biz.dto.pub.PubDeleteRequest;
import com.ppdai.infrastructure.radar.biz.dto.pub.PubDeleteResponse;
import com.ppdai.infrastructure.radar.client.metric.MetricSingleton;

/**
 * Created by zhangyicong on 17-12-12.
 */
public class RadarResourceIml implements RadarResource {

	private static final Logger logger = LoggerFactory.getLogger(RadarResourceIml.class);

	private static final String METRIC_APITIME_NAME = "radarclient.api.time";
	private static final String METRIC_APIERR_NAME = "radarclient.api.error";

	private JsonHttpClient httpClient;
	private String registryServer;
	private String[] registryUrls = null;
	private MetricRegistry metricRegistry;
	private AtomicInteger count = new AtomicInteger(0);

	public RadarResourceIml(String registryServer, JsonHttpClient httpClient) {
		this.registryServer = registryServer;
		this.httpClient = httpClient;
		this.metricRegistry = MetricSingleton.getMetricRegistry();
		initRegistryUrls();
	}

	private void initRegistryUrls() {
		registryUrls = registryServer.split(",");
	}

	public RadarResourceIml(String registryServer) {
		this(registryServer, new JsonHttpClientImpl());
	}

	private void checkResponse(Object request, BaseResponse response, String url, String api) throws IOException {
		if (response != null && !response.isSuc()) {
			if (metricRegistry != null) {
				metricRegistry.counter(METRIC_APIERR_NAME + "?api=" + api).inc();
			}
			if (RadarConstanst.NO.equals(response.getCode())) {
				throw new IOException("failed to post " + url + ", request: " + JsonUtil.toJsonNull(request)
						+ ", response: " + JsonUtil.toJsonNull(response));
			}
		}
	}

	private String getRegistryUrl() {
		if (count.get() >= Integer.MAX_VALUE) {
			count.set(0);
		}
		return registryUrls[count.incrementAndGet() % registryUrls.length];
	}

	@Override
	public RegisterInstanceResponse registerInstance(RegisterInstanceRequest request) throws Exception {
		String api = "registerInstance";
		String url = getRegistryUrl() + RadarConstanst.INSTPRE + "/registerInstance";
		return post(api, url, request, RegisterInstanceResponse.class);
	}

	@Override
	public DeRegisterInstanceResponse deRegisterInstance(DeRegisterInstanceRequest request) throws Exception {
		String api = "deRegisterInstance";
		String url = getRegistryUrl() + RadarConstanst.INSTPRE + "/deRegisterInstance";
		return post(api, url, request, DeRegisterInstanceResponse.class);
	}

	@Override
	public RegisterClientResponse registerClient(RegisterClientRequest request) throws Exception {
		String api = "registerClient";
		String url = getRegistryUrl() + RadarConstanst.APPPRE + "/registerClient";
		return post(api, url, request, RegisterClientResponse.class);
	}

	@Override
	public HeartBeatResponse heartbeat(HeartBeatRequest request) throws Exception {
		String api = "heartbeat";
		String url = getRegistryUrl() + RadarConstanst.INSTPRE + "/heartbeat";
		return post(api, url, request, HeartBeatResponse.class);
	}

	@Override
	public GetAppResponse getApp(GetAppRequest request) throws Exception {
		String api = "getApp";
		String url = getRegistryUrl() + RadarConstanst.APPPRE + "/getApp";
		return post(api, url, request, GetAppResponse.class);
	}

	@Override
	public AdjustResponse adjust(AdjustRequest request) throws Exception {
		String api = "adjust";
		String url = getRegistryUrl() + RadarConstanst.PUBPRE + "/adjust";
		return post(api, url, request, AdjustResponse.class);
	}

	@Override
	public GetAppResponse getAppPolling(GetAppRequest request) throws Exception {
		String api = "getAppPolling";
		String url = getRegistryUrl() + RadarConstanst.APPPRE + "/getAppPolling";
		return post(api, url, request, GetAppResponse.class);
	}

	@Override
	public GetAppMetaResponse getAppMeta(GetAppMetaRequest request) throws Exception {
		String api = "getAppMeta";
		String url = getRegistryUrl() + RadarConstanst.APPPRE + "/getAppMeta";
		return post(api, url, request, GetAppMetaResponse.class);
	}

	private <T extends BaseResponse> T post(String api, String url, Object request, Class<T> class1) throws Exception {
		T response;
		Timer.Context timer = null;
		if (metricRegistry != null) {
			timer = metricRegistry.timer(METRIC_APITIME_NAME + "?api=" + api).time();
		}
		try {
			response = httpClient.post(url, request, class1);
		} catch (Exception e) {
			if (metricRegistry != null) {
				metricRegistry.counter(METRIC_APIERR_NAME + "?api=" + api).inc();
			}
			throw new IOException("failed to post " + url + ", request: " + JsonUtil.toJsonNull(request) + ", error: "
					+ e.getMessage(), e);
		} finally {
			if (timer != null) {
				timer.stop();
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("post: {}, request: {}, response: {}", url, JsonUtil.toJsonNull(request),
					JsonUtil.toJsonNull(response));
		}
		checkResponse(request, (BaseResponse) response, url, api);
		return response;
	}

	@Override
	public AddInstancesResponse addInstance(AddInstancesRequest request) throws Exception {
		String api = "addInstance";
		String url = getRegistryUrl() + RadarConstanst.PUBPRE + "/addInstance";
		return post(api, url, request, AddInstancesResponse.class);
	}

	@Override
	public GetStatusResponse getStatus(GetStatusRequest request) throws Exception {
		String api = "getStatus";
		String url = getRegistryUrl() + RadarConstanst.PUBPRE + "/getStatus";
		return post(api, url, request, GetStatusResponse.class);
	}

	@Override
	public PubDeleteResponse pubDelete(PubDeleteRequest request) throws Exception {
		String api = "pubDel";
		String url = getRegistryUrl() + RadarConstanst.PUBPRE + "/pubDel";
		return post(api, url, request, PubDeleteResponse.class);
	}

	@Override
	public void hs() {
		String url = getRegistryUrl();
		try {
			httpClient.get(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
