-- 章节历史版本表添加备注和发布状态字段
ALTER TABLE agent_novel_chapter_version
    ADD COLUMN remark VARCHAR(255) NULL COMMENT '版本备注',
    ADD COLUMN published TINYINT DEFAULT 0 COMMENT '是否是已发布的版本 0-未发布 1-已发布',
    ADD COLUMN content_md5 VARCHAR(32) NULL COMMENT '内容MD5值，用于快速比对内容是否变化';

-- 创建索引优化查询
CREATE INDEX idx_chapter_version_published ON agent_novel_chapter_version (chapter_id, published);
CREATE INDEX idx_chapter_version_md5 ON agent_novel_chapter_version (chapter_id, content_md5);
