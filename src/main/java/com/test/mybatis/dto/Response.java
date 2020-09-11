package com.test.mybatis.dto;

import com.test.mybatis.common.ResponseMsgEnum;
import lombok.*;

/**
 * 响应信息
 */
@Getter
@AllArgsConstructor
public class Response<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Response<T> success(T data) {
        return new Response<>(ResponseMsgEnum.SUCCESS.getCode(), ResponseMsgEnum.SUCCESS.getMsg(), data);
    }

    public static <T> Response<T> error(Integer code, String msg, T data) {
        return new Response<>(code, msg, data);
    }

    public static <T> Response<T> error() {
        return error(ResponseMsgEnum.ERROR.getCode(), ResponseMsgEnum.ERROR.getMsg(), null);
    }

    public static <T> Response<T> error(String msg) {
        return error(ResponseMsgEnum.ERROR.getCode(), msg, null);
    }
}
