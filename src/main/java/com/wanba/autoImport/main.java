package com.wanba.autoImport;

import com.wanba.autoImport.model.ApiConfig;
import com.wanba.autoImport.util.QdoxUse;

import java.util.Arrays;

public class main {
    public static void main(String[] args) {
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setSourceTreeList(Arrays.asList("/Users/yzr/IdeaProjects/wb-slave-root/wb-slave-api/src/main/java/com/wb/slaveapi/controller/webapi","/Users/yzr/IdeaProjects/wb-slave-root/wb-slave-rpc-client/src/main/java/com/wanba/slave/rpc/model"));
        apiConfig.setApplicationName("wb_manager");
        QdoxUse.begin(apiConfig);
    }
}
