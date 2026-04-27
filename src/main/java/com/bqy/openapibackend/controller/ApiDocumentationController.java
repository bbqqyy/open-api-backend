package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.model.vo.ApiDocumentationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * API 文档接口 - 提供快速开始指南
 *
 * 帮助外部系统快速了解如何调用平台 API
 */
@Tag(name = "API文档", description = "API 快速开始指南和集成文档")
@RestController
@RequestMapping("/api/docs")
public class ApiDocumentationController {

    /**
     * 获取 API 快速开始指南
     */
    @Operation(summary = "快速开始指南", description = "获取外部系统集成的快速开始指南")
    @GetMapping("/quick-start")
    public ApiResponse<ApiDocumentationVO> getQuickStartGuide() {
        ApiDocumentationVO documentation = ApiDocumentationVO.builder()
                .title("API 快速开始指南")
                .description("本指南将帮助您快速了解如何调用平台 API")
                .version("1.0")
                .baseUrl("使用本平台的基础url")
                .authentication(createAuthenticationInfo())
                .steps(createQuickStartSteps())
                .codeExamples(createCodeExamples())
                .commonErrors(createCommonErrors())
                .supportContact("2807247572@qq.com")
                .build();

        return ApiResponse.success(documentation);
    }

    /**
     * 获取认证信息
     */
    @Operation(summary = "认证信息", description = "获取 API 密钥认证的详细信息")
    @GetMapping("/authentication")
    public ApiResponse<Map<String, Object>> getAuthenticationInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("method", "HMAC-SHA256 签名认证");
        info.put("type", "AccessKey + SecretKey");
        info.put("description", "所有 API 调用都需要在请求头中包含认证信息");
        info.put("requiredHeaders", new String[]{
            "X-Access-Key - 您的访问密钥",
            "X-Signature - HMAC-SHA256 签名",
            "X-Timestamp - 当前时间戳（毫秒）",
            "X-Nonce - 随机值（防重放）"
        });
        info.put("steps", new String[]{
            "1. 用户注册后自动获得 AccessKey 和 SecretKey",
            "2. 构建待签名字符串: Method\\nPath\\nTimestamp\\nNonce\\nBody",
            "3. 使用 HMAC-SHA256 生成签名: HMAC-SHA256(字符串, SecretKey)",
            "4. 将签名和其他信息放入请求头中"
        });
        return ApiResponse.success(info);
    }

    /**
     * 获取 API 端点列表
     */
    @Operation(summary = "API 端点列表", description = "获取所有可用的 API 端点信息")
    @GetMapping("/endpoints")
    public ApiResponse<Map<String, Object>> getEndpoints() {
        Map<String, Object> endpoints = new HashMap<>();

        // 用户相关接口
        Map<String, Object> userApi = new HashMap<>();
        userApi.put("POST /user/register", "用户注册（会返回 AccessKey 和 SecretKey）");
        userApi.put("GET /user/api-keys", "查看已有的 API 密钥");
        userApi.put("POST /user/regenerate-api-keys", "重新生成 API 密钥");
        endpoints.put("用户管理", userApi);

        // API 调用接口
        Map<String, Object> invokeApi = new HashMap<>();
        invokeApi.put("POST /invoke-with-key/{apiId}", "使用 API Key 认证调用 API（跨系统调用）");
        endpoints.put("API 调用", invokeApi);

        // API 信息接口
        Map<String, Object> infoApi = new HashMap<>();
        infoApi.put("GET /api/info/{apiId}", "获取单个 API 的详细信息");
        infoApi.put("GET /api/info/list", "获取 API 列表");
        endpoints.put("API 信息", infoApi);

        return ApiResponse.success(endpoints);
    }

    /**
     * 获取集成示例代码
     */
    @Operation(summary = "集成示例代码", description = "获取特定编程语言的完整代码示例，可直接复制使用")
    @GetMapping("/code-sample/{language}")
    public ApiResponse<Map<String, Object>> getCodeSample(
            @PathVariable
            @io.swagger.v3.oas.annotations.Parameter(description = "编程语言: java, python, nodejs", example = "java")
            String language) {
        Map<String, Object> sample = new HashMap<>();

        switch (language.toLowerCase()) {
            case "java":
                sample.put("language", "Java");
                sample.put("description", "使用 HttpURLConnection 调用 API 的完整示例");
                sample.put("imports", new String[]{
                    "import java.io.IOException;",
                    "import java.net.HttpURLConnection;",
                    "import java.net.URL;",
                    "import java.nio.charset.StandardCharsets;",
                    "import javax.crypto.Mac;",
                    "import javax.crypto.spec.SecretKeySpec;",
                    "import java.util.Base64;"
                });
                sample.put("code", "// 1. 配置信息\n" +
                    "String accessKey = \"your_access_key_here\";\n" +
                    "String secretKey = \"your_secret_key_here\";\n" +
                    "String apiHost = \"https://your-api-platform.com\";\n" +
                    "String apiId = \"123\";\n" +
                    "\n" +
                    "// 2. 准备请求\n" +
                    "String method = \"POST\";\n" +
                    "String path = \"/invoke-with-key/\" + apiId;\n" +
                    "long timestamp = System.currentTimeMillis();\n" +
                    "String nonce = \"nonce_\" + System.nanoTime();\n" +
                    "String body = \"{\\\"param1\\\":\\\"value1\\\"}\";\n" +
                    "\n" +
                    "// 3. 构建待签名字符串\n" +
                    "String payload = method + \"\\n\" + path + \"\\n\" + timestamp + \"\\n\" + nonce + \"\\n\" + body;\n" +
                    "\n" +
                    "// 4. 生成签名\n" +
                    "Mac mac = Mac.getInstance(\"HmacSHA256\");\n" +
                    "SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), \"HmacSHA256\");\n" +
                    "mac.init(keySpec);\n" +
                    "byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));\n" +
                    "String signature = Base64.getEncoder().encodeToString(signatureBytes);\n" +
                    "\n" +
                    "// 5. 发送请求\n" +
                    "URL url = new URL(apiHost + path);\n" +
                    "HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n" +
                    "conn.setRequestMethod(method);\n" +
                    "conn.setRequestProperty(\"X-Access-Key\", accessKey);\n" +
                    "conn.setRequestProperty(\"X-Signature\", signature);\n" +
                    "conn.setRequestProperty(\"X-Timestamp\", String.valueOf(timestamp));\n" +
                    "conn.setRequestProperty(\"X-Nonce\", nonce);\n" +
                    "conn.setRequestProperty(\"Content-Type\", \"application/json\");\n" +
                    "conn.setDoOutput(true);\n" +
                    "\n" +
                    "// 6. 发送请求体\n" +
                    "conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));\n" +
                    "\n" +
                    "// 7. 处理响应\n" +
                    "int responseCode = conn.getResponseCode();\n" +
                    "System.out.println(\"Response Code: \" + responseCode);");
                break;

            case "python":
                sample.put("language", "Python");
                sample.put("description", "使用 requests 库调用 API 的完整示例");
                sample.put("pip_install", "pip install requests");
                sample.put("code", "import requests\n" +
                    "import hmac\n" +
                    "import hashlib\n" +
                    "import time\n" +
                    "import base64\n" +
                    "import json\n" +
                    "\n" +
                    "# 1. 配置信息\n" +
                    "access_key = \"your_access_key_here\"\n" +
                    "secret_key = \"your_secret_key_here\"\n" +
                    "api_host = \"https://your-api-platform.com\"\n" +
                    "api_id = \"123\"\n" +
                    "\n" +
                    "# 2. 准备请求\n" +
                    "method = \"POST\"\n" +
                    "path = f\"/invoke-with-key/{api_id}\"\n" +
                    "timestamp = int(time.time() * 1000)\n" +
                    "nonce = f\"nonce_{int(time.time() * 1000000)}\"\n" +
                    "body = json.dumps({\"param1\": \"value1\"})\n" +
                    "\n" +
                    "# 3. 构建待签名字符串\n" +
                    "payload = f\"{method}\\n{path}\\n{timestamp}\\n{nonce}\\n{body}\"\n" +
                    "\n" +
                    "# 4. 生成签名\n" +
                    "signature = base64.b64encode(\n" +
                    "    hmac.new(secret_key.encode(), payload.encode(), hashlib.sha256).digest()\n" +
                    ").decode()\n" +
                    "\n" +
                    "# 5. 构建请求头\n" +
                    "headers = {\n" +
                    "    \"X-Access-Key\": access_key,\n" +
                    "    \"X-Signature\": signature,\n" +
                    "    \"X-Timestamp\": str(timestamp),\n" +
                    "    \"X-Nonce\": nonce,\n" +
                    "    \"Content-Type\": \"application/json\"\n" +
                    "}\n" +
                    "\n" +
                    "# 6. 发送请求\n" +
                    "url = f\"{api_host}{path}\"\n" +
                    "response = requests.post(url, headers=headers, data=body)\n" +
                    "\n" +
                    "# 7. 处理响应\n" +
                    "print(f\"Status Code: {response.status_code}\")\n" +
                    "print(f\"Response: {response.json()}\")");
                break;

            case "nodejs":
            case "node":
            case "js":
                sample.put("language", "Node.js");
                sample.put("description", "使用 axios 和 crypto 调用 API 的完整示例");
                sample.put("npm_install", "npm install axios");
                sample.put("code", "const crypto = require('crypto');\n" +
                    "const axios = require('axios');\n" +
                    "\n" +
                    "// 1. 配置信息\n" +
                    "const accessKey = 'your_access_key_here';\n" +
                    "const secretKey = 'your_secret_key_here';\n" +
                    "const apiHost = 'https://your-api-platform.com';\n" +
                    "const apiId = '123';\n" +
                    "\n" +
                    "// 2. 准备请求\n" +
                    "const method = 'POST';\n" +
                    "const path = `/invoke-with-key/${apiId}`;\n" +
                    "const timestamp = Date.now();\n" +
                    "const nonce = `nonce_${Date.now()}`;\n" +
                    "const body = JSON.stringify({ param1: 'value1' });\n" +
                    "\n" +
                    "// 3. 构建待签名字符串\n" +
                    "const payload = `${method}\\n${path}\\n${timestamp}\\n${nonce}\\n${body}`;\n" +
                    "\n" +
                    "// 4. 生成签名\n" +
                    "const signature = crypto\n" +
                    "    .createHmac('sha256', secretKey)\n" +
                    "    .update(payload)\n" +
                    "    .digest('base64');\n" +
                    "\n" +
                    "// 5. 构建请求头\n" +
                    "const headers = {\n" +
                    "    'X-Access-Key': accessKey,\n" +
                    "    'X-Signature': signature,\n" +
                    "    'X-Timestamp': timestamp.toString(),\n" +
                    "    'X-Nonce': nonce,\n" +
                    "    'Content-Type': 'application/json'\n" +
                    "};\n" +
                    "\n" +
                    "// 6. 发送请求\n" +
                    "const url = `${apiHost}${path}`;\n" +
                    "axios\n" +
                    "    .post(url, body, { headers })\n" +
                    "    .then(response => {\n" +
                    "        console.log('Status Code:', response.status);\n" +
                    "        console.log('Response:', response.data);\n" +
                    "    })\n" +
                    "    .catch(error => {\n" +
                    "        console.error('Error:', error.message);\n" +
                    "    });");
                break;

            case "curl":
                sample.put("language", "cURL");
                sample.put("description", "使用 cURL 命令调用 API 的示例");
                sample.put("code", "#!/bin/bash\n" +
                    "\n" +
                    "# 1. 配置信息\n" +
                    "ACCESS_KEY=\"your_access_key_here\"\n" +
                    "SECRET_KEY=\"your_secret_key_here\"\n" +
                    "API_HOST=\"https://your-api-platform.com\"\n" +
                    "API_ID=\"123\"\n" +
                    "\n" +
                    "# 2. 准备请求\n" +
                    "METHOD=\"POST\"\n" +
                    "PATH=\"/invoke-with-key/${API_ID}\"\n" +
                    "TIMESTAMP=$(date +%s)000\n" +
                    "NONCE=\"nonce_$(date +%s%N)\"\n" +
                    "BODY='{\"param1\":\"value1\"}'\n" +
                    "\n" +
                    "# 3. 构建待签名字符串\n" +
                    "PAYLOAD=\"${METHOD}\\n${PATH}\\n${TIMESTAMP}\\n${NONCE}\\n${BODY}\"\n" +
                    "\n" +
                    "# 4. 生成签名 (使用 echo -e)\n" +
                    "SIGNATURE=$(echo -e \"${PAYLOAD}\" | openssl dgst -sha256 -hmac \"${SECRET_KEY}\" -binary | base64)\n" +
                    "\n" +
                    "# 5. 发送请求\n" +
                    "curl -X ${METHOD} \"${API_HOST}${PATH}\" \\\n" +
                    "    -H \"X-Access-Key: ${ACCESS_KEY}\" \\\n" +
                    "    -H \"X-Signature: ${SIGNATURE}\" \\\n" +
                    "    -H \"X-Timestamp: ${TIMESTAMP}\" \\\n" +
                    "    -H \"X-Nonce: ${NONCE}\" \\\n" +
                    "    -H \"Content-Type: application/json\" \\\n" +
                    "    -d \"${BODY}\"");
                break;

            default:
                sample.put("error", "不支持的编程语言: " + language);
                sample.put("supported", new String[]{"java", "python", "nodejs", "curl"});
        }

        return ApiResponse.success(sample);
    }

    /**
     * 获取常见问题
     */
    @Operation(summary = "常见问题", description = "获取集成过程中的常见问题和解答")
    @GetMapping("/faqs")
    public ApiResponse<Map<String, String>> getFAQs() {
        Map<String, String> faqs = new HashMap<>();

        faqs.put(
            "Q: 如何获取 AccessKey 和 SecretKey？",
            "A: 在平台注册新账号后，系统会自动为您生成 AccessKey 和 SecretKey。" +
            "注册成功页面会显示完整的密钥信息，请妥善保存。" +
            "之后可以在用户中心的\"账号安全\"页面查看密钥。"
        );

        faqs.put(
            "Q: SecretKey 遗忘了怎么办？",
            "A: SecretKey 仅在注册时显示一次，遗忘后无法恢复。" +
            "您需要在用户中心重新生成密钥（旧密钥会立即失效）。" +
            "确保在所有应用中立即更新新的密钥。"
        );

        faqs.put(
            "Q: 签名生成错误（401 认证失败）怎么办？",
            "A: 检查以下几点：\n" +
            "1. 确认 AccessKey 和 SecretKey 正确\n" +
            "2. 确认待签名字符串格式正确（Method\\nPath\\nTimestamp\\nNonce\\nBody）\n" +
            "3. 确认使用了 HMAC-SHA256 算法\n" +
            "4. 确认时间戳在有效范围内（±5分钟）\n" +
            "5. 确认是 UTF-8 编码"
        );

        faqs.put(
            "Q: 如何安全地存储 SecretKey？",
            "A: 建议做法：\n" +
            "1. 使用环境变量存储密钥，不要硬编码在代码中\n" +
            "2. 使用密钥管理系统（如 AWS Secrets Manager）\n" +
            "3. 加密敏感配置文件\n" +
            "4. 定期轮换密钥\n" +
            "5. 不要在日志中打印完整的 SecretKey"
        );

        faqs.put(
            "Q: 请求超时或连接拒绝？",
            "A: 检查以下几点：\n" +
            "1. 确认 API 端点 URL 正确\n" +
            "2. 确认已连接到网络\n" +
            "3. 确认防火墙未阻止连接\n" +
            "4. 确认服务器未宕机（查看平台状态页面）\n" +
            "5. 检查请求头是否完整"
        );

        faqs.put(
            "Q: 429 Too Many Requests 错误？",
            "A: 这表示您的请求超出了速率限制。\n" +
            "某些 API 有调用次数限制（如分析功能每天 5 次）。\n" +
            "请等待一段时间后重试，或升级账号获得更高的限额。"
        );

        return ApiResponse.success(faqs);
    }

    // ==================== 私有方法 ====================

    private Map<String, Object> createAuthenticationInfo() {
        Map<String, Object> auth = new HashMap<>();
        auth.put("type", "AccessKey + SecretKey + HMAC-SHA256 签名");
        auth.put("description", "使用 HMAC-SHA256 算法对请求进行签名认证");
        auth.put("requiredHeaders", new String[]{
            "X-Access-Key",
            "X-Signature",
            "X-Timestamp",
            "X-Nonce"
        });
        auth.put("signatureAlgorithm", "HMAC-SHA256");
        auth.put("signatureEncoding", "Base64");
        return auth;
    }

    private Map<String, String>[] createQuickStartSteps() {
        Map<String, String>[] steps = new HashMap[5];

        steps[0] = new HashMap<>();
        steps[0].put("step", "1. 注册账号");
        steps[0].put("description", "在平台上创建新账号，系统会自动为您生成 AccessKey 和 SecretKey");
        steps[0].put("endpoint", "POST /user/register");

        steps[1] = new HashMap<>();
        steps[1].put("step", "2. 保存密钥");
        steps[1].put("description", "妥善保存您的 AccessKey 和 SecretKey，特别是 SecretKey 仅在注册时显示一次");
        steps[1].put("tips", "建议保存到密钥管理系统中，使用环境变量存储");

        steps[2] = new HashMap<>();
        steps[2].put("step", "3. 生成签名");
        steps[2].put("description", "对每个 API 请求生成 HMAC-SHA256 签名");
        steps[2].put("format", "HMAC-SHA256(Method\\nPath\\nTimestamp\\nNonce\\nBody, SecretKey)");

        steps[3] = new HashMap<>();
        steps[3].put("step", "4. 添加认证头");
        steps[3].put("description", "在请求头中添加认证信息");
        steps[3].put("headers", "X-Access-Key, X-Signature, X-Timestamp, X-Nonce");

        steps[4] = new HashMap<>();
        steps[4].put("step", "5. 发送请求");
        steps[4].put("description", "使用 HTTPS 发送请求");
        steps[4].put("example", "POST https://your-api-platform.com/invoke-with-key/{apiId}");

        return steps;
    }

    private Map<String, String>[] createCodeExamples() {
        Map<String, String>[] examples = new HashMap[3];

        examples[0] = new HashMap<>();
        examples[0].put("language", "Java");
        examples[0].put("file", "查看 ApiClientExample.java 中的 example1_NativeHttp() 和 example2_HttpClient()");

        examples[1] = new HashMap<>();
        examples[1].put("language", "Python");
        examples[1].put("description", "使用 requests 和 hmac 库");
        examples[1].put("tip", "查看 ApiClientExample.java 中的 Python 示例代码注释");

        examples[2] = new HashMap<>();
        examples[2].put("language", "Node.js");
        examples[2].put("description", "使用 axios 和 crypto 库");
        examples[2].put("tip", "查看 ApiClientExample.java 中的 Node.js 示例代码注释");

        return examples;
    }

    private Map<String, String>[] createCommonErrors() {
        Map<String, String>[] errors = new HashMap[5];

        errors[0] = new HashMap<>();
        errors[0].put("code", "400");
        errors[0].put("error", "Bad Request - 请求头不完整");
        errors[0].put("solution", "确认所有必需的请求头已包含：X-Access-Key, X-Signature, X-Timestamp, X-Nonce");

        errors[1] = new HashMap<>();
        errors[1].put("code", "401");
        errors[1].put("error", "Unauthorized - 认证失败");
        errors[1].put("solution", "检查 AccessKey 是否正确，确认签名算法无误，检查时间戳是否在有效范围内");

        errors[2] = new HashMap<>();
        errors[2].put("code", "403");
        errors[2].put("error", "Forbidden - 无权访问");
        errors[2].put("solution", "确认您有权访问该 API，检查用户权限设置");

        errors[3] = new HashMap<>();
        errors[3].put("code", "404");
        errors[3].put("error", "Not Found - API 不存在或已离线");
        errors[3].put("solution", "确认 API ID 正确，检查 API 是否已发布");

        errors[4] = new HashMap<>();
        errors[4].put("code", "429");
        errors[4].put("error", "Too Many Requests - 超出速率限制");
        errors[4].put("solution", "等待一段时间后重试，某些 API 有每日调用限制");

        return errors;
    }
}

