package com.test.mybatis.dto;

import lombok.*;

/**
 * 响应信息
 */
@Getter
@AllArgsConstructor
public class Response<T> {
    private String msg;
    private Integer code;
    private T data;

    public static <T> Response<T> success(T data) {
        return new Response<>("成功", 0, data);
    }

    public static <T> Response<T> error(String msg, Integer code, T data) {
        return new Response<>(msg, code, data);
    }

    public static <T> Response<T> error() {
        return error("失败", -1, null);
    }
}
