-- ========================================================
-- HiNovel AI 小说创作平台 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- ========================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hinovel_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE hinovel_platform;

-- ========================================================
-- 1. 用户认证模块 (hinovel-module-auth)
-- ========================================================

-- 用户表
CREATE TABLE IF NOT EXISTS auth_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    email VARCHAR(128) NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    avatar_url VARCHAR(512) COMMENT '头像URL',
    role VARCHAR(16) DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    status VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
    creator BIGINT COMMENT '创建人ID',
    updater BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE KEY uk_email (email),
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================================
-- 2. 小说创作模块 (hinovel-module-agent)
-- ========================================================

-- 小说表
CREATE TABLE IF NOT EXISTS agent_novel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '小说ID',
    user_id BIGINT NOT NULL COMMENT '作者用户ID',
    title VARCHAR(256) NOT NULL COMMENT '小说标题',
    description TEXT COMMENT '小说简介',
    cover_url VARCHAR(512) COMMENT '封面图片URL',
    status VARCHAR(16) DEFAULT 'ONGOING' COMMENT '状态: ONGOING-连载中, COMPLETED-已完结, PAUSED-暂停',
    word_count BIGINT DEFAULT 0 COMMENT '总字数',
    chapter_count INT DEFAULT 0 COMMENT '总章节数',
    creator BIGINT COMMENT '创建人ID',
    updater BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说表';

-- 小说大纲表
CREATE TABLE IF NOT EXISTS agent_novel_outline (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '大纲ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    content TEXT COMMENT '大纲内容(Markdown)',
    creator BIGINT COMMENT '创建人ID',
    updater BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE KEY uk_novel_id (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说大纲表';

-- 小说章节表
CREATE TABLE IF NOT EXISTS agent_novel_chapter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '章节ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    chapter_number INT DEFAULT 0 COMMENT '章节数（第几章）',
    title VARCHAR(256) NOT NULL COMMENT '章节标题',
    content LONGTEXT COMMENT '章节内容',
    word_count INT DEFAULT 0 COMMENT '章节字数',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    status VARCHAR(16) DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PUBLISHED-已发布',
    creator BIGINT COMMENT '创建人ID',
    updater BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_novel_id (novel_id),
    INDEX idx_chapter_number (chapter_number),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说章节表';

-- 章节大纲表
CREATE TABLE IF NOT EXISTS agent_chapter_outline (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '章节大纲ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    chapter_id BIGINT COMMENT '章节ID',
    outline_content TEXT COMMENT '大纲内容',
    plot_points TEXT COMMENT '剧情要点(JSON)',
    involved_characters TEXT COMMENT '涉及人物(JSON)',
    emotion_tone VARCHAR(64) COMMENT '情感基调',
    scene_setting VARCHAR(512) COMMENT '场景设置',
    auto_audit TINYINT DEFAULT 1 COMMENT '是否自动审核（0-否，1-是）',
    creator BIGINT COMMENT '创建人ID',
    updater BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_novel_id (novel_id),
    INDEX idx_chapter_id (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节大纲表';

-- 章节版本历史表
CREATE TABLE IF NOT EXISTS agent_novel_chapter_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '版本ID',
    chapter_id BIGINT NOT NULL COMMENT '所属章节ID',
    content LONGTEXT COMMENT '版本完整内容',
    word_count INT DEFAULT 0 COMMENT '版本字数',
    change_diff TEXT COMMENT '与上一版本的diff',
    created_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_chapter_id (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节版本历史表';

-- 小说人物表
CREATE TABLE IF NOT EXISTS agent_novel_character (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '人物ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    name VARCHAR(128) NOT NULL COMMENT '人物姓名',
    alias VARCHAR(256) COMMENT '别名/绰号',
    avatar_url VARCHAR(512) COMMENT '人物头像URL',
    gender VARCHAR(16) COMMENT '性别: MALE-男, FEMALE-女, OTHER-其他',
    age INT COMMENT '年龄',
    appearance TEXT COMMENT '外貌特征描述',
    personality TEXT COMMENT '性格特点',
    background TEXT COMMENT '背景故事',
    goals TEXT COMMENT '目标/动机',
    abilities TEXT COMMENT '能力/技能',
    notes TEXT COMMENT '备注',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    creator BIGINT COMMENT '创建人ID',
    updater BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_novel_id (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说人物表';

-- 人物关系表
CREATE TABLE IF NOT EXISTS agent_novel_character_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    character_id BIGINT NOT NULL COMMENT '人物ID(主体)',
    target_id BIGINT NOT NULL COMMENT '目标人物ID(客体)',
    relation_type VARCHAR(32) COMMENT '关系类型: FRIEND-朋友, ENEMY-敌人, LOVER-恋人, FAMILY-家人, MASTER-师徒, COLLEAGUE-同事, OTHER-其他',
    description VARCHAR(512) COMMENT '关系描述',
    creator BIGINT COMMENT '创建人ID',
    updater BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_novel_id (novel_id),
    INDEX idx_character_id (character_id),
    INDEX idx_target_id (target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人物关系表';

-- ========================================================
-- 3. AI Starter (hinovel-spring-boot-starter-ai)
-- ========================================================

-- LLM 提供方表
CREATE TABLE IF NOT EXISTS ai_llm_provider (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '提供方ID',
    name VARCHAR(128) NOT NULL COMMENT '名称',
    provider_type VARCHAR(32) NOT NULL COMMENT '类型: OPENAI/DEEPSEEK/QWEN/GEMINI/CUSTOM',
    base_url VARCHAR(512) COMMENT 'Base URL',
    api_key VARCHAR(512) COMMENT 'API Key',
    models TEXT COMMENT '支持的模型列表(JSON)',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用: 1-启用, 0-禁用',
    creator BIGINT COMMENT '创建人ID',
    updater BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_provider_type (provider_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM提供方表';

-- ========================================================
-- 初始化数据
-- ========================================================

-- 插入默认管理员账号（幂等，邮箱唯一约束保证不重复）
INSERT IGNORE INTO auth_user (username, email, password, role, status) VALUES
('admin', 'admin@hinovel.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'ADMIN', 'ACTIVE');

-- 插入默认 LLM 提供方配置（幂等，重复执行不会报错）
INSERT IGNORE INTO ai_llm_provider (name, provider_type, base_url, models, is_active) VALUES
('OpenAI', 'OPENAI', 'https://api.openai.com/v1', '["gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo"]', 0),
('DeepSeek', 'DEEPSEEK', 'https://api.deepseek.com/v1', '["deepseek-chat", "deepseek-reasoner"]', 0),
('通义千问', 'QWEN', 'https://dashscope.aliyuncs.com/api/v1', '["qwen-max", "qwen-plus", "qwen-turbo"]', 0),
('Gemini', 'GEMINI', 'https://generativelanguage.googleapis.com/v1beta', '["gemini-1.5-pro", "gemini-1.5-flash"]', 0);
