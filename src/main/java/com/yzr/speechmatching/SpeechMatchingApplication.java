package com.yzr.speechmatching;

import com.yzr.speechmatching.util.JavaDocUse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static com.yzr.speechmatching.util.JavaDocUse.show;

@SpringBootApplication
public class SpeechMatchingApplication {

    public static void main (String[] args) {
        SpringApplication.run(SpeechMatchingApplication.class, args);
    }

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");
            //所有controller
            Map<String, Object> controllerBeansMap = ctx.getBeansWithAnnotation(Controller.class);
            controllerBeansMap.putAll(ctx.getBeansWithAnnotation(RestController.class));
            System.out.println(" controller size : " + controllerBeansMap.size());
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
                                Arrays.asList(finalUrl).forEach(eachUrl -> System.out.println(baseUrl + eachUrl));
                                //处理每个请求路径的方法参数 返回值参数
                                //参数类型数组
                                Class<?>[] parameterTypes = method.getParameterTypes();
                                //参数名称数组
                                String[] parameters = parameterNameDiscoverer.getParameterNames(method);
                                //参数注解 二维数组
                                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                                String classPath = controllerClass.getResource("/").getPath();

                                //从注释中获取参数信息
                                //System.out.println("编译后文件夹地址 : " + classPath);
                                //System.out.println("当前类的源码文件地址 : " +  modelPath + "/src/main/java/" + controllerClass.getName().replace(".","/") + ".java");

                                com.sun.tools.javadoc.Main.execute(new String[] {"-doclet",
                                        JavaDocUse.Doclet.class.getName(),
                                        "-encoding","utf-8","-private","-classpath",
                                        classPath,
                                        classPath.replace("/target/classes/","") + "/src/main/java/" +  controllerClass.getName().replace(".","/") + ".java"});
                                show(classPath, 1);

                            }
                        });
                    }

                });
            }

        };
    }

}
