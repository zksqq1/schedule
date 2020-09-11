package com.test.mybatis.exception;

import com.test.mybatis.common.ResponseMsgEnum;
import com.test.mybatis.dto.Response;
import com.test.mybatis.exception.CustomException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 *
 */
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    @ExceptionHandler(value = CustomException.class)
    public Response<Void> serviceError(CustomException ex) {
        return Response.error(ex.getMessage());
    }

    @ExceptionHandler(value = {
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class
    })
    public Response<Void> serviceError(Exception ex) {
        return Response.error(ResponseMsgEnum.ERR_PARAM.getCode(), ex.getMessage(), null);
    }
}
