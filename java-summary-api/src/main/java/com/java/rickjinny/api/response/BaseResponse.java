package com.java.rickjinny.api.response;

import lombok.Data;

@Data
public class BaseResponse<T> {

    private Integer code;

    private String msg;

    private T data;

    public BaseResponse(Integer code, String message) {
        this.code = code;
        this.msg = msg;
    }

    public BaseResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public BaseResponse(StatusCode statusCode) {
        this.code = statusCode.getCode();
        this.msg = statusCode.getMsg();
    }
}
