-- ========================================================
-- 小说配置表 DDL
-- 生成时间: 2026-03-14
-- 说明: 统一管理小说级别和用户级别的 AI 功能配置
-- ========================================================

-- 小说配置表（小说级别配置，优先级高于用户默认配置）
CREATE TABLE IF NOT EXISTS agent_novel_settings
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    novel_id
    BIGINT
    NOT
    NULL
    COMMENT
    '小说ID（0 表示用户级别默认配置）',
    user_id
    BIGINT
    COMMENT
    '用户ID（用户级别默认配置时必填）',
    auto_audit_after_rewrite
    TINYINT
    DEFAULT
    1
    COMMENT
    '改写后自动审核: 0-否, 1-是',
    auto_outline_after_publish
    TINYINT
    DEFAULT
    1
    COMMENT
    '发布后自动生成AI大纲: 0-否, 1-是',
    auto_vector_after_publish
    TINYINT
    DEFAULT
    1
    COMMENT
    '发布后自动存储向量: 0-否, 1-是',
    rewrite_context_chapters
    INT
    DEFAULT
    2
    COMMENT
    'AI改写时携带前几章内容作为前情提要（0表示不携带）',
    rewrite_include_outline
    TINYINT
    DEFAULT
    1
    COMMENT
    'AI改写时是否携带全文大纲: 0-否, 1-是',
    audit_include_outline
    TINYINT
    DEFAULT
    1
    COMMENT
    'AI审核时是否携带全文大纲: 0-否, 1-是',
    creator
    BIGINT
    COMMENT
    '创建人ID',
    updater
    BIGINT
    COMMENT
    '更新人ID',
    create_time
    DATETIME
    DEFAULT
    CURRENT_TIMESTAMP
    COMMENT
    '创建时间',
    update_time
    DATETIME
    DEFAULT
    CURRENT_TIMESTAMP
    ON
    UPDATE
    CURRENT_TIMESTAMP
    COMMENT
    '更新时间',
    deleted
    TINYINT
    DEFAULT
    0
    COMMENT
    '逻辑删除: 0-正常, 1-已删除',
    version
    INT
    DEFAULT
    0
    COMMENT
    '乐观锁版本号',
    PRIMARY
    KEY
(
    id
),
    UNIQUE KEY uk_novel_id
(
    novel_id,
    deleted
),
    KEY idx_user_id
(
    user_id
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = '小说配置表';

-- 新增问答配置字段
ALTER TABLE agent_novel_settings
    ADD COLUMN qa_include_outline TINYINT DEFAULT 1 COMMENT 'AI问答时是否携带全文大纲: 0-否, 1-是';
ALTER TABLE agent_novel_settings
    ADD COLUMN qa_context_length INT DEFAULT 10 COMMENT 'AI问答时携带的历史消息条数（0表示不携带）';

-- 删除 agent_chapter_outline 表中的 auto_audit 字段（配置已迁移到 agent_novel_settings）
-- ALTER TABLE agent_chapter_outline DROP COLUMN auto_audit;

-- ========================================================
-- AI 问答会话表
-- ========================================================

CREATE TABLE IF NOT EXISTS agent_chat_session
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    novel_id
    BIGINT
    NOT
    NULL
    COMMENT
    '小说ID',
    title
    VARCHAR
(
    256
) COMMENT '会话标题',
    creator BIGINT COMMENT '创建人ID',
    updater BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY
(
    id
),
    KEY idx_novel_id
(
    novel_id
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'AI问答会话表';

-- ========================================================
-- AI 问答消息表
-- ========================================================

CREATE TABLE IF NOT EXISTS agent_chat_message
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    session_id
    BIGINT
    NOT
    NULL
    COMMENT
    '会话ID',
    novel_id
    BIGINT
    NOT
    NULL
    COMMENT
    '小说ID',
    role
    VARCHAR
(
    32
) NOT NULL COMMENT '消息角色: user/assistant',
    content TEXT COMMENT '消息内容',
    creator BIGINT COMMENT '创建人ID',
    updater BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY
(
    id
),
    KEY idx_session_id
(
    session_id
),
    KEY idx_novel_id
(
    novel_id
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'AI问答消息表';
