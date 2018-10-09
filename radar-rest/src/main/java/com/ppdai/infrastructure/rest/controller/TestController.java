package com.ppdai.infrastructure.rest.controller;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.exception.TestException;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;
import com.ppdai.infrastructure.radar.biz.common.util.EmailUtil;
import com.ppdai.infrastructure.radar.biz.service.ServiceTest;

@RestController
public class TestController {
	private Logger log = LoggerFactory.getLogger(TestController.class);
	@Autowired
	private ServiceTest serviceTest;
	@Autowired
	private SoaConfig soaConfig;

	@Autowired
	private EmailUtil emailUtil;

	private AtomicBoolean flag = new AtomicBoolean(true);
	
	@GetMapping(value = "/hs", produces = { "text/html;charset=utf-8" })
	public String hs1() {		
		return "OK";
	}	

	@GetMapping(value = "/clear", produces = { "text/html;charset=utf-8" })
	public String clear() {
		return serviceTest.clear() + "";
	}

	@GetMapping(value = "/ht", produces = { "text/html;charset=utf-8" })
	public Object ht() {
		return serviceTest.getHt();
	}

	@GetMapping(value = "/sd", produces = { "text/html;charset=utf-8" })
	public Object sd() {
		emailUtil.sendInfoMail("ceshi", "ceshi");
		emailUtil.sendWarnMail("ceshi", "ceshi");
		emailUtil.sendErrorMail("ceshi", "ceshi");
		return "ok";
	}

	@GetMapping(value = "/test", produces = { "text/html;charset=utf-8" })
	public String test() {
		try {
			if (flag.get()) {
				flag.set(false);
				serviceTest.test(1);
			} else {
				return "有未完成测试";
			}
		} catch (TestException e) {
			flag.set(true);
			return "测试正常";
		} catch (Exception e) {
			log.error("测试异常", e);
			flag.set(true);
			return "测试异常";
		}
		return "测试正常";
	}

	@GetMapping("/test1")
	public String test1(HttpServletRequest request) {
		Integer t = 1;
		if (StringUtils.isEmpty(request.getParameter("t"))) {
			t = 1;
		} else {
			try {
				t = Integer.parseInt(request.getParameter("t"));
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		CountDownLatch countDownLatch = new CountDownLatch(t);
		for (int i = 0; i < t; i++) {
			new Test(i + 1, countDownLatch).start();
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "压测开始！";
	}

	class Test {
		private int count;
		private CountDownLatch countDownLatch;
		private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 3L, TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(500), SoaThreadFactory.create("TestController", true),
				new ThreadPoolExecutor.DiscardPolicy());;

		public Test(int c, CountDownLatch countDownLatch1) {
			count = c;
			countDownLatch = countDownLatch1;
		}

		public void start() {
			if (soaConfig.enableTest()) {
				executor.execute(() -> {
					try {
						System.out.println("servicethread-" + count);
						serviceTest.test1(count);
					} catch (TestException e) {
					} catch (Exception e) {
					} finally {
						countDownLatch.countDown();
						System.out.println("countDownLatchCount-" + countDownLatch.getCount());
					}
				});
			}
		}
	}
}
