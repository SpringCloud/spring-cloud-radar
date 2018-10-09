package com.ppdai.infrastructure.radar.biz.common.trace;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 保留追踪信息 Created by liujianjun02 on 2017/12/15.
 */
public class TraceMessage {
	private AtomicInteger counter = new AtomicInteger(0);
	private Map<Integer, TraceMessageItem> data = new ConcurrentHashMap<>(200);
	private String name;
	public TraceMessage(String name){
		this.name=name;
	}
	public void add(TraceMessageItem item) {
		if(!TraceFactory.isEnabled(name)){
			return;
		}
		try {
			TraceMessageItem preTraceMessage = data.get(counter.get());
			if (preTraceMessage != null) {
				if (preTraceMessage.status.equals(item.status)) {
					preTraceMessage.startTime = item.startTime;
					preTraceMessage.endTime = item.endTime;
					preTraceMessage.msg = item.msg;
				} else {
					Integer index = counter.incrementAndGet();
					counter.compareAndSet(100, 0);
					data.put(index, item);
				}
			} else {
				data.put(counter.get(), item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AtomicInteger getCounter() {
		return counter;
	}

	public Map<Integer, TraceMessageItem> getData() {
		return data;
	}

}
