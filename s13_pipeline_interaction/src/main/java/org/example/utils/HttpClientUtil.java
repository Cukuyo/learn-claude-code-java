package org.example.utils;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * HttpClient封装工具类
 */
public class HttpClientUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    /**
     * llm请求
     *
     * @param url    model url
     * @param apiKey apiKey
     * @param body   请求体
     * @return 请求响应
     * @throws IOException          io异常
     * @throws InterruptedException 线程等待中断
     */
    public static JSONObject send(String url, String apiKey, JSONObject body) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        LOGGER.debug("请求url: {}, 状态码: {}, 请求耗时: {} ms", url, response.statusCode(), (System.currentTimeMillis() - start));

        return JSONObject.parseObject(response.body());
    }
}
