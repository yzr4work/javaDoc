package com.yzr.speechmatching.model;

public class RespModel {
    private int code;
    private String msg;
    private Object data;
    final static int SUCCESS_CODE = 0;
    final static String SUCCESS_MSG = "success";
    final static int FAIL_CODE = 1;
    final static String FAIL_MSG = "fail";

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

    public void setData (Object data) {
        this.data = data;
    }

    public static RespModel success(Object data){
        RespModel respModel = new RespModel();
        respModel.setCode(SUCCESS_CODE);
        respModel.setMsg(SUCCESS_MSG);
        respModel.setData(data);
        return respModel;
    }

    public static RespModel fail(Object data){
        RespModel respModel = new RespModel();
        respModel.setCode(FAIL_CODE);
        respModel.setMsg(FAIL_MSG);
        respModel.setData(data);
        return respModel;
    }
}
