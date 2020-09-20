package com.yzr.speechmatching.util;

import com.sun.javadoc.*;

import java.util.Arrays;

public class JavaDocUse {
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
    public static void show (String classPath,  int outMethodDoc) {
        ClassDoc[] classes = rootDoc.classes();
        for (ClassDoc classDoc : classes) {
            outDocInfo(classDoc, classPath, outMethodDoc);
        }

    }

    private static void outDocInfo (ClassDoc classDoc,String classPath,  int outMethodDoc) {
        System.out.println(classDoc.name() +
                "类的注释:" );
        System.out.println( classDoc.commentText());
        Tag[] authors = classDoc.tags("author");
        Arrays.asList(authors).forEach(tag -> System.out.println(tag.text()));
        if (outMethodDoc == 1){
            //输出方法的注释
            MethodDoc[] methodDocs = classDoc.methods();
            for (MethodDoc methodDoc : methodDocs) {
                // 打印出方法上的注释
                System.out.println("类"
                        + classDoc.name() + ","
                        + methodDoc.name() +
                        "方法注释:"
                );
                System.out.println(methodDoc.commentText());
                Tag[] methodParams = methodDoc.tags("param");
                Tag[] returns = methodDoc.tags("return");
                Arrays.asList(methodParams).forEach(methodParam -> {
                    ParamTag methodParam1 = (ParamTag) methodParam;
                    System.out.println(methodParam1.parameterName() + "   " + methodParam1.inlineTags()[0].text());
                    if (methodParam1.inlineTags().length > 1){
                        Tag linkTag = methodParam1.inlineTags()[1];
                        if (linkTag.name().equals("@link")){
                            //取link的对象的class
                            SeeTag linkTag1 = (SeeTag) linkTag;
                            showInnerJavaDoc(classPath, linkTag1);
                        }
                    }

                });
                Arrays.asList(returns).forEach(returnType -> {
                    System.out.println("返回值:");
                    if (returnType.inlineTags().length > 1){
                        Tag linkTag = returnType.inlineTags()[1];
                        if (linkTag.name().equals("@link")){
                            //取link的对象的class
                            SeeTag linkTag1 = (SeeTag) linkTag;
                            showInnerJavaDoc(classPath, linkTag1);
                        }
                    }
                });
            }
        }else {
            //输出私有属性的注释
            FieldDoc[] fieldDocs = classDoc.fields(false);
            System.out.println("属性参数 : ");
            for (FieldDoc fieldDoc : fieldDocs) {
                System.out.println(fieldDoc.name() + "   " + fieldDoc.type().typeName()  + "   " + fieldDoc.commentText());
                Tag[] seeTag = fieldDoc.tags("@see");
                if (seeTag.length > 0){
                    //取link的对象的class
                    SeeTag linkTag1 = (SeeTag) seeTag[0];
                    showInnerJavaDoc(classPath, linkTag1);
                }
            }

        }
    }

    private static void showInnerJavaDoc (String classPath, SeeTag linkTag1) {
        ClassDoc referencedClassDoc = linkTag1.referencedClass();
        String linkClassPath = referencedClassDoc.qualifiedName();
        com.sun.tools.javadoc.Main.execute(new String[] {"-doclet",
                Doclet.class.getName(),
                "-encoding","utf-8","-private","-classpath",
                classPath,
                classPath.replace("/target/classes/","") + "/src/main/java/" +  linkClassPath.replace(".","/") + ".java"});
        show(classPath,  0);
    }
}

