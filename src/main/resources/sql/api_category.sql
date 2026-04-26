use open_api;
CREATE TABLE if not exists api_category
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name        VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);