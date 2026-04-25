package com.weblog.postservice.utils;

import java.util.UUID;

public class SlugGenerator {
	
	private SlugGenerator() {
		// utility class, prevent instantiation
	}
	
	public static String generateSlug(String title) {
		return title.toLowerCase()
		            .replaceAll("[^a-z0-9\\s]", "")
		            .replaceAll("\\s+", "-")
		            .replaceAll("-{2,}", "-")           // collapse multiple dashes
		       + "-" + UUID.randomUUID().toString().substring(0, 8); // uniqueness
	}
}
