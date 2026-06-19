-- ============================================================
-- 表名重命名 DDL：统一为 模块名_tablename 规则
-- 执行前请备份数据库
-- ============================================================

-- ==================== novel 模块（原 agent_novel_* → novel_*） ====================

-- 小说主表
ALTER TABLE agent_novel RENAME TO novel;

-- 小说章节表
ALTER TABLE agent_novel_chapter RENAME TO novel_chapter;

-- 小说章节版本表
ALTER TABLE agent_novel_chapter_version RENAME TO novel_chapter_version;

-- 小说大纲表
ALTER TABLE agent_novel_outline RENAME TO novel_outline;

-- 小说人物表
ALTER TABLE agent_novel_character RENAME TO novel_character;

-- 小说人物关系表
ALTER TABLE agent_novel_character_relation RENAME TO novel_character_relation;

-- 小说配置表
ALTER TABLE agent_novel_settings RENAME TO novel_settings;

-- 章节大纲表
ALTER TABLE agent_chapter_outline RENAME TO novel_chapter_outline;

-- ==================== agent 模块 ====================

-- Agent 配置表（原 t_agent，命名不规范）
ALTER TABLE t_agent RENAME TO agent_config;

-- 小说 Agent 绑定配置表
ALTER TABLE agent_novel_agent_config RENAME TO agent_novel_config;

-- ==================== auth 模块 ====================

-- 用户表（原 member_user）
ALTER TABLE member_user RENAME TO auth_user;

-- ==================== infra 模块 ====================

-- OSS 上传记录表（原 sys_oss_upload_log）
ALTER TABLE sys_oss_upload_log RENAME TO infra_oss_upload_log;
