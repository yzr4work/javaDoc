package com.yzr.speechmatching.model.yapi;

public class BodyJsonWithSchema extends BodyJson {
    /**
     * 约束
     */
    private String  $schema = "http://json-schema.org/draft-04/schema#";

    public String get$schema () {
        return $schema;
    }

    public BodyJsonWithSchema () {
    }

    public BodyJsonWithSchema (BodyJson bodyJson) {
        this.setType(bodyJson.getType());
        this.setRequired(bodyJson.getRequired());
        this.setProperties(bodyJson.getProperties());
    }
}
