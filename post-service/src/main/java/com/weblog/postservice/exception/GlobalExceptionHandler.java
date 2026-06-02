package com.weblog.postservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private Map <String, Object> buildResponse(int status, String message) {
	Map <String, Object> body = new HashMap <>();
	body.put("status", status);
	body.put("message", message);
	body.put("timestamp", LocalDateTime.now().toString());
	return body;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity <Map <String, Object>> handleValidationException(
		  MethodArgumentNotValidException ex) {

	Map <String, String> errors =
			ex.getBindingResult().getFieldErrors().stream()
			  .collect(
					  Collectors.toMap(
							  FieldError::getField,
							  fieldError ->
									  fieldError.getDefaultMessage() != null
									  ? fieldError.getDefaultMessage()
									  : "Invalid value"));

	log.warn("Validation failed: {}", errors);

	Map <String, Object> body = new HashMap <>();
	body.put("status", 400);
	body.put("message", "Validation failed");
	body.put("timestamp", LocalDateTime.now().toString());
	body.put("errors", errors);

	return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(PostNotFoundException.class)
  public ResponseEntity <Map <String, Object>> handlePostNotFound(PostNotFoundException ex) {
	log.warn("Post not found: {}", ex.getMessage());
	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildResponse(404, ex.getMessage()));
  }

  @ExceptionHandler(DuplicateTitleException.class)
  public ResponseEntity <Map <String, Object>> handleDuplicateTitle(DuplicateTitleException ex) {
	log.warn("Duplicate title: {}", ex.getMessage());

	return ResponseEntity.status(HttpStatus.CONFLICT).body(buildResponse(409, ex.getMessage()));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity <Map <String, Object>> handleUnauthorized(UnauthorizedException ex) {
	log.warn("Unauthorized action: {}", ex.getMessage());

	return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildResponse(403, ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity <Map <String, Object>> handleGeneral(Exception ex) {
	log.error("Unexpected error: {}", ex.getMessage(), ex);

	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						 .body(buildResponse(500, ex.getMessage()));
  }
}
