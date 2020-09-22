package com.yzr.speechmatching.model;

public class BodyProperties {
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
    private BodyJson properties;

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public String getDescription () {
        return description;
    }

    public void setDescription (String description) {
        this.description = description;
    }

    public BodyJson getItems () {
        return items;
    }

    public void setItems (BodyJson items) {
        this.items = items;
    }

    public BodyJson getProperties () {
        return properties;
    }

    public void setProperties (BodyJson properties) {
        this.properties = properties;
    }
}
