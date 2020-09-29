package com.yzr.speechmatching;

import com.yzr.speechmatching.model.yapi.JavaApiInfo;
import com.yzr.speechmatching.util.JavaDocUse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.yzr.speechmatching.util.JavaDocUse.show;

@SpringBootApplication
public class SpeechMatchingApplication {

    public static void main (String[] args) {
        SpringApplication.run(SpeechMatchingApplication.class, args);
    }

    @Autowired
    private Environment env;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            try {
                scanControllerGenerateDoc(ctx);
            } catch (Exception e) {
                System.err.println("generateDoc is error " + e.getMessage());
            }
        };
    }

    private void scanControllerGenerateDoc(ApplicationContext ctx){
        //所有controller
        Map<String, Object> controllerBeansMap = ctx.getBeansWithAnnotation(Controller.class);
        controllerBeansMap.putAll(ctx.getBeansWithAnnotation(RestController.class));
        if (controllerBeansMap.size() == 0){
            return;
        }
        String serverPort = env.getProperty("server.port");
        String applicationName = env.getProperty("spring.application.name");
        if (applicationName == null ){
            applicationName = "";
        }

        String h5Token = env.getProperty("h5.token");
        String apiToken = env.getProperty("api.token");
        String rpcToken = env.getProperty("rpc.token");
        String managerToken = env.getProperty("manager.token");
        String gameToken = env.getProperty("game.token");

        String token  = apiToken;
        if (env.getProperty("wb.clusterName") != null){
            token = gameToken;
        }else if ("80".equals(serverPort)){
            token = rpcToken;
        } else if (applicationName.contains("admin") || applicationName.contains("manager") ){
            token = managerToken;
        }

        //遍历每个controller
        for (Map.Entry<String, Object> controllerBeanEntry : controllerBeansMap.entrySet()) {
            System.out.println(" current controller is : " + controllerBeanEntry.getKey());
            //类上注解
            Class<?> controllerClass = controllerBeanEntry.getValue().getClass();
            RequestMapping[] requestMappingsOnClass = controllerClass.getAnnotationsByType(RequestMapping.class);
            GetMapping[] getMappingsOnClass = controllerClass.getAnnotationsByType(GetMapping.class);
            PostMapping[] postMappingsOnClass = controllerClass.getAnnotationsByType(PostMapping.class);
            String[] controllerBaseUrl = null;
            if (requestMappingsOnClass.length > 0){
                controllerBaseUrl = requestMappingsOnClass[0].value();
            }
            if (getMappingsOnClass.length > 0){
                controllerBaseUrl = getMappingsOnClass[0].value();
            }
            if (postMappingsOnClass.length > 0){
                controllerBaseUrl = postMappingsOnClass[0].value();
            }
            //方法上注解
            Method[] methods = controllerClass.getMethods();
            String[] finalControllerBaseUrl = controllerBaseUrl;
            AtomicReference<String> finalToken = new AtomicReference<>(token);
            String finalApplicationName = applicationName;
            Arrays.asList(methods).forEach(method -> {
                if (! method.getDeclaringClass().getName().equals(controllerClass.getName())){
                    return;
                }
                RequestMapping requestMappingOnMethod = method.getAnnotation(RequestMapping.class);
                GetMapping getMappingOnMethod = method.getAnnotation(GetMapping.class);
                PostMapping postMappingOnMethod = method.getAnnotation(PostMapping.class);
                if (requestMappingOnMethod == null && getMappingOnMethod == null && postMappingOnMethod == null){
                    return;
                }
                String[] url = null;
                if (requestMappingOnMethod != null){
                    url = requestMappingOnMethod.value();
                }
                if (getMappingOnMethod != null ){
                    url = getMappingOnMethod.value();
                }
                if (postMappingOnMethod != null){
                    url = postMappingOnMethod.value();
                }
                if (finalControllerBaseUrl.length > 0){
                    String[] finalUrl = url;
                    Arrays.asList(finalControllerBaseUrl).forEach(baseUrl -> {
                        if (finalUrl.length > 0){
                            System.out.println("url : ");
                            Arrays.asList(finalUrl).forEach(eachUrl -> {
                                genderDoc(h5Token, apiToken, controllerClass, finalToken, finalApplicationName, method, requestMappingOnMethod, postMappingOnMethod, baseUrl, eachUrl);
                            });
                        }
                    });
                }

            });
        }

    }

    private void genderDoc(String h5Token, String apiToken, Class<?> controllerClass, AtomicReference<String> finalToken, String finalApplicationName, Method method, RequestMapping requestMappingOnMethod, PostMapping postMappingOnMethod, String baseUrl, String eachUrl) {
        String controllerUrl =  baseUrl + eachUrl;
        System.out.println(controllerUrl);
        if (finalToken.get().equals(apiToken) && controllerUrl.startsWith("/web/webApi")){
            finalToken.set(h5Token);
        }
        String classPath = controllerClass.getResource("/").getPath();
        com.sun.tools.javadoc.Main.execute(new String[] {"-doclet",
                JavaDocUse.Doclet.class.getName(),
                "-encoding","utf-8",
                classPath.replace("/target/classes/","") + "/src/main/java/" +  controllerClass.getName().replace(".","/") + ".java"});
        JavaApiInfo javaApiInfo = new JavaApiInfo();
        javaApiInfo.setPath(controllerUrl);
        if (postMappingOnMethod != null || (requestMappingOnMethod != null && Arrays.asList(requestMappingOnMethod.method()).contains(RequestMethod.POST))){
            javaApiInfo.setMethod("POST");
        }else {
            javaApiInfo.setMethod("GET");
        }
        javaApiInfo.setToken(finalToken.get());
        javaApiInfo.setApplicationName(finalApplicationName);
        show(classPath, javaApiInfo,method.getName());
    }


}
