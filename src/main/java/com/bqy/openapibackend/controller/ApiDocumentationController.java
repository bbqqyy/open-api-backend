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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API 文档接口 - 提供快速开始指南
 *
 * 帮助外部系统快速了解如何通过 api-client-sdk 或直接 HTTP 调用平台 API
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
                .title("Open API 平台快速开始指南")
                .description("本指南帮助您快速了解如何调用平台 API。" +
                        "提供两种方式：1) 引入 api-client-sdk（推荐）；2) 直接构造 HTTP 请求（HMAC-SHA256 签名）。")
                .version("1.0")
                .baseUrl("http://localhost:8080")
                .authentication(createAuthenticationInfo())
                .steps(createQuickStartSteps())
                .codeExamples(createCodeExamples())
                .commonErrors(createCommonErrors())
                .supportContact("2807247572@qq.com")
                .build();

        return ApiResponse.success(documentation);
    }

    /**
     * 获取认证信息详情
     */
    @Operation(summary = "认证信息", description = "获取 HMAC-SHA256 签名认证的详细说明")
    @GetMapping("/authentication")
    public ApiResponse<Map<String, Object>> getAuthenticationInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("method", "HMAC-SHA256 签名认证");
        info.put("type", "AccessKey + SecretKey");
        info.put("description", "通过 /apiInfo/invoke-with-key/{apiId} 调用 API 时，需在请求头中携带签名信息");
        info.put("requiredHeaders", new String[]{
            "X-Access-Key  —— 您的访问密钥（注册后在用户中心获取）",
            "X-Signature   —— HMAC-SHA256 签名（Base64 标准编码）",
            "X-Timestamp   —— 当前时间戳（毫秒），有效窗口 ±5 分钟",
            "X-Nonce       —— 随机字符串（防重放，建议使用 UUID）"
        });
        info.put("signaturePayloadFormat", "METHOD\\nPATH\\nTIMESTAMP\\nNONCE\\nBODY");
        info.put("signaturePayloadNote", "GET 请求 BODY 为空字符串；PATH 不含 QueryString；BODY 为原始 JSON 字符串");
        info.put("signaturePayloadExample",
                "POST\\n/apiInfo/invoke-with-key/1\\n1714356000000\\nabc-uuid\\n{\"min\":1,\"max\":100}");
        info.put("algorithm", "HmacSHA256（javax.crypto.Mac）");
        info.put("encoding", "Base64 标准编码（Base64.getEncoder()）");
        info.put("steps", new String[]{
            "1. 注册账号后，在用户中心获取 AccessKey 和 SecretKey",
            "2. 构建待签名字符串：METHOD + \"\\n\" + PATH + \"\\n\" + TIMESTAMP + \"\\n\" + NONCE + \"\\n\" + BODY",
            "3. 签名：Mac mac = Mac.getInstance(\"HmacSHA256\"); mac.init(new SecretKeySpec(secretKey.getBytes(UTF-8), \"HmacSHA256\"));",
            "4. 编码：String signature = Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(UTF-8)));",
            "5. 将四个头部放入 HTTP 请求：X-Access-Key / X-Signature / X-Timestamp / X-Nonce"
        });
        return ApiResponse.success(info);
    }

    /**
     * 获取 SDK 集成说明
     */
    @Operation(summary = "SDK 集成说明", description = "介绍如何引入 api-client-sdk 进行快速集成")
    @GetMapping("/sdk")
    public ApiResponse<Map<String, Object>> getSdkInfo() {
        Map<String, Object> sdk = new LinkedHashMap<>();
        sdk.put("name", "api-client-sdk");
        sdk.put("description", "Open API 平台官方 Java SDK，封装了签名认证、HTTP 调用、响应解析等全部细节，开箱即用");
        sdk.put("mavenDependency",
                "<dependency>\n" +
                "    <groupId>com.bqy</groupId>\n" +
                "    <artifactId>api-client-sdk</artifactId>\n" +
                "    <version>1.0.0-SNAPSHOT</version>\n" +
                "</dependency>");
        sdk.put("springBootConfig",
                "# application.properties\n" +
                "open-api.client.base-url=http://localhost:8080\n" +
                "open-api.client.access-key=ak_your_access_key\n" +
                "open-api.client.secret-key=sk_your_secret_key\n" +
                "# 可选配置\n" +
                "open-api.client.connect-timeout=5000\n" +
                "open-api.client.read-timeout=30000");
        sdk.put("springBootUsage",
                "@Resource\n" +
                "private OpenApiClient openApiClient;\n\n" +
                "// GET 请求（参数作为 QueryString）\n" +
                "Object result = openApiClient.invokeGet(apiId, Map.of(\"min\", 1, \"max\", 100));\n\n" +
                "// POST 请求（参数作为 JSON Body）\n" +
                "Object result = openApiClient.invokePost(apiId, Map.of(\"text\", \"Hello\"));\n\n" +
                "// 内置便捷方法（无需指定 apiId）\n" +
                "openApiClient.randomNumber(1, 100);      // apiId=1 随机整数\n" +
                "openApiClient.reverseText(\"abc\");         // apiId=2 文本翻转\n" +
                "openApiClient.ipInfo(\"8.8.8.8\");          // apiId=3 IP信息查询\n" +
                "openApiClient.mathAdd(3.14, 2.86);       // apiId=4 加法计算\n" +
                "openApiClient.hello(\"张三\", \"zh\");       // apiId=5 个性化问候");
        sdk.put("nonSpringUsage",
                "// 非 Spring Boot 项目手动创建客户端\n" +
                "OpenApiClientConfig config = new OpenApiClientConfig();\n" +
                "config.setBaseUrl(\"http://localhost:8080\");\n" +
                "config.setAccessKey(\"ak_your_access_key\");\n" +
                "config.setSecretKey(\"sk_your_secret_key\");\n" +
                "OpenApiClient client = new OpenApiClient(config);\n" +
                "Object result = client.invokeGet(1L, Map.of(\"min\", 1, \"max\", 100));");
        sdk.put("sessionAuthUsage",
                "// Session 认证方式（需先登录）\n" +
                "String sessionId = client.login(\"your_account\", \"your_password\");\n" +
                "// 登录成功后 sessionId 会自动保存到 config 中\n" +
                "Object result = client.invokeWithSession(apiId, params);");
        sdk.put("authModes", new String[]{
            "API Key 认证（推荐）：配置 accessKey + secretKey，调用 invokeGet / invokePost / invoke",
            "Session 认证：先调用 client.login(account, password)，再调用 invokeWithSession"
        });
        return ApiResponse.success(sdk);
    }

    /**
     * 获取 API 端点列表
     */
    @Operation(summary = "API 端点列表", description = "获取平台主要 API 端点信息")
    @GetMapping("/endpoints")
    public ApiResponse<Map<String, Object>> getEndpoints() {
        Map<String, Object> endpoints = new LinkedHashMap<>();

        Map<String, Object> userApi = new LinkedHashMap<>();
        userApi.put("POST /user/register", "用户注册（返回 AccessKey 和 SecretKey）");
        userApi.put("POST /user/login", "用户登录（返回 Session Cookie）");
        userApi.put("GET /user/api-keys", "查看当前用户的 AccessKey（需登录）");
        userApi.put("POST /user/regenerate-api-keys", "重新生成 API 密钥（旧密钥立即失效）");
        endpoints.put("用户管理", userApi);

        Map<String, Object> invokeApi = new LinkedHashMap<>();
        invokeApi.put("POST /apiInfo/invoke-with-key/{apiId}", "API Key 认证调用（需携带 X-Access-Key / X-Signature / X-Timestamp / X-Nonce 请求头）");
        invokeApi.put("POST /apiInfo/invoke/{apiId}", "Session 认证调用（需携带登录后的 Cookie）");
        endpoints.put("API 调用", invokeApi);

        Map<String, Object> infoApi = new LinkedHashMap<>();
        infoApi.put("GET /apiInfo/page", "分页获取 API 列表（公开）");
        infoApi.put("GET /apiInfo/detail/{apiId}", "获取单个 API 的详细信息");
        infoApi.put("GET /apiInfo/page/my", "获取我的 API 列表（需登录）");
        infoApi.put("POST /apiInfo/apply/{apiId}", "申请某个 API 的调用权限");
        endpoints.put("API 信息", infoApi);

        return ApiResponse.success(endpoints);
    }

    /**
     * 获取集成示例代码
     */
    @Operation(summary = "集成示例代码", description = "获取特定编程语言的完整代码示例")
    @GetMapping("/code-sample/{language}")
    public ApiResponse<Map<String, Object>> getCodeSample(
            @PathVariable
            @io.swagger.v3.oas.annotations.Parameter(description = "编程语言: java-sdk, java, python, nodejs, curl", example = "java-sdk")
            String language) {
        Map<String, Object> sample = new LinkedHashMap<>();

        switch (language.toLowerCase()) {
            case "java-sdk":
                sample.put("language", "Java（使用 api-client-sdk，推荐）");
                sample.put("description", "引入官方 SDK，无需手动处理签名，直接调用接口");
                sample.put("mavenDependency",
                        "<dependency>\n" +
                        "    <groupId>com.bqy</groupId>\n" +
                        "    <artifactId>api-client-sdk</artifactId>\n" +
                        "    <version>1.0.0-SNAPSHOT</version>\n" +
                        "</dependency>");
                sample.put("code",
                        "// application.properties 中配置：\n" +
                        "// open-api.client.base-url=http://localhost:8080\n" +
                        "// open-api.client.access-key=ak_your_access_key\n" +
                        "// open-api.client.secret-key=sk_your_secret_key\n\n" +
                        "// Spring Boot 中注入使用\n" +
                        "@Resource\n" +
                        "private OpenApiClient openApiClient;\n\n" +
                        "// 示例1：调用随机数接口 (apiId=1, GET)\n" +
                        "Map<String, Object> r1 = openApiClient.randomNumber(1, 100);\n" +
                        "System.out.println(\"随机数: \" + r1.get(\"result\"));\n\n" +
                        "// 示例2：调用文本翻转接口 (apiId=2, POST)\n" +
                        "Map<String, Object> r2 = openApiClient.reverseText(\"Hello, Open API!\");\n" +
                        "System.out.println(\"翻转结果: \" + r2.get(\"reversed\"));\n\n" +
                        "// 示例3：通用调用任意已注册接口\n" +
                        "Object result = openApiClient.invokeGet(3L, Map.of(\"ip\", \"8.8.8.8\"));\n" +
                        "System.out.println(\"IP 信息: \" + result);");
                break;

            case "java":
                sample.put("language", "Java（原生 HTTP，手动签名）");
                sample.put("description", "不使用 SDK，直接构造 HMAC-SHA256 签名并发起 HTTP 请求");
                sample.put("imports", new String[]{
                    "import java.net.HttpURLConnection;",
                    "import java.net.URL;",
                    "import java.nio.charset.StandardCharsets;",
                    "import javax.crypto.Mac;",
                    "import javax.crypto.spec.SecretKeySpec;",
                    "import java.util.Base64;"
                });
                sample.put("code",
                        "String accessKey = \"ak_your_access_key\";\n" +
                        "String secretKey = \"sk_your_secret_key\";\n" +
                        "String baseUrl   = \"http://localhost:8080\";\n" +
                        "Long   apiId     = 1L;\n\n" +
                        "// 签名参数\n" +
                        "String method    = \"POST\";\n" +
                        "String path      = \"/apiInfo/invoke-with-key/\" + apiId;\n" +
                        "long   timestamp = System.currentTimeMillis();\n" +
                        "String nonce     = java.util.UUID.randomUUID().toString().replace(\"-\", \"\");\n" +
                        "String body      = \"{\\\"min\\\":1,\\\"max\\\":100}\";\n\n" +
                        "// 构建待签名字符串\n" +
                        "String payload = method + \"\\n\" + path + \"\\n\" + timestamp + \"\\n\" + nonce + \"\\n\" + body;\n\n" +
                        "// HMAC-SHA256 签名\n" +
                        "Mac mac = Mac.getInstance(\"HmacSHA256\");\n" +
                        "mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), \"HmacSHA256\"));\n" +
                        "String signature = Base64.getEncoder().encodeToString(\n" +
                        "        mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));\n\n" +
                        "// 发送请求\n" +
                        "HttpURLConnection conn = (HttpURLConnection) new URL(baseUrl + path).openConnection();\n" +
                        "conn.setRequestMethod(method);\n" +
                        "conn.setRequestProperty(\"X-Access-Key\",  accessKey);\n" +
                        "conn.setRequestProperty(\"X-Signature\",   signature);\n" +
                        "conn.setRequestProperty(\"X-Timestamp\",   String.valueOf(timestamp));\n" +
                        "conn.setRequestProperty(\"X-Nonce\",       nonce);\n" +
                        "conn.setRequestProperty(\"Content-Type\",  \"application/json\");\n" +
                        "conn.setDoOutput(true);\n" +
                        "conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));\n" +
                        "System.out.println(\"HTTP \" + conn.getResponseCode());");
                break;

            case "python":
                sample.put("language", "Python");
                sample.put("description", "使用 requests 和 hmac 库，手动构造 HMAC-SHA256 签名");
                sample.put("pipInstall", "pip install requests");
                sample.put("code",
                        "import requests, hmac, hashlib, base64, time, uuid, json\n\n" +
                        "access_key = 'ak_your_access_key'\n" +
                        "secret_key = 'sk_your_secret_key'\n" +
                        "base_url   = 'http://localhost:8080'\n" +
                        "api_id     = 1\n\n" +
                        "method    = 'POST'\n" +
                        "path      = f'/apiInfo/invoke-with-key/{api_id}'\n" +
                        "timestamp = int(time.time() * 1000)\n" +
                        "nonce     = str(uuid.uuid4()).replace('-', '')\n" +
                        "body      = json.dumps({'min': 1, 'max': 100})\n\n" +
                        "# 构建待签名字符串\n" +
                        "payload   = f'{method}\\n{path}\\n{timestamp}\\n{nonce}\\n{body}'\n\n" +
                        "# HMAC-SHA256 签名\n" +
                        "signature = base64.b64encode(\n" +
                        "    hmac.new(secret_key.encode('utf-8'), payload.encode('utf-8'), hashlib.sha256).digest()\n" +
                        ").decode('utf-8')\n\n" +
                        "headers = {\n" +
                        "    'X-Access-Key': access_key,\n" +
                        "    'X-Signature':  signature,\n" +
                        "    'X-Timestamp':  str(timestamp),\n" +
                        "    'X-Nonce':      nonce,\n" +
                        "    'Content-Type': 'application/json'\n" +
                        "}\n\n" +
                        "resp = requests.post(base_url + path, headers=headers, data=body)\n" +
                        "print(f'Status: {resp.status_code}')\n" +
                        "print(f'Result: {resp.json()}')");
                break;

            case "nodejs":
            case "node":
            case "js":
                sample.put("language", "Node.js");
                sample.put("description", "使用 axios 和内置 crypto 模块，手动构造 HMAC-SHA256 签名");
                sample.put("npmInstall", "npm install axios");
                sample.put("code",
                        "const crypto = require('crypto');\n" +
                        "const axios  = require('axios');\n\n" +
                        "const accessKey = 'ak_your_access_key';\n" +
                        "const secretKey = 'sk_your_secret_key';\n" +
                        "const baseUrl   = 'http://localhost:8080';\n" +
                        "const apiId     = 1;\n\n" +
                        "const method    = 'POST';\n" +
                        "const path      = `/apiInfo/invoke-with-key/${apiId}`;\n" +
                        "const timestamp = Date.now();\n" +
                        "const nonce     = crypto.randomUUID().replace(/-/g, '');\n" +
                        "const body      = JSON.stringify({ min: 1, max: 100 });\n\n" +
                        "// 构建待签名字符串\n" +
                        "const payload   = `${method}\\n${path}\\n${timestamp}\\n${nonce}\\n${body}`;\n\n" +
                        "// HMAC-SHA256 签名\n" +
                        "const signature = crypto\n" +
                        "    .createHmac('sha256', secretKey)\n" +
                        "    .update(payload)\n" +
                        "    .digest('base64');\n\n" +
                        "axios.post(baseUrl + path, body, {\n" +
                        "    headers: {\n" +
                        "        'X-Access-Key':  accessKey,\n" +
                        "        'X-Signature':   signature,\n" +
                        "        'X-Timestamp':   String(timestamp),\n" +
                        "        'X-Nonce':       nonce,\n" +
                        "        'Content-Type':  'application/json'\n" +
                        "    }\n" +
                        "}).then(r => console.log('Result:', r.data))\n" +
                        "  .catch(e => console.error('Error:', e.message));");
                break;

            case "curl":
                sample.put("language", "cURL");
                sample.put("description", "使用 Shell 脚本配合 openssl 生成签名并调用 API");
                sample.put("code",
                        "#!/bin/bash\n\n" +
                        "ACCESS_KEY=\"ak_your_access_key\"\n" +
                        "SECRET_KEY=\"sk_your_secret_key\"\n" +
                        "BASE_URL=\"http://localhost:8080\"\n" +
                        "API_ID=\"1\"\n\n" +
                        "METHOD=\"POST\"\n" +
                        "PATH=\"/apiInfo/invoke-with-key/${API_ID}\"\n" +
                        "TIMESTAMP=$(date +%s)000\n" +
                        "NONCE=$(cat /proc/sys/kernel/random/uuid 2>/dev/null || uuidgen | tr -d '-')\n" +
                        "BODY='{\"min\":1,\"max\":100}'\n\n" +
                        "# 构建待签名字符串（注意使用 printf 保留 \\n）\n" +
                        "PAYLOAD=$(printf '%s\\n%s\\n%s\\n%s\\n%s' \"$METHOD\" \"$PATH\" \"$TIMESTAMP\" \"$NONCE\" \"$BODY\")\n\n" +
                        "# HMAC-SHA256 签名\n" +
                        "SIGNATURE=$(printf '%s' \"$PAYLOAD\" | openssl dgst -sha256 -hmac \"$SECRET_KEY\" -binary | base64)\n\n" +
                        "# 发送请求\n" +
                        "curl -s -X ${METHOD} \"${BASE_URL}${PATH}\" \\\n" +
                        "    -H \"X-Access-Key: ${ACCESS_KEY}\" \\\n" +
                        "    -H \"X-Signature: ${SIGNATURE}\" \\\n" +
                        "    -H \"X-Timestamp: ${TIMESTAMP}\" \\\n" +
                        "    -H \"X-Nonce: ${NONCE}\" \\\n" +
                        "    -H \"Content-Type: application/json\" \\\n" +
                        "    -d \"${BODY}\" | python3 -m json.tool");
                break;

            default:
                sample.put("error", "不支持的编程语言: " + language);
                sample.put("supported", new String[]{"java-sdk", "java", "python", "nodejs", "curl"});
        }

        return ApiResponse.success(sample);
    }

    /**
     * 获取常见问题
     */
    @Operation(summary = "常见问题", description = "获取集成过程中的常见问题和解答")
    @GetMapping("/faqs")
    public ApiResponse<Map<String, String>> getFAQs() {
        Map<String, String> faqs = new LinkedHashMap<>();

        faqs.put(
            "Q: 如何获取 AccessKey 和 SecretKey？",
            "A: 在平台注册新账号后，系统自动生成 AccessKey 和 SecretKey。" +
            "可在用户中心的「账号安全」页面查看 AccessKey；" +
            "SecretKey 仅注册时展示一次，请立即保存。" +
            "遗忘后可通过「重新生成密钥」接口（POST /user/regenerate-api-keys）重置，旧密钥即刻失效。"
        );

        faqs.put(
            "Q: 使用 api-client-sdk 时如何配置？",
            "A: 在 application.properties 中添加以下配置即可自动注入 OpenApiClient：\n" +
            "  open-api.client.base-url=http://localhost:8080\n" +
            "  open-api.client.access-key=ak_your_access_key\n" +
            "  open-api.client.secret-key=sk_your_secret_key\n" +
            "然后在需要的地方 @Resource private OpenApiClient openApiClient; 即可直接使用。"
        );

        faqs.put(
            "Q: 401 Unauthorized - 签名验证失败怎么办？",
            "A: 按以下顺序排查：\n" +
            "1. 确认 AccessKey 和 SecretKey 与平台一致\n" +
            "2. 确认待签名字符串格式：METHOD\\nPATH\\nTIMESTAMP\\nNONCE\\nBODY（使用 \\n 而非实际换行）\n" +
            "3. 确认 PATH 不含 QueryString（GET 参数不参与签名）\n" +
            "4. 确认时间戳为毫秒级，且与服务器时间差在 ±5 分钟内\n" +
            "5. 确认使用 HmacSHA256 算法，签名结果为 Base64 标准编码（非 URL 编码）\n" +
            "6. 确认 BODY 参与签名的字符串与实际发送的请求体完全一致"
        );

        faqs.put(
            "Q: 403 Forbidden - 无权调用该接口？",
            "A: 可能原因：\n" +
            "1. 您没有申请过该 API 的调用权限（需先调用 POST /apiInfo/apply/{apiId}）\n" +
            "2. 申请状态为 pending（等待 API 拥有者审批）或 rejected（已被拒绝）\n" +
            "3. 如果您是 API 的拥有者，可以直接调用自己的接口，无需申请权限"
        );

        faqs.put(
            "Q: 如何安全存储 SecretKey？",
            "A: 最佳实践：\n" +
            "1. 使用环境变量（如 OPEN_API_SECRET_KEY）而非硬编码\n" +
            "2. Spring Boot 中通过 application.properties 配置，不要提交到版本控制\n" +
            "3. 使用密钥管理系统（如 Vault、AWS Secrets Manager）\n" +
            "4. 定期轮换密钥，并在日志中避免打印完整 SecretKey"
        );

        faqs.put(
            "Q: 429 Too Many Requests - 超出限流？",
            "A: API 拥有者可以为其接口设置限流规则（每秒 QPS 和每日调用上限）。\n" +
            "AI 分析功能每个 API 每人每天最多生成 5 次报告。\n" +
            "建议在业务侧添加重试机制（指数退避），或联系 API 拥有者申请提高限额。"
        );

        faqs.put(
            "Q: SDK 与直接 HTTP 调用有什么区别？",
            "A: api-client-sdk 优势：\n" +
            "1. 自动处理签名生成（无需手动实现 HMAC-SHA256 逻辑）\n" +
            "2. 自动解包响应（直接获取 data 字段，无需解析 ApiResponse 外层结构）\n" +
            "3. 内置嵌套响应处理（自动识别并解包双层 ApiResponse）\n" +
            "4. 支持 Spring Boot 自动配置，@Resource 即可注入\n" +
            "5. 内置 5 个业务快捷方法（randomNumber/reverseText/ipInfo/mathAdd/hello）"
        );

        return ApiResponse.success(faqs);
    }

    // ==================== 私有方法 ====================

    private Map<String, Object> createAuthenticationInfo() {
        Map<String, Object> auth = new LinkedHashMap<>();
        auth.put("type", "HMAC-SHA256 签名认证（API Key 模式）");
        auth.put("invokeEndpoint", "POST /apiInfo/invoke-with-key/{apiId}");
        auth.put("requiredHeaders", new String[]{
            "X-Access-Key", "X-Signature", "X-Timestamp", "X-Nonce"
        });
        auth.put("signatureAlgorithm", "HmacSHA256");
        auth.put("signatureEncoding", "Base64 标准编码");
        auth.put("signaturePayload", "METHOD\\nPATH\\nTIMESTAMP\\nNONCE\\nBODY");
        auth.put("sdkSupport", "引入 api-client-sdk 后签名由 SDK 自动处理，无需手动实现");
        return auth;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String>[] createQuickStartSteps() {
        Map<String, String>[] steps = new HashMap[6];

        steps[0] = new LinkedHashMap<>();
        steps[0].put("step", "1. 注册账号");
        steps[0].put("description", "调用 POST /user/register 注册账号，响应中包含 AccessKey 和 SecretKey，请立即保存");
        steps[0].put("note", "SecretKey 仅显示一次，之后只能重新生成");

        steps[1] = new LinkedHashMap<>();
        steps[1].put("step", "2. 选择集成方式");
        steps[1].put("description", "推荐：引入 api-client-sdk Maven 依赖，配置三行 properties 即可使用");
        steps[1].put("alternative", "或手动实现 HMAC-SHA256 签名，参考 /api/docs/code-sample/{language}");

        steps[2] = new LinkedHashMap<>();
        steps[2].put("step", "3. 浏览 API 列表");
        steps[2].put("description", "调用 GET /apiInfo/page 浏览平台上已发布的公开 API，获取目标接口的 apiId");
        steps[2].put("note", "仅状态为「发布成功」且已上线的接口可被调用");

        steps[3] = new LinkedHashMap<>();
        steps[3].put("step", "4. 申请调用权限");
        steps[3].put("description", "如果您不是接口拥有者，需调用 POST /apiInfo/apply/{apiId} 申请权限，等待 API 拥有者审批");
        steps[3].put("note", "接口拥有者调用自己的接口无需申请权限");

        steps[4] = new LinkedHashMap<>();
        steps[4].put("step", "5. 调用接口");
        steps[4].put("sdkWay", "openApiClient.invokeGet(apiId, params) 或 openApiClient.invokePost(apiId, params)");
        steps[4].put("httpWay", "POST /apiInfo/invoke-with-key/{apiId}，携带四个签名请求头");

        steps[5] = new LinkedHashMap<>();
        steps[5].put("step", "6. 处理响应");
        steps[5].put("description", "SDK 自动解包返回 data 字段内容；直接 HTTP 调用时响应格式为 {code:0, message:'ok', data:{...}}");
        steps[5].put("successCode", "code=0 表示成功");

        return steps;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String>[] createCodeExamples() {
        Map<String, String>[] examples = new HashMap[5];

        examples[0] = new LinkedHashMap<>();
        examples[0].put("language", "Java（api-client-sdk，推荐）");
        examples[0].put("description", "引入 SDK，三行配置，开箱即用");
        examples[0].put("detailEndpoint", "GET /api/docs/code-sample/java-sdk");

        examples[1] = new LinkedHashMap<>();
        examples[1].put("language", "Java（原生 HTTP）");
        examples[1].put("description", "手动构造 HMAC-SHA256 签名，使用 HttpURLConnection");
        examples[1].put("detailEndpoint", "GET /api/docs/code-sample/java");

        examples[2] = new LinkedHashMap<>();
        examples[2].put("language", "Python");
        examples[2].put("description", "使用 requests + hmac 库，手动签名");
        examples[2].put("detailEndpoint", "GET /api/docs/code-sample/python");

        examples[3] = new LinkedHashMap<>();
        examples[3].put("language", "Node.js");
        examples[3].put("description", "使用 axios + crypto 模块，手动签名");
        examples[3].put("detailEndpoint", "GET /api/docs/code-sample/nodejs");

        examples[4] = new LinkedHashMap<>();
        examples[4].put("language", "cURL");
        examples[4].put("description", "Shell 脚本配合 openssl 生成签名");
        examples[4].put("detailEndpoint", "GET /api/docs/code-sample/curl");

        return examples;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String>[] createCommonErrors() {
        Map<String, String>[] errors = new HashMap[6];

        errors[0] = new LinkedHashMap<>();
        errors[0].put("code", "400");
        errors[0].put("error", "Bad Request - 请求参数错误或请求头不完整");
        errors[0].put("solution", "确认请求头包含 X-Access-Key / X-Signature / X-Timestamp / X-Nonce；检查请求体格式是否正确");

        errors[1] = new LinkedHashMap<>();
        errors[1].put("code", "401");
        errors[1].put("error", "Unauthorized - 签名验证失败");
        errors[1].put("solution", "检查签名格式：METHOD\\nPATH\\nTIMESTAMP\\nNONCE\\nBODY；确认 AccessKey/SecretKey 正确；确认时间戳在 ±5 分钟内");

        errors[2] = new LinkedHashMap<>();
        errors[2].put("code", "403");
        errors[2].put("error", "Forbidden - 无权调用该接口");
        errors[2].put("solution", "先调用 POST /apiInfo/apply/{apiId} 申请权限，等待 API 拥有者审批通过（status=approved）后即可调用");

        errors[3] = new LinkedHashMap<>();
        errors[3].put("code", "404");
        errors[3].put("error", "Not Found - API 不存在");
        errors[3].put("solution", "确认 apiId 正确；检查 API 是否已发布（status=3）且已上线（isOnline=1）");

        errors[4] = new LinkedHashMap<>();
        errors[4].put("code", "429");
        errors[4].put("error", "Too Many Requests - 超出限流配额");
        errors[4].put("solution", "该 API 存在限流规则，等待后重试；AI 分析功能每个 API 每人每天限 5 次");

        errors[5] = new LinkedHashMap<>();
        errors[5].put("code", "500");
        errors[5].put("error", "Internal Server Error - 服务器内部错误");
        errors[5].put("solution", "检查目标 API 提供者服务是否正常运行；查看平台日志定位具体错误");

        return errors;
    }
}

