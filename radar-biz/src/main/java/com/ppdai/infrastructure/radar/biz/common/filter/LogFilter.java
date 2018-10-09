package com.ppdai.infrastructure.radar.biz.common.filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.MDC;

public class LogFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		MDC.put("guid", UUID.randomUUID().toString().replaceAll("-", "_"));
		try {
			chain.doFilter(request, response);
		} catch (Exception e) {
			throw e;
		}finally {
			MDC.remove("guid");
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
