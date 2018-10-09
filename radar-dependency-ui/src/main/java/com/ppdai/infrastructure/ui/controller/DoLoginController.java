package com.ppdai.infrastructure.ui.controller;

import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.radar.biz.service.UserService;
import com.ppdai.infrastructure.ui.utils.DesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DoLoginController
 *
 * @author wanghe
 * @date 2018/03/21
 */
@Controller
public class DoLoginController {
	private Logger log = LoggerFactory.getLogger(DoLoginController.class);

	@Autowired
	private UserService userService;

	/**
	 *
	 * @return
	 */
	@RequestMapping("/doLogin")
	public String doLogin(HttpServletRequest request) {
		// TODO: 自行添加跳转至登录页逻辑
		String href = "login";
		return "redirect:" + href;
	}

	/**
	 * 登录验证
	 * 
	 * @param userId
	 * @param passWord
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/verification")
		public UiResponse verification(@RequestParam("userId") String userId, @RequestParam("passWord") String passWord,
									   HttpServletResponse response) {
		UiResponse uiResponse=new UiResponse();
		try {
			boolean flag=userService.login(userId, passWord);
			if(flag){
				Cookie ck=new Cookie("userSessionId",DesUtil.encrypt(userId));
				ck.setMaxAge(60*600);
				response.addCookie(ck);
				uiResponse.setSuc(true);
				uiResponse.setMsg("登录成功");
			}else{
				uiResponse.setSuc(false);
				uiResponse.setMsg("用户名或密码错误");
			}
		} catch (Exception e) {
			uiResponse.setSuc(false);
			uiResponse.setMsg("用户名或密码错误");
		}
		return uiResponse;
	}

}
