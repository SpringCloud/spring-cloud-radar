package com.ppdai.infrastructure.radar.client.http.proxy;

import java.util.ArrayList;
import java.util.Map;

import com.ppdai.infrastructure.radar.client.http.transport.DefaultTransportManager;
import com.ppdai.infrastructure.radar.client.http.transport.ITransportManager;
import com.ppdai.infrastructure.radar.client.http.util.BeanUtils;

class ServiceProxyImpl implements ISoaService {
	private ServiceMeta serviceMeta;
	private volatile ITransportManager iTransportManager;
	private final Object lockObj = new Object();

	public ServiceProxyImpl(ServiceMeta serviceMeta) {
		this.serviceMeta = serviceMeta;
	}

	@Override
	public <TResponse> TResponse request(Class<TResponse> resp, String path, Object data, Map<String, String> header) {
		if (iTransportManager == null) {
			synchronized (lockObj) {
				if (iTransportManager == null) {
					Map<String, ITransportManager> mapRs = BeanUtils.getContext()
							.getBeansOfType(ITransportManager.class);
					if (mapRs != null) {
						iTransportManager=new ArrayList<>(mapRs.values()).get(0);
					}
					else{
						iTransportManager=new DefaultTransportManager();
					}
				}
			}
		}
		// TODO Auto-generated method stub
		return iTransportManager.request(resp, path, data, header,
				serviceMeta);
	}

	@Override
	public <TResponse> TResponse request(Class<TResponse> resp, String path, Object data) {
		// TODO Auto-generated method stub
		return request(resp, path, data, null);
	}

}
