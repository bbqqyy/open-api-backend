use open_api;
CREATE TABLE api_permission
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    api_id      BIGINT NOT NULL COMMENT 'API ID',
    user_id     BIGINT NOT NULL COMMENT '申请人（调用者）',
    owner_id    BIGINT NOT NULL COMMENT 'API拥有者',
    status      VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);