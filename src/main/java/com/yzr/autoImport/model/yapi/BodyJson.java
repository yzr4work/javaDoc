package com.yzr.autoImport.model.yapi;

import java.util.Map;

/**
 * @author yzr
 */
public class BodyJson {
    /**
     * 类型
     */
    private String type;
    /**
     * 注释
     */
    private String description;
    /**
     * 集合元素
     */
    private BodyJson items;
    /**
     * 对象信息
     */
    private Map<String, BodyJson> properties;
    /**
     * 必须字段
     */
    private String[] required;

    public BodyJson() {

    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BodyJson getItems() {
        return items;
    }

    public void setItems(BodyJson items) {
        this.items = items;
    }

    public Map<String, BodyJson> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, BodyJson> properties) {
        this.properties = properties;
    }

    public String[] getRequired() {
        return required;
    }

    public void setRequired(String[] required) {
        this.required = required;
    }

    public BodyJson(String type, String description) {
        this.type = type;
        this.description = description;
    }
}
