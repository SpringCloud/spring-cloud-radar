package com.ppdai.infrastructure.ui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.ppdai.infrastructure.ui.utils.CookieUtil;
import com.ppdai.infrastructure.ui.utils.DesUtil;

@Order(1)
@WebFilter(filterName = "AuthFilter", urlPatterns = "/*")
@Configuration
public class AuthFilter implements Filter {
	Logger log = LoggerFactory.getLogger(this.getClass().getName());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String uri = request.getRequestURI();
		if (skipUri(uri)) {
			chain.doFilter(request, response);
		} else {
			try {
				Cookie cookie = CookieUtil.getCookie(request, "userSessionId");
				if (cookie == null) {
					response.sendRedirect("/login");
				} else {
					String userId = DesUtil.decrypt(cookie.getValue());
					req.setAttribute("userSessionId", userId);					
					chain.doFilter(request, response);
				}

			} catch (Exception e) {
				log.error("login fail", e);
				response.sendRedirect("/login");
			}
		}
	}

	private boolean skipUri(String uri) {
		for (String skipUri : new String[] { "/login", ".js", ".css", ".jpg", ".woff", ".png", "/auth", "/test",
				"/verification" }) {
			if (uri.contains(skipUri)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
	
}
