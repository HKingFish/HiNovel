-- 嵌入式模型配置表（用户隔离）
-- @date 2026/3/23

-- 初始化 user_id=1 的百炼 Embedding 配置（api_key 为空，请通过页面填写后保存）
INSERT INTO `ai_embedding_config` (`user_id`, `name`, `model_type`, `base_url`, `api_key`, `model_name`, `dimensions`, `is_active`, `deleted`)
VALUES (1, '阿里云百炼 text-embedding-v3', 'openai', 'https://dashscope.aliyuncs.com/compatible-mode/v1', NULL, 'text-embedding-v3', 1024, 1, 0);

CREATE TABLE IF NOT EXISTS `ai_embedding_config` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '所属用户 ID',
    `name`        VARCHAR(64)  NOT NULL COMMENT '配置名称',
    `model_type`  VARCHAR(32)  NOT NULL DEFAULT 'openai' COMMENT '模型类型（openai/onnx）',
    `base_url`    VARCHAR(256) DEFAULT NULL COMMENT 'API 基础地址',
    `api_key`     VARCHAR(512) DEFAULT NULL COMMENT 'API 密钥（加密存储）',
    `model_name`  VARCHAR(128) DEFAULT NULL COMMENT '模型名称',
    `dimensions`  INT          DEFAULT 1024 COMMENT '向量维度',
    `is_active`   TINYINT      NOT NULL DEFAULT 1 COMMENT '是否激活（1=激活，0=停用）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0=正常，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '嵌入式模型配置表';
