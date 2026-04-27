package com.bqy.openapibackend.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * API 客户端示例代码
 *
 * 展示如何在其他项目中使用 API Key 认证调用您平台的 API
 */
public class ApiClientExample {

    /**
     * 示例 1: 使用 Java 原生 HttpURLConnection 调用
     */
    public static void example1_NativeHttp() throws IOException {
        String accessKey = "your_access_key_here";
        String secretKey = "your_secret_key_here";
        String apiHost = "http://your-api-platform.com";
        String apiId = "123";

        // 1. 准备请求参数
        String method = "GET";
        String path = "/invoke-with-key/" + apiId;
        Long timestamp = System.currentTimeMillis();
        String nonce = "nonce_" + System.nanoTime();
        String body = ""; // GET 请求无 body

        // 2. 生成签名
        String signature = ApiKeyAuthUtils.generateSignature(
            method,
            path,
            timestamp,
            nonce,
            body,
            secretKey
        );

        // 3. 构建 URL 和请求头
        URL url = new URL(apiHost + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("X-Access-Key", accessKey);
        conn.setRequestProperty("X-Signature", signature);
        conn.setRequestProperty("X-Timestamp", String.valueOf(timestamp));
        conn.setRequestProperty("X-Nonce", nonce);
        conn.setRequestProperty("Content-Type", "application/json");

        // 4. 发送请求
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
    }

    /**
     * 示例 2: 使用 OkHttp 库调用 (更推荐)
     *
     * Maven 依赖:
     * <dependency>
     *     <groupId>com.squareup.okhttp3</groupId>
     *     <artifactId>okhttp</artifactId>
     *     <version>4.10.0</version>
     * </dependency>
     */
    public static void example2_OkHttp() {
        // String accessKey = "your_access_key_here";
        // String secretKey = "your_secret_key_here";
        // String apiHost = "http://your-api-platform.com";
        // String apiId = "123";
        //
        // // 1. 准备请求
        // String method = "POST";
        // String path = "/invoke-with-key/" + apiId;
        // Long timestamp = System.currentTimeMillis();
        // String nonce = ApiKeyAuthUtils.generateNonce();
        // String body = "{\"param1\": \"value1\"}";
        //
        // // 2. 生成签名
        // String signature = ApiKeyAuthUtils.generateSignature(
        //     method,
        //     path,
        //     timestamp,
        //     nonce,
        //     body,
        //     secretKey
        // );
        //
        // // 3. 创建请求
        // OkHttpClient client = new OkHttpClient();
        // RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/json"));
        //
        // Request request = new Request.Builder()
        //     .url(apiHost + path)
        //     .post(requestBody)
        //     .addHeader("X-Access-Key", accessKey)
        //     .addHeader("X-Signature", signature)
        //     .addHeader("X-Timestamp", String.valueOf(timestamp))
        //     .addHeader("X-Nonce", nonce)
        //     .build();
        //
        // // 4. 发送请求
        // try (Response response = client.newCall(request).execute()) {
        //     System.out.println("Response: " + response.body().string());
        // }
    }

    /**
     * 示例 3: 使用 Python 调用 (给其他语言开发者参考)
     *
     * pip install requests
     */
    public static void example3_Python() {
        String pythonCode = """
import hashlib
import hmac
import base64
import requests
from datetime import datetime

# 配置
ACCESS_KEY = "your_access_key_here"
SECRET_KEY = "your_secret_key_here"
API_HOST = "http://your-api-platform.com"
API_ID = "123"

# 1. 准备请求数据
method = "POST"
path = f"/invoke-with-key/{API_ID}"
timestamp = int(datetime.now().timestamp() * 1000)
nonce = f"nonce_{timestamp}"
body = '{"param1": "value1"}'

# 2. 构建待签名字符串
payload = f"{method}\\n{path}\\n{timestamp}\\n{nonce}\\n{body}"

# 3. 生成签名
signature = base64.b64encode(
    hmac.new(
        SECRET_KEY.encode(),
        payload.encode(),
        hashlib.sha256
    ).digest()
).decode()

# 4. 发送请求
headers = {
    "X-Access-Key": ACCESS_KEY,
    "X-Signature": signature,
    "X-Timestamp": str(timestamp),
    "X-Nonce": nonce,
    "Content-Type": "application/json"
}

response = requests.post(
    f"{API_HOST}{path}",
    headers=headers,
    data=body
)

print(f"Status Code: {response.status_code}")
print(f"Response: {response.json()}")
        """;
        System.out.println(pythonCode);
    }

    /**
     * 示例 4: 使用 JavaScript/Node.js 调用
     *
     * npm install crypto-js axios
     */
    public static void example4_JavaScript() {
        String jsCode = """
const crypto = require('crypto');
const axios = require('axios');

const ACCESS_KEY = 'your_access_key_here';
const SECRET_KEY = 'your_secret_key_here';
const API_HOST = 'http://your-api-platform.com';
const API_ID = '123';

// 1. 准备请求数据
const method = 'POST';
const path = `/invoke-with-key/${API_ID}`;
const timestamp = Date.now();
const nonce = `nonce_${timestamp}`;
const body = JSON.stringify({param1: 'value1'});

// 2. 构建待签名字符串
const payload = `${method}\\n${path}\\n${timestamp}\\n${nonce}\\n${body}`;

// 3. 生成签名
const signature = crypto
    .createHmac('sha256', SECRET_KEY)
    .update(payload)
    .digest('base64');

// 4. 发送请求
axios.post(
    `${API_HOST}${path}`,
    body,
    {
        headers: {
            'X-Access-Key': ACCESS_KEY,
            'X-Signature': signature,
            'X-Timestamp': timestamp.toString(),
            'X-Nonce': nonce,
            'Content-Type': 'application/json'
        }
    }
).then(response => {
    console.log('Status:', response.status);
    console.log('Data:', response.data);
}).catch(error => {
    console.error('Error:', error);
});
        """;
        System.out.println(jsCode);
    }

    /**
     * 生成 AccessKey 和 SecretKey (服务端使用)
     *
     * 这些密钥应该在用户在您的平台注册时自动生成
     */
    public static class KeyGenerator {

        /**
         * 生成随机的 AccessKey
         */
        public static String generateAccessKey() {
            return "ak_" + generateRandomString(32);
        }

        /**
         * 生成随机的 SecretKey
         */
        public static String generateSecretKey() {
            return "sk_" + generateRandomString(32);
        }

        /**
         * 生成随机字符串
         */
        private static String generateRandomString(int length) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder result = new StringBuilder();
            java.util.Random random = new java.util.Random();

            for (int i = 0; i < length; i++) {
                result.append(chars.charAt(random.nextInt(chars.length())));
            }

            return result.toString();
        }
    }

    /**
     * ============================================================================
     * 如何获取 AccessKey 和 SecretKey
     * ============================================================================
     *
     * 以下是用户从您的平台获取 API 密钥的完整流程说明：
     */
    public static class HowToGetApiKeys {

        /**
         * 步骤 1: 注册账号
         *
         * 调用 POST /user/register 接口，提交以下信息：
         *
         * {
         *   "userAccount": "your_username",
         *   "userPassWord": "your_password",
         *   "checkPassWord": "your_password",
         *   "userName": "Your Display Name",
         *   "phoneNumber": "13800138000"
         * }
         *
         * 响应包含：
         * {
         *   "code": 0,
         *   "message": "success",
         *   "data": {
         *     "userId": 123,
         *     "userAccount": "your_username",
         *     "userName": "Your Display Name",
         *     "accessKey": "ak_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
         *     "secretKey": "sk_yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy",
         *     "message": "✅ 注册成功！请妥善保存您的 AccessKey 和 SecretKey..."
         *   }
         * }
         *
         * ⚠️  关键提示：
         * - SecretKey 仅在注册时显示一次！
         * - 请立即复制并妥善保存 AccessKey 和 SecretKey
         * - 不要分享或暴露 SecretKey 给任何人
         * - 不要在代码中硬编码这些密钥
         *
         */
        public static final String STEP_1_REGISTRATION = "见上方注释";

        /**
         * 步骤 2: 登录后查看密钥
         *
         * 如果用户需要查看已有的密钥（不包含完整的 SecretKey），调用：
         *
         * GET /user/api-keys
         *
         * 需要在请求头中包含 Session Cookie（登录后自动获得）：
         * Cookie: JSESSIONID=xxxxx
         *
         * 响应：
         * {
         *   "code": 0,
         *   "message": "success",
         *   "data": {
         *     "accessKey": "ak_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
         *     "secretKeyMasked": "sk_xxxx****xxxx",  // 出于安全考虑只显示掩码
         *     "lastUpdated": "2026-04-10 10:30:00",
         *     "message": "完整的 Secret Key 仅在注册时显示一次..."
         *   }
         * }
         *
         */
        public static final String STEP_2_VIEW_KEYS = "见上方注释";

        /**
         * 步骤 3: 密钥丢失？重新生成
         *
         * 如果用户遗忘或泄露了 SecretKey，可以重新生成新的密钥。
         *
         * 调用 POST /user/regenerate-api-keys
         *
         * 需要登录状态（同样需要 Session Cookie）。
         *
         * 响应：
         * {
         *   "code": 0,
         *   "message": "success",
         *   "data": {
         *     "userId": 123,
         *     "userAccount": "your_username",
         *     "userName": "Your Display Name",
         *     "accessKey": "ak_new_access_key_xxxxxxxxxxxxxx",
         *     "secretKey": "sk_new_secret_key_yyyyyyyyyyyyyyy",
         *     "message": "✅ 密钥已重新生成！旧的密钥已失效..."
         *   }
         * }
         *
         * ⚠️  重要提示：
         * - 重新生成后，旧的密钥立即失效！
         * - 所有使用旧密钥的应用调用都会被拒绝
         * - 必须立即在所有应用中更新新的密钥
         * - 新的 SecretKey 仅显示一次，请立即保存
         *
         */
        public static final String STEP_3_REGENERATE_KEYS = "见上方注释";

        /**
         * 前端界面需要提供的功能
         *
         * 1. 【注册页面】
         *    - 表单字段: 账号、密码、确认密码、昵称、电话
         *    - 提交后显示"保存成功"提示
         *    - 在显著位置显示生成的 AccessKey 和 SecretKey
         *    - 提供"复制"按钮，方便用户一键复制
         *    - 提供"下载"按钮，允许下载为文本或 JSON 文件
         *    - 显示警告信息："SecretKey 仅显示一次，遗忘后无法恢复！"
         *    - 要求用户勾选"已保存"才能继续
         *    - 继续后跳转到集成指南或 API 文档
         *
         * 2. 【用户中心 > 账号安全页面】
         *    - 显示当前的 AccessKey
         *    - 显示掩码的 SecretKey（如 sk_xxxx****xxxx）
         *    - 显示密钥最后更新的时间
         *    - 提供"复制 AccessKey"按钮
         *    - 提供"重新生成密钥"按钮（红色，带警告图标）
         *
         * 3. 【重新生成密钥确认对话框】
         *    - 显示警告："重新生成后，旧的密钥将立即失效"
         *    - 列出可能受影响的系统或应用
         *    - 要求用户二次确认
         *    - 生成成功后，用与注册相同的方式显示新密钥
         *    - 提醒用户立即在所有应用中更新新的密钥
         *
         * 4. 【帮助文档】
         *    - 详细说明 API Key 认证流程
         *    - 提供各种编程语言的完整示例代码
         *    - 包含常见错误排查指南
         *    - 提供"客服支持"链接
         *
         */
        public static final String FRONTEND_UI_REQUIREMENTS = "见上方注释";
    }
}

