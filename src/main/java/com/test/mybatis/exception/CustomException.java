package com.test.mybatis.exception;

/**
 *
 */
public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message, null, true, false);
    }
}
