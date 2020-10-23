package com.wanba.autoImport.model.yapi;

/**
 * @author yzr
 */
public class Cat {
    /**
     * 项目id
     */
    private int project_id;
    /**
     * 文件夹id
     */
    private int _id;
    /**
     * 名称
     */
    private String name;
    /**
     * 备注
     */
    private String desc;
    /**
     * token 创建文件夹时使用
     */
    private String token;

    public Cat() {
    }

    public Cat(int project_id, String name, String desc, String token) {
        this.project_id = project_id;
        this.name = name;
        this.desc = desc;
        this.token = token;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
