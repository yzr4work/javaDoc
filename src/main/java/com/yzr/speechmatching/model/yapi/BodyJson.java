package com.yzr.speechmatching.model.yapi;

import java.util.Map;

public class BodyJson {

    /**
     * 类型
     */
    private String type = "object";
    /**
     * 属性
     */
    private Map<String, BodyProperties> properties;
    /**
     * 必须属性
     */
    private String[] required;

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public Map<String, BodyProperties> getProperties () {
        return properties;
    }

    public void setProperties (Map<String, BodyProperties> properties) {
        this.properties = properties;
    }

    public String[] getRequired () {
        return required;
    }

    public void setRequired (String[] required) {
        this.required = required;
    }

}
