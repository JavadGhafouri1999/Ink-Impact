package com.weblog.apigateway.kafka;

import java.util.List;

public record UserActivityEvent(
		String userId,
		List <String> roles,
		String path,
		String method,
		String ip,
		String userAgent,
		String timestamp) { }
