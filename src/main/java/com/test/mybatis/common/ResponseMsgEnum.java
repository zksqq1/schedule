package com.test.mybatis.common;

/**
 *
 */
public enum  ResponseMsgEnum {
    SUCCESS(0 , "OK"),
    ERROR(1, "FAIL"),
    ERR_PARAM(10001, "参数错误"),

    ;

    private int code;
    private String msg;
    ResponseMsgEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public ResponseMsgEnum setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public ResponseMsgEnum setMsg(String msg) {
        this.msg = msg;
        return this;
    }
}
