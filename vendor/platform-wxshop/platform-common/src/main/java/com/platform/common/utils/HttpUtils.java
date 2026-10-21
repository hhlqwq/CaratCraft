package com.platform.common.utils;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * @author 何海龙
 */
public class HttpUtils {
  /**
   * 向指定 URL 发送POST方法的请求
   *
   * @param url 发送请求的 URL
   * @param params 请求的参数集合
   * @return 远程资源的响应结果
   * @throws Exception Exception
   */
  public static String sendPost(String url, String params) throws Exception {
    HttpPost httpPost = new HttpPost(url);
    httpPost.setEntity(new StringEntity(params, StandardCharsets.UTF_8));
    try (CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(httpPost)) {
      HttpEntity entity = response.getEntity();
      return entity == null ? "" : EntityUtils.toString(entity, StandardCharsets.UTF_8);
    }
  }

  /**
   * 获取网络资源到InputStream
   *
   * @param imgUrl imgUrl
   * @return InputStream
   * @throws Exception Exception
   */
  public static InputStream getNetResource(String imgUrl) throws Exception {
    URLConnection conn = URI.create(imgUrl).toURL().openConnection();
    return conn.getInputStream();
  }
}
