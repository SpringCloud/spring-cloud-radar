package com.ppdai.infrastructure.radar.client.http.common;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

public class SoaCommandForSemaphoreIsolation  extends HystrixCommand<CloseableHttpResponse> {
	private CloseableHttpClient httpclient;
	private HttpUriRequest httpUriRequest;   
    public SoaCommandForSemaphoreIsolation(CloseableHttpClient httpclient,HttpUriRequest httpUriRequest,  String commandGroup, String commandKey) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandGroup))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                .andCommandPropertiesDefaults(
                        // we want to default to semaphore-isolation since this wraps
                        // 2 others commands that are already thread isolated
                        HystrixCommandProperties.Setter()
                                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                ));

        this.httpclient = httpclient;
        this.httpUriRequest=httpUriRequest;        
    }

    @Override
    protected CloseableHttpResponse run() throws Exception {
        try {
            return forward();
        } catch (IOException e) {
            throw e;
        }
    }

   private CloseableHttpResponse forward() throws IOException {
//    	Context ctx = new CatContext();
//    	Cat.logRemoteCallClient(ctx);
//    	httpUriRequest.addHeader(Constants.CAT_ROOT_MESSAGE_ID, ctx.getProperty(Cat.Context.ROOT));
//    	httpUriRequest.addHeader(Constants.CAT_PARENT_MESSAGE_ID, ctx.getProperty(Cat.Context.PARENT));
//    	httpUriRequest.addHeader(Constants.CAT_CHILD_MESSAGE_ID, ctx.getProperty(Cat.Context.CHILD));
        return httpclient.execute(httpUriRequest);
    }
}