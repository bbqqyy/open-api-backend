use open_api;
CREATE TABLE api_statistics
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    api_id        BIGINT                             NOT NULL COMMENT 'API ID',
    call_count    INT      DEFAULT 0 COMMENT '调用次数',
    success_count INT      DEFAULT 0 COMMENT '成功次数',
    fail_count    INT      DEFAULT 0 COMMENT '失败次数',
    stat_date     DATETIME default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP COMMENT '统计日期'
);