package com.wanba.autoImport.model.yapi;

public class JavaApiInfo {
    /**
     * switch_notice
     */
    private final boolean switch_notice = false;
    /**
     * 返回值 固定为json格式
     */
    private final boolean res_body_is_json_schema = true;
    private final String res_body_type = "json";
    /**
     * token
     */
    private String token;
    /**
     * 接口名称
     */
    private String title;
    /**
     * 分类id
     */
    private String catid;
    /**
     * 接口路径
     */
    private String path;
    /**
     * 方法类型
     */
    private String method;
    /**
     * 接口完成状态
     */
    private String status;
    /**
     * 信息
     */
    private String message;
    /**
     * 请求参数表单
     */
    private ReqForm[] req_query;
    /**
     * 请求参数 body 中json字符串
     */
    private String req_body_other;
    /**
     * 请求头信息
     */
    private ReqHeader[] req_headers;
    /**
     * 返回值 json字符串
     */
    private String res_body = "json";
    /**
     * 请求参数 是否json格式
     */
    private boolean req_body_is_json_schema;
    /**
     * 项目名称 对应文件夹名称
     */
    private String applicationName;
    /**
     * 请求参数body格式
     */
    private String req_body_type;
    /**
     * 备注
     */
    private String desc;
    /**
     * 接口文档id
     */
    private int _id;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCatid() {
        return catid;
    }

    public void setCatid(String catid) {
        this.catid = catid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isSwitch_notice() {
        return switch_notice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ReqForm[] getReq_query() {
        return req_query;
    }

    public void setReq_query(ReqForm[] req_query) {
        this.req_query = req_query;
    }

    public String getReq_body_other() {
        return req_body_other;
    }

    public void setReq_body_other(String req_body_other) {
        this.req_body_other = req_body_other;
    }

    public String getRes_body() {
        return res_body;
    }

    public void setRes_body(String res_body) {
        this.res_body = res_body;
    }

    public boolean isRes_body_is_json_schema() {
        return res_body_is_json_schema;
    }

    public boolean isReq_body_is_json_schema() {
        return req_body_is_json_schema;
    }

    public void setReq_body_is_json_schema(boolean req_body_is_json_schema) {
        this.req_body_is_json_schema = req_body_is_json_schema;
    }

    public String getRes_body_type() {
        return res_body_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public ReqHeader[] getReq_headers() {
        return req_headers;
    }

    public void setReq_headers(ReqHeader[] req_headers) {
        this.req_headers = req_headers;
    }

    public String getReq_body_type() {
        return req_body_type;
    }

    public void setReq_body_type(String req_body_type) {
        this.req_body_type = req_body_type;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int get_id () {
        return _id;
    }

    public void set_id (int _id) {
        this._id = _id;
    }
}
