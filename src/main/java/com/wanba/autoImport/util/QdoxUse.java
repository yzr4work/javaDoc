package com.wanba.autoImport.util;

import com.alibaba.fastjson.JSON;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.*;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
import com.wanba.autoImport.model.ApiConfig;
import com.wanba.autoImport.model.yapi.BodyJson;
import com.wanba.autoImport.model.yapi.JavaApiInfo;
import com.wanba.autoImport.model.yapi.ReqForm;
import com.wanba.autoImport.model.yapi.ReqHeader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class QdoxUse {
    private static final Map<String, JavaClass> javaClassMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> tokenCatIdMap = new ConcurrentHashMap<>();

    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2, 1, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(1000));


    //入口
    public static void begin(ApiConfig apiConfig) {
        for (String filePath : apiConfig.getSourceTreeList()) {
            JavaProjectBuilder builder = new JavaProjectBuilder();
            builder.addSourceTree(new File(filePath));
            Collection<JavaClass> classes = builder.getClasses();
            for (JavaClass javaClass : classes) {
                //除外文件
                if (!javaClass.getName().endsWith("Service")
                        && !javaClass.getFullyQualifiedName().endsWith("DAO")
                        && !javaClass.getFullyQualifiedName().endsWith("Impl")
                        && !javaClass.getFullyQualifiedName().endsWith("Util")) {
                    javaClassMap.put(javaClass.getName(), javaClass);
                }
            }
        }
        generateDoc(apiConfig);
    }


    private static void generateDoc(ApiConfig apiConfig) {
        int apiCount = 0;
        for (Map.Entry<String, JavaClass> m : javaClassMap.entrySet()) {
            JavaClass javaClass = m.getValue();
            //获取类上面的注解
            List<JavaAnnotation> annotations = javaClass.getAnnotations();
            //type 代表不同的接口类型 0为正常的spring接口 1为旧微服务
            for (JavaAnnotation annotation : annotations) {
                String annotationName = annotation.getType().getValue();
                //判断当前类是否是controller 或者是老的微服务
                boolean isOldMS = false;
                if (javaClass.getSuperJavaClass() != null) {
                    isOldMS = "com.wb.microserver.MsFunction".equals(javaClass.getSuperJavaClass().getFullyQualifiedName());

                }
                if ("Controller".equals(annotationName) ||
                        "RestController".equals(annotationName) ||
                        isOldMS) {
                    apiCount = parseJavaDoc(apiConfig, apiCount, javaClass, annotations, isOldMS);

                }
            }
        }
        int i = 0;
        while (true) {
            i++;
            if (threadPoolExecutor.getActiveCount() == 0) {
                System.out.println("生成JavaDoc 文档 " + apiCount + "个");
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //10秒没同步完关闭不再同步
            if (i >= 20) {
                return;
            }
        }
    }

    private static int parseJavaDoc(ApiConfig apiConfig, int apiCount, JavaClass javaClass, List<JavaAnnotation> annotations, boolean isOldMs) {
        String reqPath = "";
        //获取类上的接口路径
        for (JavaAnnotation javaAnnotation : annotations) {
            if (!"org.springframework.web.bind.annotation.RequestMapping".equals(javaAnnotation.getType().getSimpleName()) || !"RequestMapping".equals(javaAnnotation.getType().getSimpleName())) {
                //处理方法
                apiCount = genderMethods(apiConfig, apiCount, javaClass, isOldMs, reqPath,  javaClass.getMethods());
            }else {
                if (javaAnnotation.getNamedParameter("value") != null) {
                    List valueList = (LinkedList)javaAnnotation.getNamedParameter("value");
                    if (CollectionUtils.isNotEmpty( valueList)){
                        for (Object o : valueList) {
                            String value = o.toString();
                            reqPath = StringUtils.isNotEmpty(value) ? value.replaceAll("'", "").replaceAll("\"", "") : "";
                            // 处理方法
                            apiCount = genderMethods(apiConfig, apiCount, javaClass, isOldMs, reqPath,  javaClass.getMethods());
                        }
                    }
                }
            }
        }

        return apiCount;
    }

    private static int genderMethods(ApiConfig apiConfig, int apiCount, JavaClass javaClass, boolean isOldMs, String reqPath, List<JavaMethod> methods) {
        for (JavaMethod method : methods) {
            //私有方法跳过 或 没有注释的接口跳过
            if (method.isPrivate() || StringUtils.isEmpty(method.getComment())) {
                continue;
            }
            //
            int reqType = 0;
            String finalReqUrl = reqPath;
            try {
                //正常spring接口 需要获取方法上请求路径
                if (!isOldMs) {
                    // 处理旧微服务接口路径 从注释中获取
                    DocletTag pathTag = method.getTagByName("path");
                    if (pathTag != null) {
                        finalReqUrl = pathTag.getValue();
                    } else {
                        //获取不到接口路径 跳出
                        break;
                    }
                } else {
                    List<JavaAnnotation> methodAnnotations = method.getAnnotations();
                    for (JavaAnnotation methodAnnotation : methodAnnotations) {
                        if (methodAnnotation != null) {
                            String methodAnnotationName = methodAnnotation.getType().getValue();
                            boolean requestMappingA = "RequestMapping".equals(methodAnnotationName) || "org.springframework.web.bind.annotation.RequestMapping".equals(methodAnnotationName);
                            boolean postMappingA = "PostMapping".equals(methodAnnotationName) || "org.springframework.web.bind.annotation.PostMapping".equals(methodAnnotationName);
                            boolean getMappingA = "GetMapping".equals(methodAnnotationName) || "org.springframework.web.bind.annotation.GetMapping".equals(methodAnnotationName);
                            //方法上没有路径注解 跳过
                            if (requestMappingA || postMappingA || getMappingA) {
                                if (methodAnnotation.getNamedParameter("value") != null) {
                                    List valueList = (LinkedList)methodAnnotation.getNamedParameter("value");
                                    if (CollectionUtils.isNotEmpty( valueList)){
                                        for (Object o : valueList) {
                                            String value = o.toString();
                                            reqPath = StringUtils.isNotEmpty(value) ? value.replaceAll("'", "").replaceAll("\"", "") : "";
                                            //接口路径
                                            String path = finalReqUrl + reqPath;
                                            finalGenderDoc(apiConfig, javaClass, method, reqType, path);
                                        }
                                    }
                                }
                            }
                            Object controllerUrlMethod = methodAnnotation.getNamedParameter("method");
                            boolean requestMappingWithPostMethod = requestMappingA && controllerUrlMethod != null && "RequestMethod.POST".equals(controllerUrlMethod.toString());
                            if (postMappingA || requestMappingWithPostMethod) {
                                reqType = 1;
                            }
                        }
                    }
                }

                if (finalGenderDoc(apiConfig, javaClass, method, reqType, finalReqUrl)) {
                    break;
                }
                apiCount++;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return apiCount;
    }

    private static boolean finalGenderDoc(ApiConfig apiConfig, JavaClass javaClass, JavaMethod method, int reqType, String finalReqUrl) {
        JavaApiInfo javaApiInfo = new JavaApiInfo();

        StringBuilder authorBuilder = new StringBuilder();
        genderAuthorInfo(javaClass.getTagsByName("author"), authorBuilder);
        javaApiInfo.setPath(finalReqUrl);
        if (reqType == 1) {
            javaApiInfo.setMethod("POST");
        } else {
            javaApiInfo.setMethod("GET");
        }
        String token = apiConfig.getRpcToken();
        int type = 2;
        if (apiConfig.getApplicationName().endsWith("api")) {
            if (finalReqUrl.contains("web/webApi")) {
                token = apiConfig.getH5Token();
            } else {
                token = apiConfig.getApiToken();
            }
            type = 1;
        } else if (apiConfig.getApplicationName().endsWith("manager")) {
            token = apiConfig.getManagerToken();
        } else if (apiConfig.getApplicationName().endsWith("game")) {
            token = apiConfig.getGameToken();
        }
        if(token  == null || token.trim().length() == 0){
            return true;
        }
        javaApiInfo.setToken(token);
        javaApiInfo.setApplicationName(apiConfig.getApplicationName());
        //创建文件夹
        Integer catId = createCat(apiConfig, token);
        //标题
        String comment = method.getComment();
        javaApiInfo.setTitle(comment);

        //接口方法的状态 完成 未完成
        javaApiInfo.setStatus(getMethodStatus(method));
        //作者

        buildAuthor(method, javaApiInfo, authorBuilder);

        List<DocletTag> nullAblesTags = method.getTagsByName("nullable");
        //参数处理
        req(method.getTagsByName("param"), nullAblesTags.size() == 1 ? nullAblesTags.get(0) : null, javaApiInfo, type);
        //返回值处理
        genderResp(method, javaApiInfo, type);

        submitImportTask(javaApiInfo, catId);
        return false;
    }


    private static Integer createCat(ApiConfig apiConfig, String token) {
        //创建文件夹  缓存map map中没有 再创建
        Integer catId = tokenCatIdMap.get(token);
        if (catId == null) {
            catId = ImportYapiUtil.getCatId(token, apiConfig.getApplicationName());
            tokenCatIdMap.put(token, catId);
        }
        return catId;
    }

    private static void submitImportTask(JavaApiInfo javaApiInfo, Integer finalCatId) {
        threadPoolExecutor.submit(() -> {
            try {
                boolean result = ImportYapiUtil.importToYapi(javaApiInfo, finalCatId);
                System.out.println("同步接口 " + javaApiInfo.getPath() + " 结果 : " + result + " 文件夹 : " + javaApiInfo.getApplicationName());
            } catch (Exception e) {
                System.out.println("接口 同步失败 : " + javaApiInfo.getPath());
                e.printStackTrace();
            }
            return null;
        });
    }

    private static void genderResp(JavaMethod method, JavaApiInfo javaApiInfo, int type) {
        List<DocletTag> returnTags = method.getTagsByName("return");
        String returnLink = null;
        DefaultJavaParameterizedType returns = (DefaultJavaParameterizedType) method.getReturns();
        String[] strings = returns.getSimpleName().split("\\.");
        String returnName = strings[strings.length - 1];
        List<JavaType> actualTypeArguments = returns.getActualTypeArguments();
        if (!CollectionUtils.isEmpty(actualTypeArguments)) {
            returnLink = actualTypeArguments.get(0).getBinaryName();
        }
        if (returnTags.size() == 1) {
            DocletTag returnTag = returnTags.get(0);
            //取link的对象
            if (returnTag.getValue().contains("@link")) {
                returnLink = returnTag.getValue().split("@link")[1].replace("}", "").trim();
                if (javaClassMap.get(returnLink) == null) {
                    returnLink = null;
                }
            }
            //resp 处理返回参数
        }
        resp(returnName, returnLink, javaApiInfo, type);
    }

    private static void genderAuthorInfo(List<DocletTag> author, StringBuilder authorBuilder) {
        if (author != null && author.size() > 0) {
            for (DocletTag tag : author) {
                authorBuilder.append(tag.getValue());
                authorBuilder.append(", ");
            }
        }
    }

    private static void buildAuthor(JavaMethod method, JavaApiInfo javaApiInfo, StringBuilder authorBuilder) {
        genderAuthorInfo(method.getTagsByName("author"), authorBuilder);
        String author;
        author = authorBuilder.toString();
        if (author.length() > 1) {
            author = author.substring(0, author.length() - 2);
        }
        javaApiInfo.setDesc(String.format("<p>作者: %s</p>", author));
    }


    //请求参数
    private static void req(List<DocletTag> methodParams, DocletTag nullAbleTag, JavaApiInfo apiInfo, int type) {
        List<String> nullAbleParamList = new ArrayList<>();
        if (nullAbleTag != null) {
            String nullTagParamStr = nullAbleTag.getValue();
            nullAbleParamList = Arrays.asList(nullTagParamStr.split(","));
        }
        if (methodParams.size() > 0) {
            List<ReqForm> reqForms = new ArrayList<>();
            BodyJson bodyJson = null;
            ArrayList<String> bodyRequiredList = new ArrayList<>();
            for (DocletTag methodParam : methodParams) {
                String methodParamValue = methodParam.getValue();
                //请求参数处理
                if (methodParamValue.contains("@link")) {
                    String[] splitArray = methodParamValue.split("@link");
                    String link = splitArray[1].replace("}", "").trim();
                    String[] split = splitArray[0].split(" ");
                    String paramName = split[0].trim();
                    // 判断link 的内容 是否为基础类型
                    if (isBaseType(link) && type == 1) {
                        //基础类型
                        singleParam(methodParam, reqForms, nullAbleParamList);
                    } else {
                        bodyJson = getReqBodyJson(apiInfo, nullAbleParamList, bodyJson, bodyRequiredList, link, split, paramName);
                    }
                }
            }
            if (reqForms.size() > 0) {
                ReqForm[] reqFormsArray = new ReqForm[reqForms.size()];
                apiInfo.setReq_query(reqForms.toArray(reqFormsArray));
                ReqHeader[] reqHeaders = new ReqHeader[1];
                List<ReqHeader> reqHeaderList = Collections.singletonList(new ReqHeader("Content-Type", "application/x-www-form-urlencoded"));
                apiInfo.setReq_headers(reqHeaderList.toArray(reqHeaders));
            } else {
                if (bodyJson != null) {
                    String[] required = new String[bodyRequiredList.size()];
                    bodyJson.setRequired(bodyRequiredList.toArray(required));
                    apiInfo.setReq_body_other(JSON.toJSONString(bodyJson));
                }
            }
        }
    }


    private static BodyJson getReqBodyJson(JavaApiInfo apiInfo, List<String> nullAbleParamList, BodyJson bodyJson, ArrayList<String> bodyRequiredList, String link, String[] split, String paramName) {
        //非基础类型
        //对象
        apiInfo.setReq_body_is_json_schema(true);
        apiInfo.setRes_body("json");
        //序列化 成 json字符串
        if (bodyJson == null) {
            bodyJson = new BodyJson();
            bodyJson.setType("object");
        }
        Map<String, BodyJson> properties = bodyJson.getProperties();
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(paramName, isBaseType(link) ? buildReqBody(changType(link), split[split.length - 2]) : obj(javaClassMap.get(link) == null ? null : link));
        bodyJson.setProperties(properties);
        ReqHeader[] reqHeaders = new ReqHeader[1];
        List<ReqHeader> reqHeaderList = Collections.singletonList(new ReqHeader("Content-Type", "application/json"));
        apiInfo.setReq_body_type("json");
        apiInfo.setReq_headers(reqHeaderList.toArray(reqHeaders));
        if (!nullAbleParamList.contains(paramName)) {
            bodyRequiredList.add(paramName);
        }
        return bodyJson;
    }

    private static BodyJson buildReqBody(String link, String description) {
        BodyJson bodyJson1 = new BodyJson();
        bodyJson1.setType(link);
        bodyJson1.setDescription(description == null ? "" : description.trim());
        return bodyJson1;
    }

    //返回值
    private static void resp(String returnName, String returnLink, JavaApiInfo apiInfo, int type) {
        BodyJson baseRespBodyJson = new BodyJson();
        baseRespBodyJson.setType("object");
        HashMap<String, BodyJson> map = new HashMap<>(3);
        if (type == 1) {
            map.put("code", new BodyJson("int", "code值"));
            map.put("msg", new BodyJson("string", "code描述"));
            map.put("data", new BodyJson("object", "内容对象"));
            String[] requiredArr = {"code", "msg", "data"};
            baseRespBodyJson.setRequired(requiredArr);
        } else if (type == 2) {
            map.put("cd", new BodyJson("int", "code值"));
            map.put("desc", new BodyJson("string", "code描述"));
            map.put("data", new BodyJson("object", "内容对象"));
            String[] requiredArr = {"cd", "desc", "data"};
            baseRespBodyJson.setRequired(requiredArr);
        }
        baseRespBodyJson.setProperties(map);


        //对象
        BodyJson bodyJson = obj(returnLink);
        if (!"MsRpcResponse".equals(returnName) && !"MsResponse".equals(returnName) && !"HttpRespBase".equals(returnName) && !"ManagerResponse".equals(returnName)) {
            apiInfo.setRes_body(JSON.toJSONString(obj(returnName)));
            return;
        }
        //二次处理
        if (bodyJson != null) {
            baseRespBodyJson.getProperties().get("data").setProperties(bodyJson.getProperties());
            baseRespBodyJson.getProperties().get("data").setType("object");
        }
        //列化 成 json字符串
        apiInfo.setRes_body(JSON.toJSONString(baseRespBodyJson));
    }

    //单参数
    private static void singleParam(DocletTag methodParam, List<ReqForm> reqForms, List<String> nullAbleParamList) {
        String[] array = methodParam.getValue().split(" ");
        //参数名称
        String parameterName = array[0];
        //参数注释
        String parameterComment = array.length > 1 ? array[1] : "";
        //是否必传
        reqForms.add(new ReqForm(nullAbleParamList.contains(parameterName) ? 0 : 1, parameterName, parameterComment));
    }

    //单字段
    private static BodyJson singleField(JavaField fieldDoc) {
        BodyJson bodyJson = new BodyJson();
        bodyJson.setType(changType(fieldDoc.getType().getName()));
        bodyJson.setDescription(fieldDoc.getComment());
        return bodyJson;
    }

    //对象
    private static BodyJson obj(String link) {
        if (StringUtils.isEmpty(link)) {
            return null;
        }
        BodyJson bodyJson = new BodyJson();
        JavaClass javaClass = javaClassMap.get(link);
        if (javaClass == null) {
            return null;
        }

        //输出私有属性的注释
        List<JavaField> fields = javaClass.getFields();
        List<String> unNullPropertiesList = new ArrayList<>();
        for (JavaField fieldDoc : fields) {
            if (fieldDoc.isStatic() || !fieldDoc.isPrivate()) {
                continue;
            }
            BodyJson bodyProperties = new BodyJson();
            JavaClass fieldDocType = fieldDoc.getType();

            if (fieldDocType.getFullyQualifiedName().contains(".") &&  !fieldDocType.getFullyQualifiedName().startsWith("java.lang")) {
                boolean isCollection = "List".equals(fieldDocType.getName()) || "Set".equals(fieldDocType.getName());
                String linkObj = fieldDocType.getName();
                if (isCollection) {
                    DefaultJavaParameterizedType fieldDocType1 = (DefaultJavaParameterizedType) fieldDocType;
                    List<JavaType> actualTypeArguments = fieldDocType1.getActualTypeArguments();
                    if (!CollectionUtils.isEmpty(actualTypeArguments)) {
                        linkObj = actualTypeArguments.get(0).getBinaryName();
                    }
                }
                BodyJson linkBodyJson = obj(linkObj);
                String type = "object";
                bodyProperties.setDescription(fieldDoc.getComment());
                //对象
                if (isCollection) {
                    //集合
                    type = "array";
                    String[] strings = linkObj.split("\\.");
                    if (linkObj.contains(".") ? isBaseType(strings[strings.length -1]) : isBaseType(linkObj)){
                        linkBodyJson = new BodyJson(changType(strings[strings.length -1]), "");
                    }
                    bodyProperties.setItems(linkBodyJson);
                } else {
                    if (linkBodyJson != null) {
                        bodyProperties.setProperties(linkBodyJson.getProperties());
                    }
                }
                if (isBaseType(linkObj)){
                    bodyProperties.setType(changType(linkObj));

                }else {
                    bodyProperties.setType(type);
                }
                //对象
            } else {
                //单字段
                bodyProperties = singleField(fieldDoc);
            }
            if (bodyJson.getProperties() == null) {
                bodyJson.setProperties(new HashMap<>(fields.size()));
            }
            bodyJson.getProperties().put(fieldDoc.getName(), bodyProperties);
            //取字段上的注解
            List<JavaAnnotation> annotations = fieldDoc.getAnnotations();
            List<JavaAnnotation> nullable = annotations.stream().filter(annotation ->
                    "Nullable".equals(annotation.getType().getName()) || "Null".equals(annotation.getType().getName())).collect(Collectors.toList());
            //没有标注是可以为null 的字段 就是必传字段
            if (CollectionUtils.isEmpty(nullable)) {
                unNullPropertiesList.add(fieldDoc.getName());
            }
        }
        String[] required = new String[unNullPropertiesList.size()];
        bodyJson.setRequired(unNullPropertiesList.toArray(required));
        return bodyJson;
    }

    //获取方法完成状态
    private static String getMethodStatus(JavaMethod method) {
        DocletTag statusTag = method.getTagByName("status");
        String status = "undone";
        if (statusTag != null) {
            if ("true".equals(statusTag.getValue())) {
                status = "done";
            }
        }
        return status;
    }

    /**
     * 判断是否是基础类型
     *
     * @param type 类型
     * @return 是基础类型 true 非基础类型 false
     */
    private static boolean isBaseType(String type) {
        boolean a = "int".equals(type) || "short".equals(type) || "long".equals(type) ||
                "double".equals(type) || "float".equals(type) ||
                "boolean".equals(type) || "String".equals(type) ||
                "char".equals(type) || "byte".equals(type);
        boolean b = "Integer".equals(type) || "Short".equals(type) || "Long".equals(type) ||
                "Double".equals(type) || "Float".equals(type) ||
                "Boolean".equals(type) || "Character".equals(type) || "Byte".equals(type) || "Date".equals(type);
        return a || b;
    }

    private static String changType(String type){
        if ("Integer".equals(type)){
            return "number";
        }
        if ("Date".equals(type)){
            return "String";
        }
        if ("List".equals(type) || "Set".equals(type)) {
            //集合
            type = "array";
        }
        return type;
    }
}
