package com.ppdai.infrastructure.radar.client.resource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.JsonUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by zhangyicong on 17-12-12.
 */
public class JsonHttpClientImpl implements JsonHttpClient {
	private static final Logger logger = LoggerFactory.getLogger(JsonHttpClientImpl.class);
	private static final MediaType JSONTYPE = MediaType.parse("application/json; charset=utf-8");

	private OkHttpClient client;
	
	public JsonHttpClientImpl(Long connTimeout, Long readTimeout) {		
		client = new OkHttpClient.Builder().connectTimeout(connTimeout, TimeUnit.MILLISECONDS)
				.readTimeout(readTimeout, TimeUnit.MILLISECONDS).build();
	}

	
	public JsonHttpClientImpl() {
		this(35000L, 35000L);
	}

	@Override
	public String post(String url, Object reqObj) throws IOException {
		String json = "";
		if (reqObj != null) {
			json = JsonUtil.toJsonNull(reqObj);
		}
		
		Response response = null;
		Transaction transaction = Tracer.newTransaction("radar-client", url);
		long start = System.nanoTime();
		try {
			RequestBody body = RequestBody.create(JSONTYPE, json);
			Request request = new Request.Builder().url(url).post(body).build();
			response = client.newCall(request).execute();
			// transaction.complete();
			if (transaction != null) {
				transaction.setStatus(Transaction.SUCCESS);
			}
			return response.body().string();
		} catch (Exception e) {
			logger.error("访问" + url + "异常,access_error,and json is " + json, e);
			logger.info("spend " + (System.nanoTime() - start) / 10000000 + "秒");
			if (transaction != null) {
				transaction.setStatus(e);
			}
			throw e;
		} finally {
			if (transaction != null) {
				transaction.complete();
			}
			try {
				if (response != null) {
					response.close();
				}
			} catch (Exception e) {

			}
		}
	}

	@Override
	public <T> T post(String url, Object request, Class<T> class1) throws IOException {
		String rs = post(url, request);
		if (rs == null || rs.length() == 0 || rs.trim().length() == 0) {
			return null;
		} else {
			return JsonUtil.parseJson(rs, class1);
		}
	}

	@Override
	public String get(String url) throws IOException {			
		Request request = new Request.Builder().url(url).get().build();
		Response response = null;
		Transaction transaction = Tracer.newTransaction("radar-client", url);
		long start = System.nanoTime();
		try {
			response = client.newCall(request).execute();
			// transaction.complete();
			if (transaction != null) {
				transaction.setStatus(Transaction.SUCCESS);
			}
			return response.body().string();
		} catch (Exception e) {
			logger.error("访问" + url + "异常", e);
			logger.info("spend " + (System.nanoTime() - start) / 10000000 + "秒");
			if (transaction != null) {
				transaction.setStatus(e);
			}
			throw e;
		} finally {
			if (transaction != null) {
				transaction.complete();
			}
			try {
				if (response != null) {
					response.close();
				}
			} catch (Exception e) {

			}
		}
	}
}
