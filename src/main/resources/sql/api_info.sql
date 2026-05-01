use open_api;
drop table if exists api_info;
CREATE TABLE api_info
(

    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'API ID',
    api_name        VARCHAR(100) NOT NULL COMMENT 'API名称',
    api_description TEXT COMMENT 'API描述',
    category_id     BIGINT COMMENT '分类ID',
    url             VARCHAR(255) NOT NULL COMMENT '请求地址',
    method          TINYINT      NOT NULL COMMENT '0 GET/1 POST/2 PUT/3 PATCH/4 DELETE',
    status          TINYINT  DEFAULT 0 COMMENT '状态 0待发布/1发布中/2发布失败/3发布成功',
    is_online       TINYINT  DEFAULT 0 COMMENT '0 下线/1 上线',
    user_id         BIGINT COMMENT '创建用户',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);