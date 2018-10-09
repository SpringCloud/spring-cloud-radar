package com.ppdai.infrastructure.radar.biz.common.aop;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ppdai.infrastructure.radar.biz.common.exception.SoaException;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;

@Aspect
@Component
public class ControllerAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAspect.class);	

	@Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
	public void anyController() {
	}
	@Around("anyController()")
	public Object invokeWithCatTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
		// 接收到请求，记录请求内容
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		if (request == null) {
			return joinPoint.proceed();
		} else {
			//正常情况下过滤掉长连接
			String url = request.getRequestURI();
			boolean flag = true;
			if (url.indexOf("/api/client/app/instance/heartbeat") != -1||
					//url.indexOf("/api/client/app/instance/getAppPolling") != -1||
					url.indexOf("instance/registerClient") != -1) {
				flag = false;
			}
			Transaction catTransaction = null;
			try {
				
				if (flag) {
					catTransaction = Tracer.newTransaction("Service", url);
				}
				Object result = joinPoint.proceed();
				if (flag) {
					catTransaction.setStatus(Transaction.SUCCESS);
				}
				return result;
			} catch (SoaException ex) {
				if (catTransaction == null) {
					catTransaction = Tracer.newTransaction("Radar-Method", url);
					flag = true;
				}
				catTransaction.setStatus(ex.getException());
				Tracer.logError(ex.getException());
				LOGGER.error(request.getRequestURL().toString(), ex.getException());
				return ex.getResponse();
			} catch (Throwable ex) {
				if (catTransaction == null) {
					catTransaction = Tracer.newTransaction("Radar-Method", url);
					flag = true;
				}
				catTransaction.setStatus(ex);
				Tracer.logError(ex);
				LOGGER.error(request.getRequestURL().toString(), ex);
				throw ex;
			} finally {
				
				if (catTransaction != null) {
					catTransaction.complete();
				}
			}
		}
	}
}
