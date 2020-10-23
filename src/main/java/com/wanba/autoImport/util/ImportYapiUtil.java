package com.wanba.autoImport.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wanba.autoImport.model.yapi.Cat;
import com.wanba.autoImport.model.yapi.JavaApiInfo;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步到Yapi工具类
 *
 * @author yzr
 */
public class ImportYapiUtil {

    private static final String BASE_URL = "http://yapi.wb-intra.com/api";

    public static boolean importToYapi(JavaApiInfo javaApiInfo) throws Exception {
        int projectId = -1;
        int catId = -1;
        //获取项目信息
        String projectJsonStr = syncGetCall("/project/get",new HashMap<String, String>(1){{put("token",javaApiInfo.getToken());}});
        JSONObject projectJsonObject = JSON.parseObject(projectJsonStr);
        if (projectJsonObject.containsKey("data")) {
            JSONObject projectInfoJsonObject = JSON.parseObject(projectJsonObject.getString("data"));
            if (projectInfoJsonObject.containsKey("_id")) {
                projectId = projectInfoJsonObject.getIntValue("_id");
            }
        }
        if (projectId > 0) {

            //获取文件夹信息
            int finalProjectId = projectId;
            String catJsonStr = syncGetCall("/interface/getCatMenu",new HashMap<String, String>(2){{put("token",javaApiInfo.getToken()); put("id",String.valueOf(finalProjectId));}});
            JSONObject catJsonObject = JSON.parseObject(catJsonStr);
            List<Cat> catList = new ArrayList<>();
            if (catJsonObject.containsKey("data")) {
                catList = JSON.parseArray(catJsonObject.getString("data"), Cat.class);
            }
            if (!CollectionUtils.isEmpty(catList)) {
                for (Cat cat : catList) {
                    if (javaApiInfo.getApplicationName().equals(cat.getName())) {
                        catId = cat.get_id();
                        break;
                    }
                }
            }
        }
        if (catId < 0) {
            //如果没有文件夹 及创建文件夹
            Cat cat = new Cat(projectId, javaApiInfo.getApplicationName(), javaApiInfo.getApplicationName(), javaApiInfo.getToken());
            String addCatJsonStr = syncPostCall("/interface/add_cat",JSON.toJSONString(cat));
            JSONObject addCatJsonObject = JSON.parseObject(addCatJsonStr);
            if (addCatJsonObject.containsKey("data")) {
                cat = JSON.parseObject(addCatJsonObject.getString("data"), Cat.class);
                if (cat != null) {
                    catId = cat.get_id();
                }
            }
        } else {
            //之前存在文件夹 可能也存在接口文档
            //尝试读取接口文档 获取之前写过的备注 将作者信息拼接进去
            int finalCatId = catId;
            String listCatJsonStr = syncGetCall("/interface/list_cat", new HashMap<String, String>(4){{put("token",javaApiInfo.getToken()); put("page","1"); put("limit", "100"); put("catid",String.valueOf(finalCatId));}});
            JSONObject listCatJsonObject = JSON.parseObject(listCatJsonStr);
            if (listCatJsonObject.containsKey("data")) {
                List<JavaApiInfo> javaApiInfos = JSON.parseArray(listCatJsonObject.getJSONObject("data").getString("list"), JavaApiInfo.class);
                if (!CollectionUtils.isEmpty(javaApiInfos)) {
                    javaApiInfos.stream().filter(apiInfo -> apiInfo.getPath().equals(javaApiInfo.getPath())).findFirst().ifPresent(apiInfo -> {
                        try {
                            String docJsonStr = syncGetCall("/interface/get",new HashMap<String, String>(2){{put("token",javaApiInfo.getToken()); put("id",String.valueOf(apiInfo.get_id()));}} );
                            JavaApiInfo oldJavaApiInfo = JSON.parseObject(JSON.parseObject(docJsonStr).getString("data"), JavaApiInfo.class);
                            String oldDesc = oldJavaApiInfo.getDesc();
                            String[] strings = oldDesc.split("<p>");
                            String oldAuthorDesc = strings[strings.length - 1];
                            if (oldAuthorDesc.contains("作者")) {
                                javaApiInfo.setDesc(oldDesc.replace("<p>" + strings[strings.length - 1], javaApiInfo.getDesc()));
                            } else {
                                javaApiInfo.setDesc(oldDesc + javaApiInfo.getDesc());
                            }
                        } catch (Exception e) {
                            System.out.println("getDoc is error patch is  " + apiInfo.getPath());
                        }
                    });
                }
            }
        }
        if (catId > 0) {
            //导入数据
            javaApiInfo.setCatid(String.valueOf(catId));
            String resultStr = syncPostCall( "/interface/save",JSON.toJSONString(javaApiInfo));
            JSONObject resultJsonObject = JSON.parseObject(resultStr);
            return 0 == resultJsonObject.getIntValue("errcode");
        }
        return false;
    }


    public static String syncGetCall(String url, Map<String, String> paramMap) throws Exception {
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        URIBuilder uriBuilder = new URIBuilder(BASE_URL + url);
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            uriBuilder.setParameter(entry.getKey(), entry.getValue());
        }
        URI uri = uriBuilder.build();
        HttpGet httpGet = new HttpGet(uri);
        //response 对象
        CloseableHttpResponse response = null;
        try {
            // 执行http get请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
            return null;
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }

    }

    public static String syncPostCall(String url ,String requestBodyJson) throws Exception {
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(BASE_URL + url);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8"); //添加请求头
        // 组织数据
        StringEntity se = new StringEntity(requestBodyJson, "utf-8");

        //对于POST请求,把请求体填充进HttpPost实体.
        httpPost.setEntity(se);
        try {
            CloseableHttpResponse response = httpclient.execute(httpPost);
            //通过HttpResponse接口的getEntity方法返回响应信息，并进行相应的处理
            return EntityUtils.toString(response.getEntity());
        } finally {
            //最后关闭HttpClient资源
            httpclient.close();
        }

    }

}
