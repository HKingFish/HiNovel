-- 模型管理和调用记录按用户数据隔离
-- 为 ai_llm_provider 和 ai_llm_call_log 表添加 user_id 字段

ALTER TABLE ai_llm_provider ADD COLUMN user_id BIGINT NULL COMMENT '所属用户ID' AFTER is_active;
ALTER TABLE ai_llm_provider ADD INDEX idx_user_id (user_id);

ALTER TABLE ai_llm_call_log ADD COLUMN user_id BIGINT NULL COMMENT '所属用户ID' AFTER is_streaming;
ALTER TABLE ai_llm_call_log ADD INDEX idx_user_id (user_id);
