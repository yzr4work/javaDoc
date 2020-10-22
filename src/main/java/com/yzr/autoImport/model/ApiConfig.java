package com.yzr.autoImport.model;

import java.util.List;

public class ApiConfig {
    /**
     * 工程名称
     */
    private String applicationName;
    /**
     * h5工程token
     */
    private String h5Token;
    /**
     * api工程token
     */
    private String apiToken;
    /**
     * rpc工程token
     */
    private String rpcToken;
    /**
     * manager工程token
     */
    private String managerToken;
    /**
     * game工程token
     */
    private String gameToken;
    /**
     * 扫描包集合
     */
    private List<String> sourceTreeList;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getH5Token() {
        return h5Token;
    }

    public void setH5Token(String h5Token) {
        this.h5Token = h5Token;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getRpcToken() {
        return rpcToken;
    }

    public void setRpcToken(String rpcToken) {
        this.rpcToken = rpcToken;
    }

    public String getManagerToken() {
        return managerToken;
    }

    public void setManagerToken(String managerToken) {
        this.managerToken = managerToken;
    }

    public String getGameToken() {
        return gameToken;
    }

    public void setGameToken(String gameToken) {
        this.gameToken = gameToken;
    }

    public List<String> getSourceTreeList() {
        return sourceTreeList;
    }

    public void setSourceTreeList(List<String> sourceTreeList) {
        this.sourceTreeList = sourceTreeList;
    }
}
