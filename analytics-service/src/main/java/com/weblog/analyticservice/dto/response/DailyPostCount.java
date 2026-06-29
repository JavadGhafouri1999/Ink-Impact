package com.weblog.analyticservice.dto.response;

import java.time.LocalDate;

public record DailyPostCount(
		LocalDate day,
		long count
) { }
