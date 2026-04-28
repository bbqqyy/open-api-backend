-- API 分析报告表
-- 用于存储 AI 生成的 API 分析报告，实现报告持久化和历史查询
use open_api;
CREATE TABLE IF NOT EXISTS `api_analysis_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报告 ID',
  `api_id` bigint NOT NULL COMMENT 'API ID',
  `report_content` longtext NOT NULL COMMENT 'AI 分析报告内容（JSON格式）',
  `summary` varchar(500) DEFAULT NULL COMMENT '报告摘要',
  `identified_issues` longtext COMMENT '识别的问题列表（JSON格式）',
  `optimization_suggestions` longtext COMMENT '优化建议列表（JSON格式）',
  `success_rate` decimal(5,2) DEFAULT NULL COMMENT 'API 成功率 (%)',
  `total_calls` int DEFAULT '0' COMMENT '统计周期内总调用次数',
  `avg_response_time` decimal(10,2) DEFAULT NULL COMMENT '平均响应时间 (ms)',
  `max_response_time` bigint DEFAULT NULL COMMENT '最大响应时间 (ms)',
  `status` varchar(20) DEFAULT 'completed' COMMENT '报告状态: generated(生成中), completed(已完成), failed(生成失败)',
  `error_message` varchar(255) DEFAULT NULL COMMENT '生成失败时的错误信息',
  `analysis_time_ms` bigint DEFAULT NULL COMMENT 'AI 分析耗时 (毫秒)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报告生成时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_api_id` (`api_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_status` (`status`),
  KEY `idx_api_created` (`api_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 分析报告表';

