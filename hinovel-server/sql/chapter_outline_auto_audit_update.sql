-- 为章节大纲表添加自动审核字段
-- 执行时间：2026-03-14
-- 作者：haowl

-- 添加 auto_audit 字段
ALTER TABLE agent_chapter_outline
    ADD COLUMN auto_audit TINYINT DEFAULT 1 COMMENT '是否自动审核（0-否，1-是）'
AFTER scene_setting;
