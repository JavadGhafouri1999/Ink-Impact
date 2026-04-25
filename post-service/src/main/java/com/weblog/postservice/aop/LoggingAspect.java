package com.weblog.postservice.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
	
	// Covers every method in controller package
	@Around("execution(* com.weblog.postservice.controller.*.*(..))")
	public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
		String method = joinPoint.getSignature().getName();
		String args   = Arrays.toString(joinPoint.getArgs());
		
		log.info("→ {} | args: {}", method, args);
		
		long start = System.currentTimeMillis();
		try {
			Object result   = joinPoint.proceed();
			long   duration = System.currentTimeMillis() - start;
			log.info("← {} | {}ms | response: {}", method, duration, result);
			return result;
		} catch(Exception e) {
			long duration = System.currentTimeMillis() - start;
			log.error("← {} FAILED | {}ms | error: {}", method, duration, e.getMessage());
			throw e;
		}
	}
	
	// Covers every method in service package
	@Around("execution(* com.weblog.postservice.service.*.*(..))")
	public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
		String method = joinPoint.getSignature().getName();
		
		log.debug("→ Service.{} started", method);
		
		long   start    = System.currentTimeMillis();
		Object result   = joinPoint.proceed();
		long   duration = System.currentTimeMillis() - start;
		
		log.debug("← Service.{} | {}ms", method, duration);
		return result;
	}
	
	// Only @Transactional methods (writes)
	@Around("@annotation(org.springframework.transaction.annotation.Transactional)")
	public Object logTransactional(ProceedingJoinPoint joinPoint) throws Throwable {
		String method = joinPoint.getSignature().getName();
		
		log.debug("Transaction start: {}", method);
		Object result = joinPoint.proceed();
		log.debug("Transaction commit: {}", method);
		
		return result;
	}
}
