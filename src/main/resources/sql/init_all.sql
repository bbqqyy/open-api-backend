-- ========================================================
-- Open API Platform 完整数据库初始化脚本
-- 执行方式: mysql -u root -p < init_all.sql
-- ========================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `open_api`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `open_api`;

-- ========================
-- 1. 用户表
-- ========================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`            bigint AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    `user_account`  varchar(256)                           NOT NULL COMMENT '账号',
    `user_password` varchar(512)                           NOT NULL COMMENT '密码',
    `phone_number`  varchar(64)                            NOT NULL COMMENT '电话',
    `email`         varchar(128)                                    COMMENT '邮箱',
    `user_name`     varchar(256)                           NOT NULL COMMENT '用户昵称',
    `user_avatar`   varchar(1024)                                   COMMENT '用户头像',
    `user_profile`  varchar(512)                                    COMMENT '用户简介',
    `access_key`    varchar(512)                           NOT NULL COMMENT 'accessKey',
    `secret_key`    varchar(512)                           NOT NULL COMMENT 'secretKey',
    `user_role`     varchar(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin',
    `edit_time`     datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`     tinyint      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    UNIQUE KEY `uid_userAccount` (`user_account`),
    INDEX `idx_userName` (`user_name`)
) COMMENT '用户' COLLATE = utf8mb4_unicode_ci;

-- ========================
-- 2. API 分类表
-- ========================
DROP TABLE IF EXISTS `api_category`;
CREATE TABLE `api_category`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(255)          COMMENT '分类描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT 'API分类' COLLATE = utf8mb4_unicode_ci;

-- ========================
-- 3. API 信息表
-- ========================
DROP TABLE IF EXISTS `api_info`;
CREATE TABLE `api_info`
(
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'API ID',
    `api_name`        VARCHAR(100) NOT NULL COMMENT 'API名称',
    `api_description` TEXT                  COMMENT 'API描述',
    `category_id`     BIGINT                COMMENT '分类ID',
    `url`             VARCHAR(255) NOT NULL COMMENT '请求地址',
    `method`          TINYINT      NOT NULL COMMENT '0 GET/1 POST/2 PUT/3 PATCH/4 DELETE',
    `status`          TINYINT  DEFAULT 0    COMMENT '状态 0待发布/1发布中/2发布失败/3发布成功',
    `is_online`       TINYINT  DEFAULT 0    COMMENT '0 下线/1 上线',
    `user_id`         BIGINT                COMMENT '创建用户',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT 'API信息' COLLATE = utf8mb4_unicode_ci;

-- ========================
-- 4. API 请求参数表
-- ========================
DROP TABLE IF EXISTS `api_param`;
CREATE TABLE `api_param`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
    `api_id`      BIGINT       NOT NULL COMMENT 'API ID',
    `param_name`  VARCHAR(100) NOT NULL COMMENT '参数名称',
    `param_type`  VARCHAR(50)           COMMENT '参数类型',
    `required`    TINYINT DEFAULT 0     COMMENT '是否必填 1是 0否',
    `description` VARCHAR(255)          COMMENT '参数说明'
) COMMENT 'API请求参数' COLLATE = utf8mb4_unicode_ci;

-- ========================
-- 5. API 响应参数表
-- ========================
DROP TABLE IF EXISTS `api_response_param`;
CREATE TABLE `api_response_param`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `api_id`      BIGINT       NOT NULL COMMENT 'API ID',
    `field_name`  VARCHAR(100)          COMMENT '字段名称',
    `field_type`  VARCHAR(50)           COMMENT '字段类型',
    `description` VARCHAR(255)          COMMENT '字段说明'
) COMMENT 'API响应参数' COLLATE = utf8mb4_unicode_ci;

-- ========================
-- 6. API 限流规则表
-- ========================
DROP TABLE IF EXISTS `api_limit`;
CREATE TABLE `api_limit`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `api_id`      BIGINT NOT NULL COMMENT 'API ID',
    `qps`         INT DEFAULT 10   COMMENT '每秒调用限制',
    `daily_limit` INT DEFAULT 1000 COMMENT '每日调用次数限制'
) COMMENT 'API限流规则' COLLATE = utf8mb4_unicode_ci;

-- ========================
-- 7. API 权限申请表
-- ========================
DROP TABLE IF EXISTS `api_permission`;
CREATE TABLE `api_permission`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `api_id`      BIGINT      NOT NULL COMMENT 'API ID',
    `user_id`     BIGINT      NOT NULL COMMENT '申请人（调用者）',
    `owner_id`    BIGINT      NOT NULL COMMENT 'API拥有者',
    `status`      VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT 'API权限申请' COLLATE = utf8mb4_unicode_ci;

-- ========================
-- 8. API 审核记录表
-- ========================
DROP TABLE IF EXISTS `api_review`;
CREATE TABLE `api_review`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核ID',
    `api_id`      BIGINT      NOT NULL COMMENT 'API ID',
    `admin_id`    BIGINT      NOT NULL COMMENT '管理员ID',
    `result`      VARCHAR(20)           COMMENT '审核结果 approved/rejected',
    `comment`     VARCHAR(255)          COMMENT '审核意见',
    `review_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间'
) COMMENT 'API审核记录' COLLATE = utf8mb4_unicode_ci;

-- ========================
-- 9. API 调用日志表
-- ========================
DROP TABLE IF EXISTS `api_call_log`;
CREATE TABLE `api_call_log`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `api_id`        BIGINT      NOT NULL COMMENT 'API ID',
    `user_id`       BIGINT               COMMENT '调用用户',
    `request_param` TEXT                 COMMENT '请求参数',
    `response_time` INT                  COMMENT '响应时间(ms)',
    `status`        VARCHAR(20)          COMMENT 'success/fail',
    `call_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间'
) COMMENT 'API调用日志' COLLATE = utf8mb4_unicode_ci;

-- ========================
-- 10. API 统计表
-- ========================
DROP TABLE IF EXISTS `api_statistics`;
CREATE TABLE `api_statistics`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `api_id`        BIGINT   NOT NULL COMMENT 'API ID',
    `call_count`    INT      DEFAULT 0 COMMENT '调用次数',
    `success_count` INT      DEFAULT 0 COMMENT '成功次数',
    `fail_count`    INT      DEFAULT 0 COMMENT '失败次数',
    `stat_date`     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '统计日期'
) COMMENT 'API统计' COLLATE = utf8mb4_unicode_ci;

-- ========================
-- 11. API 分析报告表
-- ========================
DROP TABLE IF EXISTS `api_analysis_report`;
CREATE TABLE `api_analysis_report`
(
    `id`                       bigint       NOT NULL AUTO_INCREMENT COMMENT '报告 ID',
    `api_id`                   bigint       NOT NULL COMMENT 'API ID',
    `report_content`           longtext     NOT NULL COMMENT 'AI 分析报告内容（JSON格式）',
    `summary`                  varchar(500)          COMMENT '报告摘要',
    `identified_issues`        longtext              COMMENT '识别的问题列表（JSON格式）',
    `optimization_suggestions` longtext              COMMENT '优化建议列表（JSON格式）',
    `success_rate`             decimal(5,2)          COMMENT 'API 成功率 (%)',
    `total_calls`              int          DEFAULT 0 COMMENT '统计周期内总调用次数',
    `avg_response_time`        decimal(10,2)         COMMENT '平均响应时间 (ms)',
    `max_response_time`        bigint                COMMENT '最大响应时间 (ms)',
    `status`                   varchar(20)  DEFAULT 'completed' COMMENT '报告状态: generated/completed/failed',
    `error_message`            varchar(255)          COMMENT '生成失败时的错误信息',
    `analysis_time_ms`         bigint                COMMENT 'AI 分析耗时 (毫秒)',
    `created_at`               datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报告生成时间',
    `updated_at`               datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_api_id` (`api_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status` (`status`),
    KEY `idx_api_created` (`api_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 分析报告表';

-- ========================
-- 12. API 告警表
-- ========================
DROP TABLE IF EXISTS `api_alert`;
CREATE TABLE `api_alert`
(
    `id`                BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '告警 ID',
    `api_id`            BIGINT       NOT NULL                   COMMENT '触发告警的 API ID',
    `api_name`          VARCHAR(100) NOT NULL                   COMMENT 'API 名称（冗余存储）',
    `owner_id`          BIGINT       NOT NULL                   COMMENT 'API 所有者 ID（告警通知对象）',
    `alert_type`        VARCHAR(50)  NOT NULL                   COMMENT '告警类型：HIGH_FAIL_RATE / HIGH_RESPONSE_TIME / NO_CALLS',
    `alert_level`       VARCHAR(20)  NOT NULL DEFAULT 'WARNING' COMMENT '告警级别：WARNING / CRITICAL',
    `alert_message`     TEXT                                    COMMENT '告警描述信息',
    `fail_rate`         DECIMAL(5,2)                            COMMENT '触发时的实际失败率（%），高失败率告警时有值',
    `avg_response_time` DECIMAL(10,2)                           COMMENT '触发时的平均响应时间（ms）',
    `total_calls`       INT          DEFAULT 0                  COMMENT '统计周期内总调用次数',
    `status`            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'  COMMENT '告警状态：ACTIVE / ACKNOWLEDGED / IGNORED',
    `created_at`        DATETIME     DEFAULT CURRENT_TIMESTAMP  COMMENT '告警创建时间',
    `updated_at`        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '告警更新时间',
    INDEX `idx_owner_id`   (`owner_id`),
    INDEX `idx_api_id`     (`api_id`),
    INDEX `idx_status`     (`status`),
    INDEX `idx_alert_type` (`alert_type`),
    INDEX `idx_created_at` (`created_at`)
) COMMENT 'API 告警记录' COLLATE = utf8mb4_unicode_ci;

-- ========================================================
-- 初始化测试数据（可选）
-- ========================================================

-- 插入管理员账号（密码: admin123456, 使用 MD5 加密后的值）
-- 实际密码加密方式请参考 UserService 中的加密逻辑
INSERT INTO `user` (`user_account`, `user_password`, `phone_number`, `user_name`, `access_key`, `secret_key`, `user_role`)
VALUES
    ('admin', 'a6a71d7d3b3c25b69b6b5f5a3d2d3e1c', '13800000000', '管理员', 'admin_ak_001', 'admin_sk_001_secret', 'admin');

-- 插入测试用分类
INSERT INTO `api_category` (`name`, `description`)
VALUES
    ('天气服务', '提供天气查询相关 API'),
    ('数据分析', '提供数据统计和分析 API'),
    ('地图服务', '提供地理位置和地图 API'),
    ('AI 能力', '提供人工智能相关 API'),
    ('通用工具', '提供通用工具类 API');

-- ========================================================
-- 完成！数据库初始化完毕
-- 共创建 11 张表
-- ========================================================
SELECT '数据库初始化完成！' AS result;
SHOW TABLES;

