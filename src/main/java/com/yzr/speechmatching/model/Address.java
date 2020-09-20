package com.yzr.speechmatching.model;

/**
 * 住址
 */
public class Address {
    /**
     * 国家
     */
    private String country;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;

    public String getCountry () {
        return country;
    }

    public void setCountry (String country) {
        this.country = country;
    }

    public String getProvince () {
        return province;
    }

    public void setProvince (String province) {
        this.province = province;
    }

    public String getCity () {
        return city;
    }

    public void setCity (String city) {
        this.city = city;
    }
}
