use open_api;
CREATE TABLE api_call_log
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    api_id        BIGINT NOT NULL COMMENT 'API ID',
    user_id       BIGINT COMMENT '调用用户',
    request_param TEXT COMMENT '请求参数',
    response_time INT COMMENT '响应时间(ms)',
    status        VARCHAR(20) COMMENT 'success/fail',
    call_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间'
);