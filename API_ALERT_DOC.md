# API 告警功能 — 接口文档

## 基础信息

| 项目 | 值 |
|------|-----|
| Base URL | `http://localhost:8080` |
| 认证方式 | Session Cookie（需先登录，携带 `JSESSIONID`） |
| 数据格式 | `Content-Type: application/json` |
| 统一响应结构 | `{ "code": 200, "data": ..., "message": "" }` |

---

## 通用响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 状态码，`200` 成功，其余为失败 |
| `message` | string | 描述信息 |
| `data` | any | 业务数据 |

### 常见错误码

| code | 含义 |
|------|------|
| `40000` | 请求参数错误 |
| `40100` | 未登录 |
| `40101` | 无权限（非本人的告警） |
| `40400` | 告警不存在 |
| `50001` | 操作失败（告警已处理，无需重复操作） |

---

## 数据字典

### ApiAlertVO — 告警对象

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 告警唯一 ID |
| `apiId` | Long | 触发告警的 API ID |
| `apiName` | String | API 名称 |
| `alertType` | String | 告警类型枚举值（见下表） |
| `alertTypeLabel` | String | 告警类型中文，如「高失败率」 |
| `alertLevel` | String | 告警级别枚举值（见下表） |
| `alertLevelLabel` | String | 告警级别中文，如「严重」 |
| `alertMessage` | String | 详细告警描述，直接展示给用户 |
| `failRate` | BigDecimal | 触发时的实际失败率（%），仅 `HIGH_FAIL_RATE` 类型有值 |
| `avgResponseTime` | BigDecimal | 触发时的平均响应时间（ms），仅 `HIGH_RESPONSE_TIME` 类型有值 |
| `totalCalls` | Integer | 统计窗口内的总调用次数 |
| `status` | String | 告警状态枚举值（见下表） |
| `statusLabel` | String | 告警状态中文，如「待处理」 |
| `createdAt` | String | 告警创建时间，ISO 8601 格式 |
| `updatedAt` | String | 告警最近更新时间 |

### 枚举值速查表

**alertType — 告警类型**

| 枚举值 | 中文标签 | 触发条件 |
|--------|---------|---------|
| `HIGH_FAIL_RATE` | 高失败率 | 失败率 ≥ 30%（WARNING）或 ≥ 60%（CRITICAL） |
| `HIGH_RESPONSE_TIME` | 高响应时间 | 平均响应时间 ≥ 2000ms（WARNING）或 ≥ 5000ms（CRITICAL） |
| `NO_CALLS` | 长时间无调用 | 上线 API 连续 60 分钟内无任何调用记录 |

**alertLevel — 告警级别**

| 枚举值 | 中文标签 | 建议前端展示色 |
|--------|---------|--------------|
| `WARNING` | 警告 | 橙色 `#faad14` |
| `CRITICAL` | 严重 | 红色 `#f5222d` |

**status — 告警状态**

| 枚举值 | 中文标签 | 说明 |
|--------|---------|------|
| `ACTIVE` | 待处理 | 新告警，用户尚未处理 |
| `ACKNOWLEDGED` | 已确认 | 用户已确认，正在处理 |
| `IGNORED` | 已忽略 | 用户认为无需处理 |

---

## 接口列表

---

### 1. 获取我的告警列表

**用途**：告警中心主页面，展示当前用户拥有的所有 API 的告警记录，支持分页和状态过滤。

```
GET /api/alerts/my
```

**请求参数（Query）**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `status` | string | 否 | 不传 = 查全部 | 状态过滤：`ACTIVE` / `ACKNOWLEDGED` / `IGNORED` |
| `pageNum` | int | 否 | `1` | 页码，从 1 开始 |
| `pageSize` | int | 否 | `10` | 每页条数，最大 100 |

**请求示例**

```
GET /api/alerts/my?status=ACTIVE&pageNum=1&pageSize=10
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "apiId": 42,
        "apiName": "天气查询 API",
        "alertType": "HIGH_FAIL_RATE",
        "alertTypeLabel": "高失败率",
        "alertLevel": "CRITICAL",
        "alertLevelLabel": "严重",
        "alertMessage": "API「天气查询 API」在过去 30 分钟内调用失败率达 65.00%（共 20 次调用，13 次失败），已超过严重阈值 60%，请立即排查！",
        "failRate": 65.00,
        "avgResponseTime": null,
        "totalCalls": 20,
        "status": "ACTIVE",
        "statusLabel": "待处理",
        "createdAt": "2026-05-01T16:30:00",
        "updatedAt": "2026-05-01T16:30:00"
      },
      {
        "id": 2,
        "apiId": 43,
        "apiName": "地图定位 API",
        "alertType": "HIGH_RESPONSE_TIME",
        "alertTypeLabel": "高响应时间",
        "alertLevel": "WARNING",
        "alertLevelLabel": "警告",
        "alertMessage": "API「地图定位 API」在过去 30 分钟内平均响应时间为 2800 ms，超过警告阈值 2000 ms，请关注接口性能。",
        "failRate": null,
        "avgResponseTime": 2800.00,
        "totalCalls": 15,
        "status": "ACTIVE",
        "statusLabel": "待处理",
        "createdAt": "2026-05-01T16:25:00",
        "updatedAt": "2026-05-01T16:25:00"
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

**data 分页结构说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | Array\<ApiAlertVO\> | 当前页的告警数组 |
| `total` | Long | 总记录数 |
| `size` | Long | 每页条数 |
| `current` | Long | 当前页码 |
| `pages` | Long | 总页数 |

---

### 2. 获取未处理告警数量

**用途**：导航栏 / 告警图标上的角标数字，轮询此接口实现实时提示。

```
GET /api/alerts/count/active
```

**请求参数**：无

**请求示例**

```
GET /api/alerts/count/active
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": 3
}
```

> `data` 直接为 `Long` 类型整数，表示未处理（`ACTIVE`）告警的总数量。数值为 `0` 时前端可隐藏角标。

---

### 3. 查询指定 API 的活跃告警

**用途**：在 API 详情页顶部展示该 API 当前是否存在告警，引导用户关注。

```
GET /api/alerts/api/{apiId}
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `apiId` | Long | 是 | API 的唯一 ID |

**请求示例**

```
GET /api/alerts/api/42
```

**成功响应（200）** — 返回该 API 所有 `ACTIVE` 状态的告警列表（非分页）

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "apiId": 42,
      "apiName": "天气查询 API",
      "alertType": "HIGH_FAIL_RATE",
      "alertTypeLabel": "高失败率",
      "alertLevel": "CRITICAL",
      "alertLevelLabel": "严重",
      "alertMessage": "API「天气查询 API」在过去 30 分钟内调用失败率达 65.00%（共 20 次调用，13 次失败），已超过严重阈值 60%，请立即排查！",
      "failRate": 65.00,
      "avgResponseTime": null,
      "totalCalls": 20,
      "status": "ACTIVE",
      "statusLabel": "待处理",
      "createdAt": "2026-05-01T16:30:00",
      "updatedAt": "2026-05-01T16:30:00"
    }
  ]
}
```

> 无告警时 `data` 为空数组 `[]`，前端判断 `data.length === 0` 则不展示告警横幅。

**失败响应**

```json
{
  "code": 40000,
  "message": "请求参数错误",
  "data": null
}
```

---

### 4. 确认告警

**用途**：用户点击「已确认」按钮，表示已知晓该问题并正在处理中，状态从 `ACTIVE` 变更为 `ACKNOWLEDGED`。

```
POST /api/alerts/{alertId}/acknowledge
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `alertId` | Long | 是 | 告警 ID |

**请求体**：无（无需 Body）

**请求示例**

```
POST /api/alerts/1/acknowledge
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**失败响应示例**

```json
{
  "code": 40400,
  "message": "告警不存在",
  "data": null
}
```

```json
{
  "code": 40101,
  "message": "无权操作该告警",
  "data": null
}
```

```json
{
  "code": 50001,
  "message": "该告警已处理，无需重复操作",
  "data": null
}
```

---

### 5. 忽略告警

**用途**：用户点击「忽略」按钮，表示该告警无需处理（如已知误报），状态从 `ACTIVE` 变更为 `IGNORED`。

```
POST /api/alerts/{alertId}/ignore
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `alertId` | Long | 是 | 告警 ID |

**请求体**：无（无需 Body）

**请求示例**

```
POST /api/alerts/1/ignore
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**失败响应**：与「确认告警」接口相同，参考接口 4。

---

## 前端页面实现建议

### 告警中心页面结构

```
┌─────────────────────────────────────────────────────┐
│  告警中心                              [全部▼]       │
├──────────┬──────────────┬────────────────────────────┤
│ 待处理(3) │ 已确认(1)    │ 已忽略(5)                  │
├──────────┴──────────────┴────────────────────────────┤
│ ⚠ 天气查询 API          高失败率  [严重] [待处理]      │
│   失败率 65%，20 次调用                               │
│   "API在过去30分钟内失败率达65%..."                   │
│   2026-05-01 16:30          [确认]  [忽略]            │
├──────────────────────────────────────────────────────┤
│ ⚡ 地图定位 API         高响应时间 [警告] [待处理]     │
│   平均响应 2800ms，15 次调用                          │
│   "API平均响应时间超过2000ms..."                      │
│   2026-05-01 16:25          [确认]  [忽略]            │
└──────────────────────────────────────────────────────┘
```

### 关键交互逻辑

**1. 导航角标（定时轮询）**

```javascript
// 每 60 秒轮询一次未处理告警数
const pollAlertCount = async () => {
  const res = await fetch('/api/alerts/count/active');
  const { data } = await res.json();
  setBadgeCount(data); // data 为 0 时隐藏角标
};
setInterval(pollAlertCount, 60000);
pollAlertCount(); // 页面加载时立即执行一次
```

**2. 加载告警列表**

```javascript
const fetchAlerts = async (status = '', pageNum = 1, pageSize = 10) => {
  const params = new URLSearchParams({ pageNum, pageSize });
  if (status) params.append('status', status);
  const res = await fetch(`/api/alerts/my?${params}`);
  const { code, data } = await res.json();
  if (code === 200) {
    setAlerts(data.records);   // 告警列表
    setTotal(data.total);      // 总数，用于分页器
  }
};
```

**3. 确认 / 忽略告警**

```javascript
// action: 'acknowledge' | 'ignore'
const handleAlert = async (alertId, action) => {
  const res = await fetch(`/api/alerts/${alertId}/${action}`, {
    method: 'POST'
  });
  const { code, message } = await res.json();
  if (code === 200) {
    message.success('操作成功');
    fetchAlerts();      // 刷新列表
    pollAlertCount();   // 刷新角标
  } else {
    message.error(message);
  }
};
```

**4. API 详情页告警提示横幅**

```javascript
// 进入 API 详情页时调用
const fetchApiAlerts = async (apiId) => {
  const res = await fetch(`/api/alerts/api/${apiId}`);
  const { data } = await res.json();
  if (data && data.length > 0) {
    // 存在 CRITICAL 告警时用红色横幅，WARNING 用橙色
    const hasCritical = data.some(a => a.alertLevel === 'CRITICAL');
    showAlertBanner(data, hasCritical ? 'error' : 'warning');
  }
};
```

### 推荐样式规范

| 场景 | 颜色建议 |
|------|---------|
| `CRITICAL` 级别标签 | 红底白字 `#f5222d` |
| `WARNING` 级别标签 | 橙底白字 `#fa8c16` |
| `ACTIVE` 状态标签 | 蓝底 `#1890ff` |
| `ACKNOWLEDGED` 状态标签 | 绿底 `#52c41a` |
| `IGNORED` 状态标签 | 灰底 `#8c8c8c` |
| `HIGH_FAIL_RATE` 图标 | ❌ 或 🔴 |
| `HIGH_RESPONSE_TIME` 图标 | ⚡ 或 🐢 |
| `NO_CALLS` 图标 | 📭 或 ⚠️ |

