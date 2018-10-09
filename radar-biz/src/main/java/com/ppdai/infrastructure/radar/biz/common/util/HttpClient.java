package com.ppdai.infrastructure.radar.biz.common.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {
	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
	private static final MediaType JSONTYPE = MediaType.parse("application/json; charset=utf-8");

	private OkHttpClient client;

	public HttpClient(int connTimeout, int readTimeout) {
		ConnectionPool connectionPool = new ConnectionPool(100, 10, TimeUnit.SECONDS);
		client = new OkHttpClient.Builder().connectionPool(connectionPool).connectTimeout(connTimeout, TimeUnit.SECONDS)
				.readTimeout(readTimeout, TimeUnit.SECONDS).build();

	}

	public HttpClient() {
		this(32, 32);
	}

	public boolean check(String url) {		
		Transaction transaction = Tracer.newTransaction("heartbeat", url);
		Response response = null;
		try {
			Request request = new Request.Builder().url(url).get().build();
		    response = client.newCall(request).execute();
			return response.isSuccessful();
		} catch (Exception e) {
			transaction.addData("error", e.getMessage());
			return false;
		} finally {
			transaction.setStatus(Transaction.SUCCESS);
			transaction.complete();
			try {
				if (response != null) {
					response.close();
				}
			} catch (Exception e) {

			}
		}
	}

	public String post(String url, Object reqObj) throws IOException {
		String json = "";
		if (reqObj != null) {
			json = JsonUtil.toJsonNull(reqObj);
		}		
		Response response = null;
		Transaction transaction = null;
		transaction = Tracer.newTransaction("mq-client", url);
		try {
			RequestBody body = RequestBody.create(JSONTYPE, json);
			Request request = new Request.Builder().url(url).post(body).build();
			response = client.newCall(request).execute();
			if (transaction != null) {
				transaction.setStatus(Transaction.SUCCESS);
			}
			if (response.isSuccessful()) {
				return response.body().string();
			} else {
				RuntimeException exception = new RuntimeException(
						response.code() + " error,and message is " + response.message());
				// logger.error("访问"+url+"异常,access_error",exception);
				// transaction.setStatus(exception);
				throw exception;
			}
		} catch (Exception e) {
			logger.error("访问" + url + "异常,access_error", e);
			transaction.setStatus(e);
			throw new RuntimeException(e);
		} finally {
			transaction.complete();
			try {
				if (response != null) {
					response.close();
				}
			} catch (Exception e) {

			}
		}
	}

	public <T> T post(String url, Object request, Class<T> class1) throws IOException {
		String rs = post(url, request);
		if (rs == null || rs.length() == 0 || rs.trim().length() == 0) {
			return null;
		} else {
			return JsonUtil.parseJson(rs, class1);
		}
	}
}
