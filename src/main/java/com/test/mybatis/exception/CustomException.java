package com.test.mybatis.exception;

/**
 *
 */
public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message, null, true, false);
    }

    public CustomException(String message, Throwable throwable) {
        super(message, throwable, true, false);
    }
}
