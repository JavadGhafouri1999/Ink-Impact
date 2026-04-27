package com.weblog.commentservice.aop;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter implements Filter {
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String             requestId   = UUID.randomUUID().toString().substring(0, 8);
		
		MDC.put("requestId", requestId);
		MDC.put("method", httpRequest.getMethod());
		MDC.put("path", httpRequest.getRequestURI());
		
		try {
			chain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}
}
