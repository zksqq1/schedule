package com.test.mybatis.dto;

/**
 * 响应信息
 */
public class Response<T> {
    private String msg;
    private Integer code;
    private T data;

    public Response(String msg, Integer code, T data) {
        this.msg = msg;
        this.code = code;
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public static class ResponseBuilder<T> {
        private String msg;
        private Integer code;
        private T data;

        public static ResponseBuilder builder() {
            return new ResponseBuilder();
        }

        public ResponseBuilder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public ResponseBuilder code(Integer code) {
            this.code = code;
            return this;
        }

        public ResponseBuilder data(T data) {
            this.data = data;
            return this;
        }

        public static ResponseBuilder error(Integer code, String msg) {
            return new ResponseBuilder().code(code).msg(msg);
        }

        public static ResponseBuilder success() {
            return new ResponseBuilder().code(0).msg("成功");
        }

        public Response<T> build() {
            return new Response<>(this.msg, this.code, this.data);
        }
    }
}
