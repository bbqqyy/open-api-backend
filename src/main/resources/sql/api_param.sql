use open_api;
CREATE TABLE if not exists api_param
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
    api_id      BIGINT       NOT NULL COMMENT 'API ID',
    param_name  VARCHAR(100) NOT NULL COMMENT '参数名称',
    param_type  VARCHAR(50) COMMENT '参数类型',
    required    TINYINT DEFAULT 0 COMMENT '是否必填 1是 0否',
    description VARCHAR(255) COMMENT '参数说明'
);
CREATE TABLE if not exists api_response_param
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    api_id      BIGINT NOT NULL COMMENT 'API ID',
    field_name  VARCHAR(100) COMMENT '字段名称',
    field_type  VARCHAR(50) COMMENT '字段类型',
    description VARCHAR(255) COMMENT '字段说明'
);