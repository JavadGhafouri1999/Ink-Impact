package com.weblog.authservice.dto.response;

import jakarta.validation.constraints.Size;

public record ResetCodeResponse(
		
		@Size(min = 6, max = 6, message = "Security code is 6 digit")
		Integer code
) { }
