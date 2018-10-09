package com.ppdai.infrastructure.radar.client.http.util;

import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.ppdai.infrastructure.radar.client.utils.SoaThreadFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

public class HttpUtil {
	private static final DynamicIntProperty MAX_CONNECTION = DynamicPropertyFactory.getInstance().getIntProperty(ConstantEnum.MaxConnection.getName(),
			ConstantEnum.MaxConnection.getValue());
	private static final DynamicIntProperty MAX_ROUTE_CONNECTION = DynamicPropertyFactory.getInstance().getIntProperty(ConstantEnum.MaxRouteConnection.getName(),
			ConstantEnum.MaxRouteConnection.getValue());

	private static final Runnable LOADER = new Runnable() {
		@Override
		public void run() {
			HttpUtil.loadClient();
		}
	};

	private static final AtomicReference<CloseableHttpClient> CLIENT_REF = new AtomicReference<CloseableHttpClient>(
			newClient());
	private static final ScheduledThreadPoolExecutor MANAGER_TIMER = new ScheduledThreadPoolExecutor(
			1,
			SoaThreadFactory.create("HttpUtil", false));

	// cleans expired connections at an interval
	static {
		MAX_CONNECTION.addCallback(LOADER);
		MAX_ROUTE_CONNECTION.addCallback(LOADER);
		MANAGER_TIMER.scheduleWithFixedDelay(() -> {
            try {
                final CloseableHttpClient hc = CLIENT_REF.get();
                if (hc == null){
                    return;
                }
                hc.getConnectionManager().closeExpiredConnections();
            } catch (Throwable t) {

            }
        }, 3, 5, TimeUnit.SECONDS);
	}

	private static final HttpClientConnectionManager newConnectionManager() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(MAX_CONNECTION.get());
		cm.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTION.get());
		return cm;
	}

	public static final CloseableHttpClient getClient() {
		return CLIENT_REF.get();
	}

	private static final void loadClient() {
		final CloseableHttpClient oldClient = CLIENT_REF.get();
		CLIENT_REF.set(newClient());
		if (oldClient != null) {
			MANAGER_TIMER.scheduleWithFixedDelay(() -> {
                try {
                    oldClient.close();
                } catch (Throwable t) {
                }
            }, 3, 5, TimeUnit.SECONDS);
		}

	}

	private static final CloseableHttpClient newClient() {
		// I could statically cache the connection manager but we will probably
		// want to make some of its properties
		// dynamic in the near future also
		RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
		HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(0, false);
		RedirectStrategy redirectStrategy = new RedirectStrategy() {
			@Override
			public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) {
				return false;
			}

			@Override
			public HttpUriRequest getRedirect(HttpRequest httpRequest, HttpResponse httpResponse,
					HttpContext httpContext) {
				return null;
			}
		};
		CloseableHttpClient httpclient = HttpClients.custom().disableContentCompression()
				.setConnectionManager(newConnectionManager()).setDefaultRequestConfig(config)
				.setRetryHandler(retryHandler).setRedirectStrategy(redirectStrategy).disableCookieManagement().build();
		return httpclient;
	}

	public static RequestConfig buildRequestConfig(String serviceName, String path) {
		path = path.substring(1,path.length()).replace('/', '.');
		RequestConfig.Builder builder = RequestConfig.custom();

		int connectTimeout = DynamicPropertyFactory.getInstance()
				.getIntProperty(serviceName + "." + path + ".connect.timeout", 0).get();
		if (connectTimeout == 0) {
			connectTimeout = DynamicPropertyFactory.getInstance().getIntProperty(serviceName + ".connect.timeout", 0)
					.get();
		}
		if (connectTimeout == 0) {
			connectTimeout = DynamicPropertyFactory.getInstance().getIntProperty("soa.connect.timeout.global", 1000)
					.get();
		}
		builder.setConnectTimeout(connectTimeout);

		int socketTimeout = DynamicPropertyFactory.getInstance()
				.getIntProperty(serviceName + "." + path + ".socket.timeout", 0).get();
		if (socketTimeout == 0) {
			socketTimeout = DynamicPropertyFactory.getInstance().getIntProperty(serviceName + ".socket.timeout", 0)
					.get();
		}
		if (socketTimeout == 0) {
			socketTimeout = DynamicPropertyFactory.getInstance().getIntProperty("soa.socket.timeout.global", 10000)
					.get();
		}
		
		builder.setSocketTimeout(socketTimeout);
		int requestConnectionTimeout = DynamicPropertyFactory.getInstance()
				.getIntProperty(serviceName + "." + path + ".request.connect.timeout", 0).get();
		if (requestConnectionTimeout == 0) {
			requestConnectionTimeout = DynamicPropertyFactory.getInstance()
					.getIntProperty(serviceName + ".request.connect.timeout", 0).get();
		}
		if (requestConnectionTimeout == 0) {
			requestConnectionTimeout = DynamicPropertyFactory.getInstance()
					.getIntProperty("soa.request.connect.timeout.global", 2000).get();
		}
		builder.setConnectionRequestTimeout(requestConnectionTimeout);
		return builder.build();
	}
}
