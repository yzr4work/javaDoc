package com.yzr.speechmatching.util;

import com.alibaba.fastjson.JSON;
import com.sun.javadoc.*;
import com.yzr.speechmatching.model.yapi.BodyJson;
import com.yzr.speechmatching.model.yapi.JavaApiInfo;
import com.yzr.speechmatching.model.yapi.ReqForm;
import com.yzr.speechmatching.model.yapi.ReqHeader;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JavaDocUse {
    //TODO 缓存已经加载过的类 classDoc

    private static RootDoc rootDoc;

    public static class Doclet {
        public static boolean start (RootDoc rootDoc) {
            JavaDocUse.rootDoc = rootDoc;
            return true;
        }
    }

    /**
     * 显示DocRoot中的基本信息
     */
    public static void show (String classPath, JavaApiInfo apiInfo, String methodName) {
        ClassDoc[] classes = rootDoc.classes();
        for (ClassDoc classDoc : classes) {
            begin(classPath, classDoc, apiInfo, methodName);
        }
    }


    //入口
    private static void begin (String classBasePath, ClassDoc classDoc, JavaApiInfo apiInfo, String methodName) {
        MethodDoc[] methodDocs = classDoc.methods();
        Tag[] authorList = classDoc.tags("author");
        StringBuilder authorBuilder = new StringBuilder();
        if (authorList != null && authorList.length > 0){
            for (Tag tag : authorList) {
                authorBuilder.append(tag.text());
                authorBuilder.append(", ");
            }
        }
        for (MethodDoc methodDoc : methodDocs) {
            //fixme 暂不支持同名方法
            if (!methodName.equals(methodDoc.name())){
                continue;
            }
            //方法的注释
            apiInfo.setTitle(methodDoc.commentText());
            //接口方法的状态 完成 未完成
            apiInfo.setStatus( getMethodStatus(methodDoc));
            Tag[] nullablesTags = methodDoc.tags("nullable");
            Tag[] authorTagList = methodDoc.tags("author");
            StringBuilder methodAuthorBuilder = new StringBuilder();
            if (authorTagList != null && authorTagList.length > 0){
                for (Tag tag : authorTagList) {
                    methodAuthorBuilder.append(tag.text());
                    methodAuthorBuilder.append(", ");
                }
            }
            String author = "";
            if (methodAuthorBuilder.length() > 0){
                author = methodAuthorBuilder.toString();
            }else {
                author = authorBuilder.toString();
            }
            if (author.length() > 1){
                author = author.substring(0,author.length() - 2 );
            }
            apiInfo.setDesc(String.format("<p>作者: %s</p>", author));
            //req 处理请求参数
            req(classBasePath, methodDoc.tags("param"), nullablesTags.length == 1 ? nullablesTags[0] : null, apiInfo);
            //resp 返回参数处理
            Tag[] returnTags = methodDoc.tags("return");
            if (returnTags.length == 1) {
                Tag returnTag = returnTags[0];
                SeeTag linkTag = null;
                if (returnTag.inlineTags().length > 1) {
                    Tag tag = returnTag.inlineTags()[1];
                    if ("@link".equals(tag.name())) {
                        //取link的对象的class
                        linkTag = (SeeTag) tag;
                    }
                }
                //resp 处理返回参数
                resp(classBasePath, linkTag == null ? null : linkTag.referencedClass(), methodDoc.returnType(), apiInfo);
            }
            //同步到Yapi
            try {
                boolean result = ImportYapiUtil.importToYapi(apiInfo);
                System.out.println("同步接口 " + apiInfo.getPath() + " 结果 : " + result);
            } catch (IOException e) {
                System.err.println("同步 yapi 平台发生IO异常 数据为 : " + JSON.toJSONString(apiInfo));
            }
        }
    }

    //获取方法完成状态
    private static String getMethodStatus (MethodDoc methodDoc) {
        Tag[] statuses = methodDoc.tags("status");
        String status = "undone";
        if (statuses.length == 1) {
            if ("true".equals(statuses[0].text())) {
                status = "done";
            }
        }
        return status;
    }

    //请求参数
    private static void req (String classBasePath, Tag[] methodParams, Tag nullAbleTag, JavaApiInfo apiInfo) {
        System.out.println("请求参数 : ");
        List<String> nullAbleParamList = new ArrayList<>();
        if (nullAbleTag != null) {
            String nullTagParamStr = nullAbleTag.text();
            nullAbleParamList = Arrays.asList(nullTagParamStr.split(","));
        }
        if (methodParams.length > 0) {
            List<ReqForm> reqForms = new ArrayList<>();
            for (Tag methodParam : methodParams) {
                ParamTag paramTag = (ParamTag) methodParam;
                //请求参数处理
                Tag[] inlineTags = paramTag.inlineTags();
                if (inlineTags.length >= 2 && "@link".equals(inlineTags[1].name())) {
                    SeeTag linkTag = (SeeTag) inlineTags[1];
                    //对象
                    //fixme  参数中包含多个对象的引用连接 存在覆盖问题
                    apiInfo.setReq_body_is_json_schema(true);
                    apiInfo.setRes_body("json");
                    //序列化 成 json字符串
                    apiInfo.setReq_body_other(JSON.toJSONString(obj(classBasePath, linkTag.referencedClass())));
                    ReqHeader[] reqHeaders = new ReqHeader[1];
                    List<ReqHeader> reqHeaderList = Collections.singletonList(new ReqHeader("Content-Type","application/json"));
                    apiInfo.setReq_body_type("json");
                    apiInfo.setReq_headers(reqHeaderList.toArray(reqHeaders));
                    return;
                } else {
                    //单参数
                    singleParam(paramTag, nullAbleParamList.contains(paramTag.parameterName()), reqForms);
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
    private static void resp (String classBasePath, ClassDoc classDoc, Type type, JavaApiInfo apiInfo) {
        BodyJson baseRespBodyJson = obj(classBasePath, type.asClassDoc());
        //对象
        BodyJson bodyJson = obj(classBasePath, classDoc);
        //二次处理
        if ( bodyJson != null && baseRespBodyJson!= null){
            baseRespBodyJson.getProperties().get("data").setProperties(bodyJson.getProperties());
            baseRespBodyJson.getProperties().get("data").setType("object");
        }
        //列化 成 json字符串
        apiInfo.setRes_body(JSON.toJSONString(baseRespBodyJson));
    }


    //单参数
    private static void singleParam (ParamTag paramTag, boolean nullAble,List<ReqForm> reqForms) {
        //参数名称
        String parameterName = paramTag.parameterName();
        //参数注释
        String parameterComment = paramTag.parameterComment();
        //是否必传
        reqForms.add(new ReqForm(nullAble ? 1 : 0, parameterName, parameterComment));
    }

    //单字段
    private static BodyJson singleField (FieldDoc fieldDoc) {
        BodyJson bodyJson = new BodyJson();
        String type = fieldDoc.type().typeName();
        if ("List".equals(type) || "Set".equals(type)) {
            //集合
            type = "array";
        }
        bodyJson.setType(type);
        bodyJson.setDescription(fieldDoc.commentText());
        return bodyJson;
    }

    //对象
    private static BodyJson obj (String classBasePath, ClassDoc linkClassDoc) {
        if (linkClassDoc == null){
            return null;
        }
        BodyJson bodyJson = new BodyJson();
        ClassDoc[] innerClassDoc = getInnerClassDoc(classBasePath, linkClassDoc);
        List<String > unNullPropertiesList = new ArrayList<>();
        for (ClassDoc classDoc : innerClassDoc) {
            //输出私有属性的注释
            FieldDoc[] fieldDocs = classDoc.fields(false);
            for (FieldDoc fieldDoc : fieldDocs) {
                BodyJson bodyProperties = new BodyJson();
                if (fieldDoc.tags("@see").length == 1) {
                    SeeTag tag = (SeeTag) fieldDoc.tags("@see")[0];
                    BodyJson linkBodyJson = obj(classBasePath, tag.referencedClass());
                    String type = "object";
                    //对象
                    if ("List".equals(fieldDoc.type().typeName()) || "Set".equals(fieldDoc.type().typeName())) {
                        //集合
                        type = "array";
                        bodyProperties.setItems(linkBodyJson);
                    }else {
                        bodyProperties.setProperties(linkBodyJson.getProperties());
                    }
                    bodyProperties.setType(type);
                    bodyProperties.setDescription(fieldDoc.commentText());
                    //对象
                } else {
                    //单字段
                    bodyProperties = singleField(fieldDoc);
                }
                if (bodyJson.getProperties() == null ){
                    bodyJson.setProperties(new HashMap<>(fieldDocs.length));
                }
                bodyJson.getProperties().put(fieldDoc.name(), bodyProperties );
                //取字段上的注解
                AnnotationDesc[] annotations = fieldDoc.annotations();
                List<AnnotationDesc> nullable = Arrays.stream(annotations).filter(annotation ->
                        Objects.equals(annotation.annotationType().typeName(), "Nullable")).collect(Collectors.toList());
                //没有标注是可以为null 的字段 就是必传字段
                if (CollectionUtils.isEmpty(nullable)){
                    unNullPropertiesList.add(fieldDoc.name());
                }
            }

        }
        String[] required = new String[unNullPropertiesList.size()];
        bodyJson.setRequired(unNullPropertiesList.toArray(required));
        return bodyJson;
    }

    //获取内部的连接的源码doc
    private static ClassDoc[] getInnerClassDoc (String classBasePath, ClassDoc referencedClassDoc) {
        String linkClassPath = referencedClassDoc.qualifiedName();
        com.sun.tools.javadoc.Main.execute(new String[]{"-doclet",
                Doclet.class.getName(),
                "-encoding", "utf-8", "-private",
                classBasePath.replace("/target/classes/", "") + "/src/main/java/" + linkClassPath.replace(".", "/") + ".java"});
        return rootDoc.classes();
    }
}

