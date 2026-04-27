# Open API 平台 - 接口文档

**项目**: Open API 后端服务
**版本**: v1.0
**最后更新**: 2026-04-27
**API Base URL**: `http://localhost:8080/api`

---

## 目录

1. [认证与授权](#认证与授权)
2. [API 管理](#api-管理)
3. [审核流程](#审核流程)
4. [测试工具](#测试工具)
5. [数据分析](#数据分析)
6. [错误处理](#错误处理)

---

## 认证与授权

### 认证方式

本项目支持两种认证方式：

#### 1. Session 认证（用户登录）
用于管理后台，基于 Cookie

#### 2. API Key 认证（第三方调用）
用于 HTTP 调用，需要在请求头中提供签名

**签名方法**:
```
Authorization: {accessKey}:{签名}
签名 = HMAC-SHA256(secretKey, 请求内容)
```

---

## API 管理

### 1. 新增 API

**请求**
```http
POST /api/info/add
Content-Type: application/json
```

**请求体**
```json
{
  "apiName": "获取用户信息",
  "apiDescription": "根据用户ID获取用户详细信息",
  "categoryId": 1,
  "url": "http://example.com/user/{id}",
  "method": "GET",
  "isOnline": 0
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| apiName | string | ✓ | API 名称 |
| apiDescription | string | ✓ | API 描述 |
| categoryId | number | ✓ | 分类 ID |
| url | string | ✓ | 请求地址 |
| method | string | ✓ | 请求方法 (GET/POST/PUT/DELETE) |
| isOnline | number | | 是否上线 (0=下线, 1=上线) |

---

### 2. 编辑 API

**请求**
```http
POST /api/info/update
Content-Type: application/json
```

**请求体**
```json
{
  "id": 1,
  "apiName": "获取用户信息（更新版）",
  "apiDescription": "根据用户ID获取详细信息",
  "categoryId": 1,
  "url": "http://example.com/api/user/{id}",
  "method": "GET"
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

---

### 3. 获取 API 列表（分页）

**请求**
```http
POST /api/info/page
Content-Type: application/json
```

**请求体**
```json
{
  "current": 1,
  "pageSize": 10,
  "apiName": "用户",
  "categoryId": 1,
  "status": 1
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 1,
        "apiName": "获取用户信息",
        "apiDescription": "根据用户ID获取用户详细信息",
        "categoryId": 1,
        "url": "http://example.com/user/1",
        "method": "GET",
        "status": 1,
        "isOnline": 1,
        "userId": 1,
        "createTime": "2026-04-27T10:00:00",
        "updateTime": "2026-04-27T10:00:00"
      }
    ],
    "total": 100,
    "pages": 10,
    "current": 1,
    "size": 10
  },
  "message": "成功"
}
```

---

### 4. 获取 API 详情

**请求**
```http
GET /api/info/detail/{apiId}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "apiName": "获取用户信息",
    "apiDescription": "根据用户ID获取用户详细信息",
    "categoryId": 1,
    "url": "http://example.com/user/{id}",
    "method": "GET",
    "status": 1,
    "isOnline": 1,
    "userId": 1,
    "params": [
      {
        "id": 1,
        "paramName": "id",
        "paramType": "integer",
        "required": 1,
        "description": "用户ID"
      }
    ],
    "responseParams": [
      {
        "id": 1,
        "fieldName": "id",
        "fieldType": "integer",
        "description": "用户ID"
      }
    ],
    "createTime": "2026-04-27T10:00:00",
    "updateTime": "2026-04-27T10:00:00"
  },
  "message": "成功"
}
```

---

### 5. 删除 API

**请求**
```http
POST /api/info/delete
Content-Type: application/json
```

**请求体**
```json
{
  "id": 1
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

---

### 6. 添加 API 参数

**请求**
```http
POST /api/param/add
Content-Type: application/json
```

**请求体**
```json
{
  "apiId": 1,
  "paramName": "userId",
  "paramType": "integer",
  "required": 1,
  "description": "用户ID"
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

---

### 7. 获取我的 API

**请求**
```http
POST /api/info/page/my
Content-Type: application/json
```

**请求体**
```json
{
  "current": 1,
  "pageSize": 10,
  "status": 1
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "records": [...],
    "total": 20,
    "pages": 2,
    "current": 1,
    "size": 10
  },
  "message": "成功"
}
```

---

## 审核流程

### 1. 获取审核进度

**请求**
```http
GET /api/review/progress/{apiId}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "apiId": 1,
    "apiName": "获取用户信息",
    "status": "releasing",
    "submitTime": "2026-04-27T10:00:00",
    "reviewDuration": 120,
    "reviewComment": "需要完善文档",
    "reviewerName": "admin",
    "reviewTime": "2026-04-27T12:00:00",
    "estimatedReviewHours": 24,
    "progressPercentage": 50
  },
  "message": "成功"
}
```

---

### 2. 获取待审核列表

**请求**
```http
GET /api/review/pending?pageNum=1&pageSize=10
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "apiId": 1,
        "apiName": "获取用户信息",
        "status": "releasing",
        "submitTime": "2026-04-27T10:00:00",
        "progressPercentage": 50
      }
    ],
    "total": 5,
    "pages": 1,
    "current": 1,
    "size": 10
  },
  "message": "成功"
}
```

---

### 3. 批量审核

**请求**
```http
POST /api/review/batch
Content-Type: application/json
```

**请求体**
```json
{
  "apiIds": [1, 2, 3],
  "result": "approved",
  "comment": "已验证，可发布",
  "priority": "normal",
  "tags": ["important", "urgent"]
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

---

### 4. 获取审核统计

**请求**
```http
GET /api/review/stats
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "pendingCount": 5,
    "approvedCount": 50,
    "rejectedCount": 10,
    "totalCount": 65,
    "approvalRate": 83.33,
    "rejectionRate": 16.67,
    "avgReviewTime": 24.5,
    "minReviewTime": 15,
    "maxReviewTime": 72,
    "weeklyNew": 8,
    "monthlyNew": 25,
    "dailyStats": [
      {
        "date": "2026-04-27",
        "pending": 3,
        "approved": 10,
        "rejected": 2,
        "avgTime": 45
      }
    ],
    "reviewerStats": [
      {
        "reviewerId": 1,
        "reviewerName": "admin",
        "totalReviewed": 20,
        "approved": 18,
        "rejected": 2,
        "approvalRate": 90.0,
        "avgReviewTime": 30
      }
    ],
    "categoryStats": [
      {
        "categoryId": 1,
        "categoryName": "用户管理",
        "pending": 2,
        "approved": 15,
        "rejected": 3,
        "approvalRate": 83.33
      }
    ]
  },
  "message": "成功"
}
```

---

### 5. 获取日期范围内的审核统计

**请求**
```http
GET /api/review/stats/range?startDate=2026-04-01&endDate=2026-04-27
```

**响应** (200 OK)
同上面的审核统计响应格式

---

### 6. 获取审核员绩效

**请求**
```http
GET /api/review/reviewer/{reviewerId}/performance
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "reviewerId": 1,
    "reviewerName": "admin",
    "totalReviewed": 50,
    "approved": 45,
    "rejected": 5,
    "approvalRate": 90.0,
    "avgReviewTime": 25
  },
  "message": "成功"
}
```

---

### 7. 获取审核员排名

**请求**
```http
GET /api/review/reviewer/rankings
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": [
    {
      "reviewerId": 1,
      "reviewerName": "admin1",
      "totalReviewed": 50,
      "approved": 48,
      "rejected": 2,
      "approvalRate": 96.0,
      "avgReviewTime": 20
    },
    {
      "reviewerId": 2,
      "reviewerName": "admin2",
      "totalReviewed": 40,
      "approved": 35,
      "rejected": 5,
      "approvalRate": 87.5,
      "avgReviewTime": 30
    }
  ],
  "message": "成功"
}
```

---

## 测试工具

### 1. 执行单次测试

**请求**
```http
POST /api/test/execute
Content-Type: application/json
```

**请求体**
```json
{
  "apiId": 1,
  "url": "http://localhost:8080/api/user/1",
  "method": "GET",
  "headers": {
    "Authorization": "Bearer token123",
    "Content-Type": "application/json"
  },
  "params": {},
  "body": null,
  "timeout": 30,
  "testName": "基础功能测试",
  "scenario": "正常请求",
  "expectedStatusCode": 200
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "request": {
      "method": "GET",
      "url": "http://localhost:8080/api/user/1",
      "headers": {...},
      "params": {},
      "timeout": 30
    },
    "response": {
      "statusCode": 200,
      "responseTime": 150,
      "responseSize": 1024,
      "success": true,
      "body": {
        "id": 1,
        "name": "John",
        "email": "john@example.com"
      }
    }
  },
  "message": "成功"
}
```

---

### 2. 批量执行测试

**请求**
```http
POST /api/test/batch
Content-Type: application/json
```

**请求体**
```json
[
  {
    "apiId": 1,
    "url": "http://localhost:8080/api/user/1",
    "method": "GET",
    "testName": "测试用例1"
  },
  {
    "apiId": 2,
    "url": "http://localhost:8080/api/user/2",
    "method": "GET",
    "testName": "测试用例2"
  }
]
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": [
    {
      "testName": "测试用例1",
      "result": "pass",
      "totalTime": 150,
      "testCaseCount": 1,
      "passedCaseCount": 1,
      "failedCaseCount": 0,
      "successRate": 100.0
    },
    {
      "testName": "测试用例2",
      "result": "pass",
      "totalTime": 160,
      "testCaseCount": 1,
      "passedCaseCount": 1,
      "failedCaseCount": 0,
      "successRate": 100.0
    }
  ],
  "message": "成功"
}
```

---

### 3. 生成测试数据

**请求**
```http
GET /api/test/data/{apiId}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "userId": 123,
    "userName": "sample_value",
    "email": "sample_value",
    "age": 25,
    "active": true
  },
  "message": "成功"
}
```

---

### 4. 获取测试数据模板

**请求**
```http
GET /api/test/template/{apiId}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "userId": {
      "type": "integer",
      "required": true,
      "description": "用户ID",
      "example": 123
    },
    "userName": {
      "type": "string",
      "required": true,
      "description": "用户名",
      "example": "sample_value"
    },
    "email": {
      "type": "string",
      "required": false,
      "description": "邮箱",
      "example": "sample_value"
    }
  },
  "message": "成功"
}
```

---

### 5. 性能测试

**请求**
```http
POST /api/test/performance/{apiId}?concurrency=10&duration=60
Content-Type: application/json
```

**请求体**
```json
[
  {
    "url": "http://localhost:8080/api/user/1",
    "method": "GET"
  }
]
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "apiId": 1,
    "testName": "性能测试",
    "testCaseCount": 600,
    "passedCaseCount": 600,
    "failedCaseCount": 0,
    "successRate": 100.0,
    "avgResponseTime": 150,
    "totalTime": 60000,
    "performanceRating": "A",
    "suggestions": [
      "性能表现良好，继续监测"
    ]
  },
  "message": "成功"
}
```

---

### 6. 压力测试

**请求**
```http
POST /api/test/stress/{apiId}?maxLoad=1000
Content-Type: application/json
```

**请求体**
```json
[
  {
    "url": "http://localhost:8080/api/user/1",
    "method": "GET"
  }
]
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "apiId": 1,
    "testName": "压力测试",
    "testCaseCount": 1000,
    "passedCaseCount": 950,
    "failedCaseCount": 50,
    "successRate": 95.0,
    "avgResponseTime": 200,
    "minResponseTime": 50,
    "maxResponseTime": 5000,
    "performanceRating": "B",
    "suggestions": [
      "提高成功率，目前为 95.00%",
      "优化响应时间，当前平均响应时间为 200ms"
    ]
  },
  "message": "成功"
}
```

---

## 数据分析

### 1. 获取 API 调用日志

**请求**
```http
POST /api/analytics/logs/list
Content-Type: application/json
```

**请求体**
```json
{
  "apiId": 1,
  "userId": null,
  "status": null,
  "startTime": "2026-04-01T00:00:00",
  "endTime": "2026-04-27T23:59:59",
  "pageNum": 1,
  "pageSize": 20
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 1,
        "apiId": 1,
        "apiName": "获取用户信息",
        "userId": 1,
        "userName": "user123",
        "params": "{\"id\": 1}",
        "responseTime": 150,
        "httpStatus": 200,
        "callTime": "2026-04-27T10:00:00"
      }
    ],
    "total": 100,
    "pages": 5,
    "current": 1,
    "size": 20
  },
  "message": "成功"
}
```

---

### 2. 获取整体分析

**请求**
```http
POST /api/analytics/overall
Content-Type: application/json
```

**请求体**
```json
{
  "startTime": "2026-04-01T00:00:00",
  "endTime": "2026-04-27T23:59:59"
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "totalCalls": 10000,
    "successfulCalls": 9800,
    "failedCalls": 200,
    "successRate": 98.0,
    "totalUsers": 500,
    "avgResponseTime": 150,
    "minResponseTime": 50,
    "maxResponseTime": 5000,
    "p95ResponseTime": 800,
    "p99ResponseTime": 2000
  },
  "message": "成功"
}
```

---

### 3. 按 API 分析

**请求**
```http
POST /api/analytics/by-api
Content-Type: application/json
```

**请求体**
```json
{
  "startTime": "2026-04-01T00:00:00",
  "endTime": "2026-04-27T23:59:59",
  "limit": 10
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": [
    {
      "apiId": 1,
      "apiName": "获取用户信息",
      "callCount": 5000,
      "successCount": 4950,
      "failureCount": 50,
      "successRate": 99.0,
      "avgResponseTime": 120
    }
  ],
  "message": "成功"
}
```

---

### 4. 按用户分析

**请求**
```http
POST /api/analytics/by-user
Content-Type: application/json
```

**请求体**
```json
{
  "startTime": "2026-04-01T00:00:00",
  "endTime": "2026-04-27T23:59:59",
  "limit": 10
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": [
    {
      "userId": 1,
      "userName": "user123",
      "callCount": 500,
      "successCount": 490,
      "failureCount": 10,
      "successRate": 98.0,
      "avgResponseTime": 160
    }
  ],
  "message": "成功"
}
```

---

### 5. 时间序列数据

**请求**
```http
POST /api/analytics/timeseries
Content-Type: application/json
```

**请求体**
```json
{
  "startTime": "2026-04-01T00:00:00",
  "endTime": "2026-04-27T23:59:59",
  "interval": "hourly"
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": [
    {
      "timestamp": "2026-04-27T10:00:00",
      "callCount": 100,
      "successCount": 98,
      "failureCount": 2,
      "avgResponseTime": 150
    }
  ],
  "message": "成功"
}
```

---

### 6. 响应时间分布

**请求**
```http
POST /api/analytics/response-distribution
Content-Type: application/json
```

**请求体**
```json
{
  "apiId": 1,
  "startTime": "2026-04-01T00:00:00",
  "endTime": "2026-04-27T23:59:59"
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "lt100ms": 2000,
    "100to300ms": 5000,
    "300to500ms": 2000,
    "500to1000ms": 800,
    "gt1000ms": 200
  },
  "message": "成功"
}
```

---

### 7. 状态码分布

**请求**
```http
POST /api/analytics/status-distribution
Content-Type: application/json
```

**请求体**
```json
{
  "apiId": 1,
  "startTime": "2026-04-01T00:00:00",
  "endTime": "2026-04-27T23:59:59"
}
```

**响应** (200 OK)
```json
{
  "code": 0,
  "data": {
    "200": 9500,
    "400": 200,
    "401": 100,
    "500": 200
  },
  "message": "成功"
}
```

---

## 错误处理

### 统一错误响应格式

```json
{
  "code": 40000,
  "data": null,
  "message": "请求参数错误"
}
```

### 常见错误码

| 错误码 | 说明 | HTTP 状态码 |
|--------|------|------------|
| 0 | 成功 | 200 |
| 40000 | 请求参数错误 | 400 |
| 40100 | 未授权 | 401 |
| 40300 | 禁止访问 | 403 |
| 40400 | 资源不存在 | 404 |
| 50000 | 服务器内部错误 | 500 |
| 50001 | 数据库错误 | 500 |
| 50002 | 第三方服务错误 | 500 |

---

## 前端开发建议

### 页面模块规划

1. **首页仪表板** (`/dashboard`)
   - 调用 `/api/analytics/overall` 显示统计
   - 调用 `/api/analytics/by-api` 显示 Top API
   - 调用 `/api/analytics/timeseries` 绘制时间序列图

2. **API 管理** (`/api/management`)
   - 获取列表: `POST /api/info/page`
   - 新增: `POST /api/info/add`
   - 编辑: `POST /api/info/update`
   - 删除: `POST /api/info/delete`
   - 查看详情: `GET /api/info/detail/{apiId}`

3. **审核管理** (`/review/management`)
   - 待审核列表: `GET /api/review/pending`
   - 批量审核: `POST /api/review/batch`
   - 统计分析: `GET /api/review/stats`

4. **测试工具** (`/test/tools`)
   - 执行测试: `POST /api/test/execute`
   - 性能测试: `POST /api/test/performance/{apiId}`
   - 压力测试: `POST /api/test/stress/{apiId}`

5. **数据分析** (`/analytics`)
   - 调用日志: `POST /api/analytics/logs/list`
   - 各类分析: `/api/analytics/*`
   - 使用 ECharts 绘制图表

### 开发流程

1. 按上述页面顺序逐个开发
2. 使用 Axios 或 Fetch API 调用接口
3. 统一处理错误响应
4. 做好加载状态和异常提示
5. 实现数据缓存和分页处理

### 推荐技术栈

- **框架**: Vue 3 或 React 18
- **HTTP 客户端**: Axios
- **UI 组件库**: Element Plus 或 Ant Design
- **图表库**: ECharts 或 Chart.js
- **状态管理**: Pinia 或 Redux

---

**文档更新日期**: 2026-04-27
**维护者**: API Team

