-- 补齐 ai_embedding_config 表缺失的 BaseEntity 字段：creator、updater、version
ALTER TABLE ai_embedding_config
    ADD COLUMN creator BIGINT NULL COMMENT '创建人ID' AFTER deleted,
    ADD COLUMN updater BIGINT NULL COMMENT '更新人ID' AFTER creator,
    ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER updater;
