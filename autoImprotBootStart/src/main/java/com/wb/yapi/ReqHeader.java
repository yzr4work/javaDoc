package com.wb.yapi;

public class ReqHeader {
    private final int required = 1;
    private String name;
    private String value;

    public ReqHeader() {
    }

    public ReqHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public int getRequired() {
        return required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
