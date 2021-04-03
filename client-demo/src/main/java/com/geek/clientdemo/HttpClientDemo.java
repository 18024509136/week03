package com.geek.clientdemo;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClientDemo {

    public static void main(String[] args) {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet("http://localhost:8080/test");
        CloseableHttpResponse response = null;

        try {
            response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("服务器响应码为：" + statusCode);

            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                String msg = EntityUtils.toString(entity, "utf-8");
                String authHeader = response.getFirstHeader("auth").getValue();
                System.out.println("服务器响应auth头部内容为：" + authHeader);
                System.out.println("服务器响应内容为： " + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (client != null) {
                    client.close();
                }

                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
