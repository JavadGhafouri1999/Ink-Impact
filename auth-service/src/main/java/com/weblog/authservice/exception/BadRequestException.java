package com.weblog.authservice.exception;

public class BadRequestException extends RuntimeException {

public BadRequestException(String message) {
  super(message);
}
}
