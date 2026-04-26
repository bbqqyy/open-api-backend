use open_api;
CREATE TABLE api_limit
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    api_id      BIGINT NOT NULL COMMENT 'API ID',
    qps         INT DEFAULT 10 COMMENT '每秒调用限制',
    daily_limit INT DEFAULT 1000 COMMENT '每日调用次数限制'
);