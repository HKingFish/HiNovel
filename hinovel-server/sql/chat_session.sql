-- ========================================================
-- AI 问答会话表 DDL
-- 生成时间: 2026-03-14
-- 说明: 管理 AI 问答的会话和消息记录
-- ========================================================

-- AI 问答会话表
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
    200
) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
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
),
    KEY idx_creator
(
    creator
)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 问答会话表';

-- AI 问答消息表
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
    20
) NOT NULL COMMENT '消息角色: user-用户, assistant-AI助手',
    content TEXT NOT NULL COMMENT '消息内容',
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
    COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 问答消息表';

-- 在 agent_novel_settings 表中增加问答相关配置字段
ALTER TABLE agent_novel_settings
    ADD COLUMN qa_include_outline TINYINT DEFAULT 1 COMMENT 'AI 问答时是否携带全文大纲: 0-否, 1-是' AFTER audit_include_outline,
    ADD COLUMN qa_context_length  INT     DEFAULT 10 COMMENT 'AI 问答时携带的历史消息条数（0表示不携带）' AFTER qa_include_outline;

-- 在 agent_novel_agent_config 表中增加问答 Agent 字段
ALTER TABLE agent_novel_agent_config
    ADD COLUMN qa_agent_id BIGINT COMMENT '问答Agent ID' AFTER editor_agent_id;
