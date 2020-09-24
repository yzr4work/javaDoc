package com.yzr.speechmatching.model;

/**
 * @author yzr
 */
public class RespModel<T> {
    private int code;
    private String msg;
    private T data;

    public RespModel<T> success(T t) {
        this.data = t;
        this.code = 0;
        this.msg = "success";
        return this;
    }

    public int getCode () {
        return code;
    }

    public void setCode (int code) {
        this.code = code;
    }

    public String getMsg () {
        return msg;
    }

    public void setMsg (String msg) {
        this.msg = msg;
    }

    public Object getData () {
        return data;
    }

    public void setData (T data) {
        this.data = data;
    }

}
