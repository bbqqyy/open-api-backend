CREATE TABLE api_review
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核ID',
    api_id      BIGINT NOT NULL COMMENT 'API ID',
    admin_id    BIGINT NOT NULL COMMENT '管理员ID',
    result      VARCHAR(20) COMMENT '审核结果 approved/rejected',
    comment     VARCHAR(255) COMMENT '审核意见',
    review_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间'
);