-- ========================================================
-- 测试数据初始化脚本
-- 用于测试 API 平台功能（接口提供者 + 接口调用者）
-- api-provider 子项目运行在 http://localhost:8081
-- ========================================================

USE `open_api`;

-- ========================
-- 1. 插入测试用户
-- ========================
-- 用户1: API提供者（会创建API供别人调用）
INSERT INTO `user` (`id`, `user_account`, `user_password`, `phone_number`, `user_name`, `access_key`, `secret_key`, `user_role`)
VALUES
    (10, 'provider_user', 'test123456', '13900000010', 'API提供者',
     'ak_provider_test_access_key_001', 'sk_provider_test_secret_key_001', 'user')
ON DUPLICATE KEY UPDATE `user_name` = VALUES(`user_name`);

-- 用户2: API调用者（会申请并调用别人的API）
INSERT INTO `user` (`id`, `user_account`, `user_password`, `phone_number`, `user_name`, `access_key`, `secret_key`, `user_role`)
VALUES
    (11, 'caller_user', 'test123456', '13900000011', 'API调用者',
     'ak_caller_test_access_key_001', 'sk_caller_test_secret_key_001', 'user')
ON DUPLICATE KEY UPDATE `user_name` = VALUES(`user_name`);

-- ========================
-- 2. 插入测试 API（由 provider_user 创建，指向 api-provider 项目）
-- ========================
-- API 1: 随机数生成接口（GET）
INSERT INTO `api_info` (`id`, `api_name`, `api_description`, `category_id`, `url`, `method`, `status`, `is_online`, `user_id`)
VALUES
    (1, '随机数生成', '生成指定范围内的随机整数，支持设置最小值和最大值', 5,
     'http://localhost:8081/api/random/number', 0, 3, 1, 10)
ON DUPLICATE KEY UPDATE `api_name` = VALUES(`api_name`), `url` = VALUES(`url`);

-- API 2: 文本翻转接口（POST）
INSERT INTO `api_info` (`id`, `api_name`, `api_description`, `category_id`, `url`, `method`, `status`, `is_online`, `user_id`)
VALUES
    (2, '文本翻转', '将输入的文本进行翻转，例如 "hello" 变成 "olleh"', 5,
     'http://localhost:8081/api/text/reverse', 1, 3, 1, 10)
ON DUPLICATE KEY UPDATE `api_name` = VALUES(`api_name`), `url` = VALUES(`url`);

-- API 3: IP信息查询接口（GET）
INSERT INTO `api_info` (`id`, `api_name`, `api_description`, `category_id`, `url`, `method`, `status`, `is_online`, `user_id`)
VALUES
    (3, 'IP信息查询', '查询指定IP地址的基本信息，包括地区、运营商等模拟数据', 5,
     'http://localhost:8081/api/ip/info', 0, 3, 1, 10)
ON DUPLICATE KEY UPDATE `api_name` = VALUES(`api_name`), `url` = VALUES(`url`);

-- API 4: 加法计算接口（GET）
INSERT INTO `api_info` (`id`, `api_name`, `api_description`, `category_id`, `url`, `method`, `status`, `is_online`, `user_id`)
VALUES
    (4, '加法计算', '计算两个数字之和，支持整数和小数', 5,
     'http://localhost:8081/api/math/add', 0, 3, 1, 10)
ON DUPLICATE KEY UPDATE `api_name` = VALUES(`api_name`), `url` = VALUES(`url`);

-- API 5: 问候接口（POST）
INSERT INTO `api_info` (`id`, `api_name`, `api_description`, `category_id`, `url`, `method`, `status`, `is_online`, `user_id`)
VALUES
    (5, '个性化问候', '根据用户名返回个性化的问候语，支持中英文', 5,
     'http://localhost:8081/api/hello', 1, 3, 1, 10)
ON DUPLICATE KEY UPDATE `api_name` = VALUES(`api_name`), `url` = VALUES(`url`);

-- ========================
-- 3. 插入请求参数定义
-- ========================
-- API 1 参数（随机数）
INSERT INTO `api_param` (`api_id`, `param_name`, `param_type`, `required`, `description`)
VALUES
    (1, 'min', 'Integer', 0, '最小值，默认为 0'),
    (1, 'max', 'Integer', 0, '最大值，默认为 100')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- API 2 参数（文本翻转）
INSERT INTO `api_param` (`api_id`, `param_name`, `param_type`, `required`, `description`)
VALUES
    (2, 'text', 'String', 1, '需要翻转的文本内容，长度不超过1000字符')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- API 3 参数（IP查询）
INSERT INTO `api_param` (`api_id`, `param_name`, `param_type`, `required`, `description`)
VALUES
    (3, 'ip', 'String', 1, '需要查询的 IP 地址，支持 IPv4 格式')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- API 4 参数（加法）
INSERT INTO `api_param` (`api_id`, `param_name`, `param_type`, `required`, `description`)
VALUES
    (4, 'a', 'Double', 1, '第一个数字'),
    (4, 'b', 'Double', 1, '第二个数字')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- API 5 参数（问候）
INSERT INTO `api_param` (`api_id`, `param_name`, `param_type`, `required`, `description`)
VALUES
    (5, 'name', 'String', 1, '用户名，用于生成个性化问候语'),
    (5, 'language', 'String', 0, '语言，支持 zh（中文）和 en（英文），默认 zh')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- ========================
-- 4. 插入响应参数定义
-- ========================
-- API 1 响应（随机数）
INSERT INTO `api_response_param` (`api_id`, `field_name`, `field_type`, `description`)
VALUES
    (1, 'result', 'Integer', '生成的随机整数'),
    (1, 'min', 'Integer', '使用的最小值范围'),
    (1, 'max', 'Integer', '使用的最大值范围');

-- API 2 响应（文本翻转）
INSERT INTO `api_response_param` (`api_id`, `field_name`, `field_type`, `description`)
VALUES
    (2, 'original', 'String', '原始文本'),
    (2, 'reversed', 'String', '翻转后的文本'),
    (2, 'length', 'Integer', '文本长度');

-- API 3 响应（IP查询）
INSERT INTO `api_response_param` (`api_id`, `field_name`, `field_type`, `description`)
VALUES
    (3, 'ip', 'String', '查询的 IP 地址'),
    (3, 'country', 'String', '国家'),
    (3, 'region', 'String', '地区/省份'),
    (3, 'city', 'String', '城市'),
    (3, 'isp', 'String', '网络运营商');

-- API 4 响应（加法）
INSERT INTO `api_response_param` (`api_id`, `field_name`, `field_type`, `description`)
VALUES
    (4, 'a', 'Double', '第一个数字'),
    (4, 'b', 'Double', '第二个数字'),
    (4, 'result', 'Double', '计算结果（a + b）');

-- API 5 响应（问候）
INSERT INTO `api_response_param` (`api_id`, `field_name`, `field_type`, `description`)
VALUES
    (5, 'greeting', 'String', '个性化问候语'),
    (5, 'name', 'String', '用户名'),
    (5, 'timestamp', 'String', '问候时间');

-- ========================
-- 5. 插入限流规则
-- ========================
INSERT INTO `api_limit` (`api_id`, `qps`, `daily_limit`)
VALUES
    (1, 10, 1000),
    (2, 10, 1000),
    (3, 5, 500),
    (4, 20, 2000),
    (5, 10, 1000)
ON DUPLICATE KEY UPDATE `qps` = VALUES(`qps`), `daily_limit` = VALUES(`daily_limit`);

-- ========================
-- 6. 插入权限记录（caller_user 有调用所有API的权限）
-- ========================
INSERT INTO `api_permission` (`api_id`, `user_id`, `owner_id`, `status`)
VALUES
    (1, 11, 10, 'approved'),
    (2, 11, 10, 'approved'),
    (3, 11, 10, 'approved'),
    (4, 11, 10, 'approved'),
    (5, 11, 10, 'approved')
ON DUPLICATE KEY UPDATE `status` = 'approved';

-- ========================
-- 7. 插入审核记录（admin 审核通过这些API）
-- ========================
INSERT INTO `api_review` (`api_id`, `admin_id`, `result`, `comment`)
VALUES
    (1, 1, '发布成功', '接口功能正常，审核通过'),
    (2, 1, '发布成功', '接口功能正常，审核通过'),
    (3, 1, '发布成功', '接口功能正常，审核通过'),
    (4, 1, '发布成功', '接口功能正常，审核通过'),
    (5, 1, '发布成功', '接口功能正常，审核通过');

SELECT '测试数据插入完成！' AS result;
SELECT '用户数据:' AS info;
SELECT id, user_account, user_name, access_key, secret_key FROM user;
SELECT 'API数据:' AS info;
SELECT id, api_name, url, method, status, is_online FROM api_info;
SELECT '权限数据:' AS info;
SELECT * FROM api_permission;

