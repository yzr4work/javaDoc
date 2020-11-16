package com.wanba.autoImport;

import com.wanba.autoImport.model.ApiConfig;
import com.wanba.autoImport.util.QdoxUse;

import java.util.Arrays;

public class main {
    public static void main(String[] args) {
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setSourceTreeList(Arrays.asList("/Users/yzr/IdeaProjects/wb-manager-root/wb-manager/src/main/java/com/wb/manager/activityreward"));
        apiConfig.setApplicationName("wb_manager");
        QdoxUse.begin(apiConfig);
    }
}
