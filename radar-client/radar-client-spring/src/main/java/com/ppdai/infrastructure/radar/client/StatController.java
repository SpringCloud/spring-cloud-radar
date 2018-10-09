package com.ppdai.infrastructure.radar.client;

import java.lang.Thread.State;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ppdai.infrastructure.radar.biz.common.trace.TraceFactory;

@RestController
public class StatController {
	@GetMapping("/radar/client/cache")
	public Object data() {
		return DiscoveryClient.getInstance().getData();
	}
	
	@GetMapping("/radar/client/instance")
	public Object instance() {
		return DiscoveryClient.getInstance().getInstanceRadar();
	}

	@GetMapping("/radar/client/trace")
	public Object trace() {
		return TraceFactory.getTraces();
	}
	
	@GetMapping("/radar/client/config")
	public Object config() {
		return DiscoveryClient.getInstance().getConfig();
	}
	@GetMapping("/radar/client/th")
	public String th() {
		StringBuilder rs = new StringBuilder();
		Map<State, Integer> state=new HashMap<>();
		for (Map.Entry<Thread, StackTraceElement[]> t1 : Thread.getAllStackTraces().entrySet()) {
			Thread thread = t1.getKey();
			StackTraceElement[] stackTraceElements = t1.getValue();
			state.putIfAbsent(thread.getState(), 0);
			state.put(thread.getState(), state.get(thread.getState())+1);
			rs.append("\n<br/>线程名称：" + thread.getName() + ",线程id:"+thread.getId()+",16进制为："+Long.toHexString(thread.getId())+",线程优先级为："+thread.getPriority()+"，线程状态："+thread.getState()+"<br/>\n");
			for (StackTraceElement st : stackTraceElements) {
				rs.append(st.toString()+ "<br/>\n");
			}
		}
		StringBuilder rs1 = new StringBuilder();
		for (Map.Entry<State, Integer> t1 : state.entrySet()) {
			rs1.append("线程状态："+t1.getKey()+ ",数量："+t1.getValue()+"<br/>\n");
		}
		return rs1.toString()+rs.toString();
	}
}
