package com.weblog.analyticservice.dto.response;

import java.time.LocalDate;

public record DailyCommentCount(
		LocalDate day,
		long count
) { }
