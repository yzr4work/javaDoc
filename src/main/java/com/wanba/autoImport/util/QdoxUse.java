package com.wanba.autoImport.util;

import com.alibaba.fastjson.JSON;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.*;
import com.wanba.autoImport.model.ApiConfig;
import com.wanba.autoImport.model.yapi.BodyJson;
import com.wanba.autoImport.model.yapi.JavaApiInfo;
import com.wanba.autoImport.model.yapi.ReqForm;
import com.wanba.autoImport.model.yapi.ReqHeader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class QdoxUse {
    private static Map<String , JavaClass> javaClassMap = new HashMap<>();

    //入口
    public static void begin(ApiConfig apiConfig){
        for (String filePath : apiConfig.getSourceTreeList()) {
            JavaProjectBuilder builder = new JavaProjectBuilder();
            builder.addSourceTree(new File(filePath));
            Collection<JavaClass> classes = builder.getClasses();
            for (JavaClass javaClass : classes) {
                javaClassMap.put(javaClass.getFullyQualifiedName(), javaClass);

            }
        }
        generateDoc(apiConfig);
    }



    private static void generateDoc(ApiConfig apiConfig) {
        for (String classFullName : javaClassMap.keySet()) {
            JavaClass javaClass = javaClassMap.get(classFullName);
            //获取类上面的注解
            List<JavaAnnotation> annotations = javaClass.getAnnotations();
            //type 代表不同的接口类型 0为正常的spring接口 1为旧微服务
            for (JavaAnnotation annotation : annotations) {
                String annotationName = annotation.getType().getValue();
                //判断当前类是否是controller 或者是老的微服务
                boolean isOldMS = "com.wb.microserver.MsFunction".equals(javaClass.getSuperJavaClass().getFullyQualifiedName());
                if ("controller".equals(annotationName) ||
                        "RestController".equals(annotationName) ||
                                    isOldMS){
                    String reqPath = "";
                    //获取类上的接口路径
                    for (JavaAnnotation javaAnnotation : annotations) {
                        if ("RequestMapping".equals(javaAnnotation.getType().getName()) ){
                            if (javaAnnotation.getNamedParameter("value") != null) {
                                String value = javaAnnotation.getNamedParameter("value").toString();
                                reqPath = !StringUtils.isEmpty(value) ? value.replaceAll("'", "").replaceAll("\"", "") : "";
                            }
                        }

                    }
                    //处理方法
                    List<JavaMethod> methods = javaClass.getMethods();
                    for (JavaMethod method : methods) {
                        //私有方法跳过 或 没有注释的接口跳过
                        if (method.isPrivate() || StringUtils.isEmpty(method.getComment())) {
                            continue;
                        }
                        //
                        int reqType = 0;
                        String finalReqUrl = reqPath;
                        //正常spring接口 需要获取方法上请求路径
                        if (!isOldMS){
                            List<JavaAnnotation> methodAnnotations = method.getAnnotations();
                            for (JavaAnnotation methodAnnotation : methodAnnotations) {
                                String methodAnnotationName = methodAnnotation.getType().getValue();
                                boolean requestMappingA = "RequestMapping".equals(methodAnnotationName) || "org.springframework.web.bind.annotation.RequestMapping".equals(methodAnnotationName);
                                boolean postMappingA = "PostMapping".equals(methodAnnotationName) || "org.springframework.web.bind.annotation.PostMapping".equals(methodAnnotationName);
                                boolean getMappingA = "GetMapping".equals(methodAnnotationName) || "org.springframework.web.bind.annotation.GetMapping".equals(methodAnnotationName);
                                //方法上没有路径注解 跳过
                                if ( !requestMappingA && !postMappingA && !getMappingA){
                                    continue;
                                }else {
                                    if (methodAnnotation.getNamedParameter("value") != null) {
                                        String value = methodAnnotation.getNamedParameter("value").toString();
                                        finalReqUrl += (!StringUtils.isEmpty(value) ? value.replaceAll("'", "").replaceAll("\"", "").replace("[","").replace("]",""): "").trim();
                                    }
                                }
                                if (postMappingA || (requestMappingA && "RequestMethod.POST".equals(methodAnnotation.getNamedParameter("method").toString()))){
                                    reqType = 1;
                                }
                                break;
                            }
                        }else {
                            // 处理旧微服务接口路径 从注释中获取
                            DocletTag pathTag = method.getTagByName("path");
                            if(pathTag != null){
                                finalReqUrl = pathTag.getValue();
                            }
                        }
                        JavaApiInfo javaApiInfo = new JavaApiInfo();

                        List<DocletTag> docletAuthorTagList = javaClass.getTagsByName("author");
                        StringBuilder authorBuilder = new StringBuilder();
                        if (docletAuthorTagList != null && docletAuthorTagList.size() > 0){
                            for (DocletTag tag : docletAuthorTagList) {
                                authorBuilder.append(tag.getValue());
                                authorBuilder.append(", ");
                            }
                        }
                        javaApiInfo.setPath(finalReqUrl);
                        if (reqType == 1){
                            javaApiInfo.setMethod("POST");
                        }else {
                            javaApiInfo.setMethod("GET");
                        }
                        String token = apiConfig.getRpcToken();
                        if (apiConfig.getApplicationName().endsWith("api")){
                            if (finalReqUrl.contains("web/webApi")){
                                token = apiConfig.getH5Token();
                            }else {
                                token = apiConfig.getApiToken();
                            }
                        }else if(apiConfig.getApplicationName().endsWith("manager")){
                            token = apiConfig.getManagerToken();
                        }else if(apiConfig.getApplicationName().endsWith("game")){
                            token = apiConfig.getGameToken();
                        }
                        javaApiInfo.setToken(token);
                        javaApiInfo.setApplicationName(apiConfig.getApplicationName());
                        //标题
                        String comment = method.getComment();
                        javaApiInfo.setTitle(comment);

                        //接口方法的状态 完成 未完成
                        javaApiInfo.setStatus( getMethodStatus(method));
                        //作者
                        List<DocletTag> authorTagList = method.getTagsByName("author");
                        StringBuilder methodAuthorBuilder = new StringBuilder();
                        if (authorTagList != null && authorTagList.size() > 0){
                            for (DocletTag tag : authorTagList) {
                                methodAuthorBuilder.append(tag.getValue());
                                methodAuthorBuilder.append(", ");
                            }
                        }
                        String author ;
                        if (methodAuthorBuilder.length() > 0){
                            author = methodAuthorBuilder.toString();
                        }else {
                            author = authorBuilder.toString();
                        }
                        if (author.length() > 1){
                            author = author.substring(0,author.length() - 2 );
                        }
                        javaApiInfo.setDesc(String.format("<p>作者: %s</p>", author));


                        List<DocletTag> nullablesTags = method.getTagsByName("nullable");
                        //参数处理

                        req( method.getTagsByName("param"), nullablesTags.size() == 1 ? nullablesTags.get(0) : null, javaApiInfo);
                        //返回值处理
                        List<DocletTag> returnTags = method.getTagsByName("return");
                        if (returnTags.size() == 1) {
                            DocletTag returnTag = returnTags.get(0);
                            String returnLink = returnTag.getValue();
                            //取link的对象
                            if (returnLink.contains("@link")){
                                returnLink = returnLink.split("@link")[1].replace("}","").trim();
                                if (javaClassMap.get(returnLink) == null){
                                    returnLink = null;
                                }
                            }
                            //resp 处理返回参数
                            resp(returnLink, javaApiInfo);
                        }
                        //同步到Yapi
                        try {
                            boolean result = ImportYapiUtil.importToYapi(javaApiInfo);
                            System.out.println("同步接口 " + javaApiInfo.getPath() + " 结果 : " + result);
                        } catch (Exception e) {
                            System.err.println("同步 yapi 平台发生IO异常 数据为 : " + JSON.toJSONString(javaApiInfo));
                        }

                    }

                }
            }
        }
    }

    //请求参数
    private static void req ( List<DocletTag> methodParams, DocletTag nullAbleTag, JavaApiInfo apiInfo) {
        System.out.println("请求参数 : ");
        List<String> nullAbleParamList = new ArrayList<>();
        if (nullAbleTag != null) {
            String nullTagParamStr = nullAbleTag.getValue();
            nullAbleParamList = Arrays.asList(nullTagParamStr.split(","));
        }
        if (methodParams.size() > 0) {
            List<ReqForm> reqForms = new ArrayList<>();
            for (DocletTag methodParam : methodParams) {
                String methodParamValue = methodParam.getValue();
                //请求参数处理
                if (methodParams.size() == 1 && methodParamValue.contains("@link")) {
                    String link = methodParamValue.split("@link")[1].replace("}","").trim();
                    if (javaClassMap.get(link) == null){
                        link = null;
                    }
                    //对象
                    apiInfo.setReq_body_is_json_schema(true);
                    apiInfo.setRes_body("json");
                    //序列化 成 json字符串

                    apiInfo.setReq_body_other(JSON.toJSONString(obj(link)));
                    ReqHeader[] reqHeaders = new ReqHeader[1];
                    List<ReqHeader> reqHeaderList = Collections.singletonList(new ReqHeader("Content-Type","application/json"));
                    apiInfo.setReq_body_type("json");
                    apiInfo.setReq_headers(reqHeaderList.toArray(reqHeaders));
                    return;
                } else {
                    //单参数
                    singleParam(methodParam, reqForms , nullAbleParamList);
                }
            }
            if (reqForms.size() > 0){
                ReqForm[] reqFormsArray = new ReqForm[reqForms.size()];
                apiInfo.setReq_query( reqForms.toArray(reqFormsArray));
                ReqHeader[] reqHeaders = new ReqHeader[1];
                List<ReqHeader> reqHeaderList = Collections.singletonList(new ReqHeader("Content-Type","application/x-www-form-urlencoded"));
                apiInfo.setReq_headers(reqHeaderList.toArray(reqHeaders));
            }
        }
    }

    //返回值
    private static void resp (String returnLink, JavaApiInfo apiInfo) {
        BodyJson baseRespBodyJson = new BodyJson();
        baseRespBodyJson.setType("object");
        HashMap<String, BodyJson> map = new HashMap<>(3);
        map.put("code",new BodyJson("int","code值"));
        map.put("msg", new BodyJson("string","code描述"));
        map.put("data", new BodyJson("object","内容对象"));
        baseRespBodyJson.setProperties(map);

        //对象
        BodyJson bodyJson = obj(returnLink);
        //二次处理
        if ( bodyJson != null){
            baseRespBodyJson.getProperties().get("data").setProperties(bodyJson.getProperties());
            baseRespBodyJson.getProperties().get("data").setType("object");
        }
        //列化 成 json字符串
        apiInfo.setRes_body(JSON.toJSONString(baseRespBodyJson));
    }

    //单参数
    private static void singleParam (DocletTag methodParam ,List<ReqForm> reqForms ,List<String> nullAbleParamList) {
        String[] array = methodParam.getValue().split(" ");
        //参数名称
        String parameterName = array[0];
        //参数注释
        String parameterComment = array[1];

        //是否必传
        reqForms.add(new ReqForm(nullAbleParamList.contains(parameterName) ? 1 : 0, parameterName, parameterComment));
    }

    //单字段
    private static BodyJson singleField (JavaField fieldDoc) {
        BodyJson bodyJson = new BodyJson();
        String type = fieldDoc.getType().getName();
        if ("List".equals(type) || "Set".equals(type)) {
            //集合
            type = "array";
        }
        bodyJson.setType(type);
        bodyJson.setDescription(fieldDoc.getComment());
        return bodyJson;
    }

    //对象
    private static BodyJson obj (String link) {
        if (StringUtils.isEmpty(link)){
            return null;
        }
        BodyJson bodyJson = new BodyJson();
        JavaClass javaClass = javaClassMap.get(link);
        if (javaClass == null){
            return null;
        }

        //输出私有属性的注释
        List<JavaField> fields = javaClass.getFields();
        List<String > unNullPropertiesList = new ArrayList<>();
        for (JavaField fieldDoc : fields) {
            BodyJson bodyProperties = new BodyJson();
            DocletTag seeTag = fieldDoc.getTagByName("see");
            if (seeTag != null) {
                BodyJson linkBodyJson = obj(seeTag.getValue().split(" ")[0]);
                String type = "object";
                //对象
                if ("List".equals(fieldDoc.getType().getName()) || "Set".equals(fieldDoc.getType().getName())) {
                    //集合
                    type = "array";
                    bodyProperties.setItems(linkBodyJson);
                }else {
                    if (linkBodyJson != null){
                        bodyProperties.setProperties(linkBodyJson.getProperties());
                    }
                }
                bodyProperties.setType(type);
                bodyProperties.setDescription(fieldDoc.getComment());
                //对象
            } else {
                //单字段
                bodyProperties = singleField(fieldDoc);
            }
            if (bodyJson.getProperties() == null ){
                bodyJson.setProperties(new HashMap<>(fields.size()));
            }
            bodyJson.getProperties().put(fieldDoc.getName(), bodyProperties );
            //取字段上的注解
            List<JavaAnnotation> annotations = fieldDoc.getAnnotations();
            List<JavaAnnotation> nullable = annotations.stream().filter(annotation ->
                    "Nullable".equals(annotation.getType().getName())).collect(Collectors.toList());
            //没有标注是可以为null 的字段 就是必传字段
            if (CollectionUtils.isEmpty(nullable)){
                unNullPropertiesList.add(fieldDoc.getComment());
            }
        }
        String[] required = new String[unNullPropertiesList.size()];
        bodyJson.setRequired(unNullPropertiesList.toArray(required));
        return bodyJson;
    }

    //获取方法完成状态
    private static String getMethodStatus (JavaMethod method) {
        DocletTag statusTag = method.getTagByName("status");
        String status = "undone";
        if (statusTag != null ) {
            if ("true".equals(statusTag.getValue())) {
                status = "done";
            }
        }
        return status;
    }
}
