package com.weblog.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

@Bean
public PasswordEncoder passwordEncoder() {
  // Argon2PasswordEncoder parameters explanation:
  // - saltLength: 16 bytes (128 bits) - standard, enough for security
  // - hashLength: 32 bytes (256 bits) - output hash size
  // - parallelism: 1 thread - can increase for multi-core systems
  // - memory: 65536 KB (64 MB) - memory cost (higher = more resistant to GPU attacks)
  // - iterations: 3 - number of passes (higher = slower, more secure)

  return new Argon2PasswordEncoder(16, 32, 1, 64, 3);
}
}
