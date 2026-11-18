-- 适用于已有数据的 MySQL 8.0 数据库，在目标库执行一次。
-- 保留存量字段值。升级前由数据库填入的默认值继续作为显式配置。
ALTER TABLE novel_settings
    DROP INDEX uk_novel_id,
    ADD UNIQUE KEY uk_settings_scope (
        novel_id, (CASE WHEN novel_id = 0 THEN COALESCE(user_id, 0) ELSE 0 END), deleted
    ),
    MODIFY COLUMN auto_audit_after_rewrite TINYINT DEFAULT NULL COMMENT '改写后自动审核',
    MODIFY COLUMN auto_outline_after_publish TINYINT DEFAULT NULL COMMENT '发布后自动生成大纲',
    MODIFY COLUMN auto_vector_after_publish TINYINT DEFAULT NULL COMMENT '发布后自动向量入库',
    MODIFY COLUMN rewrite_context_chapters INT DEFAULT NULL COMMENT '改写携带前几章',
    MODIFY COLUMN rewrite_include_outline TINYINT DEFAULT NULL COMMENT '改写是否携带全文大纲',
    MODIFY COLUMN audit_include_outline TINYINT DEFAULT NULL COMMENT '审核是否携带全文大纲',
    MODIFY COLUMN qa_include_outline TINYINT DEFAULT NULL COMMENT '问答是否携带全文大纲',
    MODIFY COLUMN qa_context_length INT DEFAULT NULL COMMENT '问答历史消息条数',
    MODIFY COLUMN auto_save_content TINYINT DEFAULT NULL COMMENT '是否自动保存编辑器内容';
