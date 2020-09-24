package com.yzr.speechmatching.model;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.List;

/**
 * 用户
 */
public class User {
    /**
     * uid111
     */
    @Nullable
    private String uid;
    /**
     * 用户名
     */
    @NotNull
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
    /**
     * 首选地址
     * @see Address 地址
     */
    private Address firstAddress;

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

    public Address getFirstAddress() {
        return firstAddress;
    }

    public void setFirstAddress(Address firstAddress) {
        this.firstAddress = firstAddress;
    }
}
