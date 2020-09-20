package com.yzr.speechmatching.model;

import java.util.List;

/**
 * 用户
 */
public class User {
    /**
     * uid111
     */
    private String uid;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 年龄
     */
    private int age;
    /**
     * 地址集合
     * @see Address 地址
     */
    private List<Address> cityList;

    public String getUid () {
        return uid;
    }

    public void setUid (String uid) {
        this.uid = uid;
    }

    public String getUserName () {
        return userName;
    }

    public void setUserName (String userName) {
        this.userName = userName;
    }

    public int getAge () {
        return age;
    }

    public void setAge (int age) {
        this.age = age;
    }

    public List<Address> getCityList () {
        return cityList;
    }

    public void setCityList (List<Address> cityList) {
        this.cityList = cityList;
    }
}
