package com.ppdai.infrastructure.rest.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
	public void home(HttpServletResponse response) {
		StringBuilder sbHtml = new StringBuilder();
		response.addHeader("Content-Type", "text/html; charset=UTF-8");
		sbHtml.append(
				"<!doctype html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>");
		sbHtml.append("Radar启动正常，欢迎使用Radar注册中心");		
		sbHtml.append("</body></html>");
		try {
			response.getWriter().write(sbHtml.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
