package com.yzr.speechmatching.model;

public class ReqBodyForm {
    /**
     * 是否必传
     */
    private int required;
    /**
     * 字段名称
     */
    private String name;
    /**
     * 字段类型
     */
    private String type;
    /**
     * 字段注释
     */
    private String desc;

    public ReqBodyForm () {
    }

    public ReqBodyForm (int required, String name, String desc) {
        this.required = required;
        this.name = name;
        this.desc = desc;
    }

    public int getRequired () {
        return required;
    }

    public void setRequired (int required) {
        this.required = required;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public String getDesc () {
        return desc;
    }

    public void setDesc (String desc) {
        this.desc = desc;
    }
}
