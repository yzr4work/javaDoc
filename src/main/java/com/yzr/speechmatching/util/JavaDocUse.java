package com.yzr.speechmatching.util;

import com.alibaba.fastjson.JSON;
import com.sun.javadoc.*;
import com.yzr.speechmatching.model.*;
import org.springframework.util.CollectionUtils;

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

            //req 处理请求参数
            req(classBasePath, methodDoc.tags("param"), nullablesTags.length == 1 ? nullablesTags[0] : null, apiInfo);
            //resp 返回参数处理
            Tag[] returnTags = methodDoc.tags("return");
            if (returnTags.length == 1) {
                Tag returnTag = returnTags[0];
                SeeTag linkTag = null;
                if (returnTag.inlineTags().length > 1) {
                    Tag tag = returnTag.inlineTags()[1];
                    if (tag.name().equals("@link")) {
                        //取link的对象的class
                        linkTag = (SeeTag) tag;
                    }
                }
                //resp 处理返回参数
                resp(classBasePath, linkTag == null ? null : linkTag.referencedClass(), methodDoc.returnType(), apiInfo);
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
            List<ReqBodyForm> reqBodyForms = new ArrayList<>();
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
                    BodyJsonWithSchema bodyJsonWithSchema = new BodyJsonWithSchema(obj(classBasePath, linkTag.referencedClass()));
                    //序列化 成 json字符串
                    apiInfo.setReq_body_other(JSON.toJSONString(bodyJsonWithSchema));
                } else {
                    //单参数
                    singleParam(paramTag, nullAbleParamList.contains(paramTag.parameterName()), reqBodyForms);
                }
            }
            if (reqBodyForms.size() > 0){
                ReqBodyForm[] reqBodyFormsArray = new ReqBodyForm[reqBodyForms.size()];
                apiInfo.setReq_body_form( reqBodyForms.toArray(reqBodyFormsArray));
                apiInfo.setRes_body("from");
            }
        }
    }

    //返回值
    private static void resp (String classBasePath, ClassDoc classDoc, Type type, JavaApiInfo apiInfo) {
        System.out.println("返回参数 : ");
        BodyJson baseRespBodyJson = obj(classBasePath, type.asClassDoc());
        //对象
        BodyJson bodyJson = obj(classBasePath, classDoc);
        //二次处理
        if ( bodyJson != null && baseRespBodyJson!= null){
            baseRespBodyJson.getProperties().get("data").setProperties(bodyJson);
        }
        //列化 成 json字符串
        apiInfo.setRes_body(JSON.toJSONString(baseRespBodyJson));
    }


    //单参数
    private static void singleParam (ParamTag paramTag, boolean nullAble,List<ReqBodyForm> reqBodyForms) {
        //参数名称
        String parameterName = paramTag.parameterName();
        //参数注释
        String parameterComment = paramTag.parameterComment();
        //是否必传
        System.out.println(parameterName + "   " + parameterComment + "   " + nullAble );
        reqBodyForms.add(new ReqBodyForm(nullAble ? 1 : 0, parameterName, parameterComment));
    }

    //单字段
    private static BodyProperties singleField (FieldDoc fieldDoc) {
        BodyProperties bodyProperties = new BodyProperties();
        String type = fieldDoc.type().typeName();
        if ("List".equals(type) || "Set".equals(type)) {
            //集合
            type = "array";
        }
        bodyProperties.setType(type);
        bodyProperties.setDescription(fieldDoc.commentText());
        System.out.println(fieldDoc.name() + "   " + type + "   " + fieldDoc.commentText());
        return bodyProperties;
    }

//    //集合
//    private static void collection (String classBasePath, ClassDoc classDoc) {
//        //说明是集合里面的元素
//        Tag[] seeTags = classDoc.tags("@see");
//        if (seeTags.length > 0) {
//            //对象
//            SeeTag seeTag = (SeeTag) seeTags[0];
//            obj(classBasePath, seeTag.referencedClass());
//        }
//    }

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
            System.out.println("属性参数 : ");
            for (FieldDoc fieldDoc : fieldDocs) {

                BodyProperties bodyProperties = new BodyProperties();
                if (fieldDoc.tags("@see").length == 1) {
                    SeeTag tag = (SeeTag) fieldDoc.tags("@see")[0];
                    System.out.println(fieldDoc.name() + "    " +  fieldDoc.commentText() + "  " + fieldDoc.type().typeName() );
                    BodyJson linkBodyJson = obj(classBasePath, tag.referencedClass());
                    String type = "obj";
                    //对象
                    if ("List".equals(fieldDoc.type().typeName()) || "Set".equals(fieldDoc.type().typeName())) {
                        //集合
                        type = "array";
                        bodyProperties.setItems(linkBodyJson);
                    }else {
                        bodyProperties.setProperties(bodyJson);
                    }
                    bodyProperties.setType(type);
                    //对象
                } else {
                    //单字段
                    bodyProperties = singleField(fieldDoc);
                }
                if (bodyJson.getProperties() == null ){
                    bodyJson.setProperties(new HashMap<>());
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
                "-encoding", "utf-8", "-private", "-classpath",
                classBasePath,
                classBasePath.replace("/target/classes/", "") + "/src/main/java/" + linkClassPath.replace(".", "/") + ".java"});
        return rootDoc.classes();
    }
}

