package com.yzr.speechmatching.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yzr.speechmatching.model.yapi.Cat;
import com.yzr.speechmatching.model.yapi.JavaApiInfo;
import okhttp3.*;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 同步到Yapi工具类
 * @author yzr
 */
public class ImportYapiUtil {

    private static final String BASE_URL = "http://yapi.wb-intra.com/api";

    public static boolean importToYapi(JavaApiInfo javaApiInfo) throws IOException {
        int projectId = -1;
        int catId = -1;
        //获取项目信息
        String projectJsonStr = syncGetCall("/project/get", "token=" + javaApiInfo.getToken());
        JSONObject projectJsonObject = JSON.parseObject(projectJsonStr);
        if (projectJsonObject.containsKey("data")){
            JSONObject projectInfoJsonObject = JSON.parseObject(projectJsonObject.getString("data"));
            if (projectInfoJsonObject.containsKey("_id")){
                projectId = projectInfoJsonObject.getIntValue("_id");
            }
        }
        if (projectId > 0){

            //获取文件夹信息
            String catJsonStr = syncGetCall("/interface/getCatMenu", "token=" + javaApiInfo.getToken() + "&id=" + projectId);
            JSONObject catJsonObject = JSON.parseObject(catJsonStr);
            List<Cat> catList = new ArrayList<>();
            if (catJsonObject.containsKey("data")){
                catList = JSON.parseArray(catJsonObject.getString("data"), Cat.class);
            }
            if (!CollectionUtils.isEmpty( catList)){
                for (Cat cat : catList) {
                    if (javaApiInfo.getApplicationName().equals(cat.getName())){
                        catId = cat.get_id();
                        break;
                    }
                }
            }
        }
        if (catId < 0){
            //如果没有文件夹 及创建文件夹
            Cat cat = new Cat(projectId, javaApiInfo.getApplicationName(), javaApiInfo.getApplicationName());
            String addCatJsonStr = syncPostCall("/interface/add_cat?token=" + javaApiInfo.getToken(), RequestBody.create(JSON.toJSONString(cat), MediaType.parse("application/json; charset=utf-8")));
            JSONObject addCatJsonObject = JSON.parseObject(addCatJsonStr);
            if (addCatJsonObject.containsKey("data")){
                cat = JSON.parseObject(addCatJsonObject.getString("data"), Cat.class);
                if (cat != null){
                    catId = cat.get_id();
                }
            }
        }else {
            //之前存在文件夹 可能也存在接口文档
            //尝试读取接口文档 获取之前写过的备注 将作者信息拼接进去
            String listCatJsonStr = syncGetCall("/interface/list_cat", "token=" + javaApiInfo.getToken() + "&page=1&limit=100&catid=" + catId );
            JSONObject listCatJsonObject = JSON.parseObject(listCatJsonStr);
            if (listCatJsonObject.containsKey("data")){
                List<JavaApiInfo> javaApiInfos = JSON.parseArray(listCatJsonObject.getJSONObject("data").getString("list"), JavaApiInfo.class);
                if (!CollectionUtils.isEmpty(javaApiInfos)){
                    javaApiInfos.stream().filter(apiInfo -> apiInfo.getPath().equals(javaApiInfo.getPath()) ).findFirst().ifPresent(apiInfo -> {
                        try {
                            String docJsonStr = syncGetCall("/interface/get", "token=" + javaApiInfo.getToken() + "&id=" + apiInfo.get_id() );
                            JavaApiInfo oldJavaApiInfo = JSON.parseObject(JSON.parseObject(docJsonStr).getString("data"), JavaApiInfo.class);
                            String oldDesc = oldJavaApiInfo.getDesc();
                            String[] strings = oldDesc.split("<p>");
                            String oldAuthorDesc = strings[strings.length-1];
                            if (oldAuthorDesc.contains("作者")){
                                javaApiInfo.setDesc(oldDesc.replace("<p>" + strings[strings.length-1], javaApiInfo.getDesc()));
                            }else {
                                javaApiInfo.setDesc(oldDesc + javaApiInfo.getDesc());
                            }
                        } catch (IOException e) {
                            System.out.println("getDoc is error patch is  " +  apiInfo.getPath());
                        }
                    });
                }
            }
        }
        if (catId > 0) {
            //导入数据
            javaApiInfo.setCatid(String.valueOf(catId));
            String resultStr = syncPostCall("/interface/save", RequestBody.create(JSON.toJSONString(javaApiInfo), MediaType.parse("application/json; charset=utf-8")));
            JSONObject resultJsonObject = JSON.parseObject(resultStr);
            return 0 == resultJsonObject.getIntValue("errcode");
        }
        return false;
    }


    public static String syncGetCall (String url, String paramStr) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        final Request request = new Request.Builder()
                .url(BASE_URL + url + "?" + paramStr)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        return Objects.requireNonNull(response.body()).string();
    }

    public static String syncPostCall (String url, RequestBody requestBody) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        final Request request = new Request.Builder()
                .url(BASE_URL + url )
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        return Objects.requireNonNull(response.body()).string();
    }

}
