package com.wb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AutoImprotConfigure {

    @Autowired
    Environment env;
    @Autowired
    ApplicationContext ctx;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wb.env",value = "dev",havingValue = "true")
    ScanService scanService (){
        ScanService scanService = new ScanService();
        scanService.scanControllerGenerateDoc(env, ctx);
        return scanService;
    }
}
