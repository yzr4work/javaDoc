package com.wanba.autoImport.model;

import java.util.List;

public class ApiConfig {
    /**
     * 工程名称
     */
    private String applicationName = "test";
    /**
     * h5工程token
     */
    private String h5Token = "ca70bcd413e4be11b970de7e18923cd8927cacf106d6c4ac7e8bf48cc14dc16b";
    /**
     * api工程token
     */
    private String apiToken = "ca70bcd413e4be11b970de7e18923cd8927cacf106d6c4ac7e8bf48cc14dc16b";
    /**
     * rpc工程token
     */
    private String rpcToken = "ca70bcd413e4be11b970de7e18923cd8927cacf106d6c4ac7e8bf48cc14dc16b";
    /**
     * manager工程token
     */
    private String managerToken = "ca70bcd413e4be11b970de7e18923cd8927cacf106d6c4ac7e8bf48cc14dc16b";
    /**
     * game工程token
     */
    private String gameToken = "ca70bcd413e4be11b970de7e18923cd8927cacf106d6c4ac7e8bf48cc14dc16b";
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
