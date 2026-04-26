package com.bqy.openapibackend.util;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class DynamicApiUtil {

    private final RestTemplate restTemplate;

    public DynamicApiUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 通用请求方法
     *
     * @param url        请求地址
     * @param method     请求方法 (GET, POST, PUT, DELETE 等)
     * @param params     请求参数 (Map 结构)
     * @param headers    请求头 (Map 结构)
     * @param body       请求体对象 (POST/PUT 时使用，可以是 Map 或 Java Bean)
     * @param returnType 返回类型 (String.class, Map.class, 或具体的 DTO.class)
     * @return 返回对象
     */
    public Object request(String url, HttpMethod method, Map<String, String> params,
                          Map<String, String> headers, Object body, Class returnType) {

        // 1. 构造 URL (处理 GET 参数)
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }
        String finalUrl = builder.toUriString();

        // 2. 构造 Headers
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }
        // 默认设置 Content-Type 为 JSON，如果是表单提交调用方需覆盖此设置
        if (httpHeaders.getContentType() == null && body != null) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        // 3. 构造 Entity (封装 Header 和 Body)
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, httpHeaders);

        // 4. 发送请求并返回
        // exchange 方法支持所有 HTTP 方法，并且可以精确控制返回类型
        ResponseEntity responseEntity = restTemplate.exchange(
                finalUrl,
                method,
                requestEntity,
                returnType
        );

        return responseEntity.getBody();
    }

    /**
     * 快速发送 POST JSON 请求
     */
    public Object postJson(String url, Object body) {
        return request(url, HttpMethod.POST, null, null, body, Map.class);
    }

    /**
     * 快速发送 GET 请求
     */
    public Object get(String url, Map<String, String> params) {
        return request(url, HttpMethod.GET, params, null, null, Map.class);
    }
}
