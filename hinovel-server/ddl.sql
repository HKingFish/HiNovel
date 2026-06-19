-- ========================================================
-- HiNovel 平台数据库初始化脚本
-- 生成时间: 2026-03-08
-- 数据库: MySQL 8.0+
-- 注意: 本脚本不建立外键约束，所有关联通过应用层保证
-- ========================================================

-- 创建数据库
CREATE
DATABASE IF NOT EXISTS hinovel DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE
hinovel;

-- ========================================================
-- 1. Member 模块 - 用户相关表
-- ========================================================

-- 用户表
CREATE TABLE IF NOT EXISTS member_user
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    username
    VARCHAR
(
    64
) NOT NULL COMMENT '用户名',
    email VARCHAR
(
    128
) COMMENT '邮箱',
    password VARCHAR
(
    128
) NOT NULL COMMENT 'BCrypt加密密码',
    avatar_url VARCHAR
(
    512
) COMMENT '头像URL',
    role VARCHAR
(
    32
) DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    status VARCHAR
(
    32
) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
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
    UNIQUE KEY uk_username
(
    username
),
    KEY idx_email
(
    email
),
    KEY idx_status
(
    status
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================================
-- 2. AI 模块 - LLM 提供方配置表
-- ========================================================

-- LLM 提供方配置表
CREATE TABLE IF NOT EXISTS ai_llm_provider
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    name
    VARCHAR
(
    64
) NOT NULL COMMENT '提供方名称',
    provider_type VARCHAR
(
    32
) NOT NULL COMMENT '提供方类型: OPENAI/DEEPSEEK等',
    base_url VARCHAR
(
    256
) COMMENT 'API基础URL',
    api_key VARCHAR
(
    512
) COMMENT 'API密钥',
    models TEXT COMMENT '支持的模型列表(JSON格式)',
    is_active TINYINT DEFAULT 1 COMMENT '是否激活: 0-否, 1-是',
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
    KEY idx_provider_type
(
    provider_type
),
    KEY idx_is_active
(
    is_active
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='LLM提供方配置表';

-- ========================================================
-- 3. Agent 模块 - Agent 配置与对话相关表
-- ========================================================

-- Agent 表
CREATE TABLE IF NOT EXISTS t_agent
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    user_id
    BIGINT
    COMMENT
    '所属用户ID',
    role_id
    BIGINT
    COMMENT
    '关联角色模板ID',
    name
    VARCHAR
(
    128
) NOT NULL COMMENT 'Agent名称',
    description VARCHAR
(
    512
) COMMENT 'Agent描述',
    system_prompt TEXT COMMENT '系统提示词',
    llm_provider_id BIGINT COMMENT 'LLM提供方ID',
    model_name VARCHAR
(
    64
) COMMENT '模型名称',
    custom_base_url VARCHAR
(
    500
) COMMENT '自定义模型API地址',
    temperature DECIMAL
(
    3,
    2
) DEFAULT 0.70 COMMENT '温度参数',
    max_tokens INT DEFAULT 2000 COMMENT '最大Token数',
    top_p DECIMAL
(
    3,
    2
) DEFAULT 1.00 COMMENT 'Top-P参数',
    max_iterations INT DEFAULT 10 COMMENT '最大执行步骤数',
    is_builtin TINYINT DEFAULT 0 COMMENT '是否内置: 0-用户自建, 1-内置',
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
    KEY idx_user_id
(
    user_id
),
    KEY idx_llm_provider_id
(
    llm_provider_id
),
    KEY idx_is_builtin
(
    is_builtin
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='Agent配置表';

-- 对话表
CREATE TABLE IF NOT EXISTS t_conversation
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    user_id
    BIGINT
    NOT
    NULL
    COMMENT
    '所属用户ID',
    agent_id
    BIGINT
    COMMENT
    '关联Agent ID',
    title
    VARCHAR
(
    256
) COMMENT '对话标题',
    message_count INT DEFAULT 0 COMMENT '消息总数',
    last_message VARCHAR
(
    512
) COMMENT '最后一条消息摘要',
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
    KEY idx_user_id
(
    user_id
),
    KEY idx_agent_id
(
    agent_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='对话表';

-- 消息表
CREATE TABLE IF NOT EXISTS t_message
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    conversation_id
    BIGINT
    NOT
    NULL
    COMMENT
    '所属对话ID',
    role
    VARCHAR
(
    32
) NOT NULL COMMENT '消息角色: USER/ASSISTANT/TOOL',
    content TEXT COMMENT '消息内容',
    step_type VARCHAR
(
    32
) COMMENT 'ReAct步骤类型: USER_INPUT/THOUGHT/ACTION/OBSERVATION/FINAL_ANSWER',
    tool_name VARCHAR
(
    64
) COMMENT '工具名称',
    tool_input TEXT COMMENT '工具调用入参(JSON)',
    tool_output TEXT COMMENT '工具调用结果',
    tokens_used INT DEFAULT 0 COMMENT '消耗的Token数',
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
    KEY idx_conversation_id
(
    conversation_id
),
    KEY idx_role
(
    role
),
    KEY idx_step_type
(
    step_type
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='消息表';

-- 小说创作片段表
CREATE TABLE IF NOT EXISTS t_novel_fragment
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    user_id
    BIGINT
    NOT
    NULL
    COMMENT
    '所属用户ID',
    title
    VARCHAR
(
    256
) COMMENT '片段标题',
    content TEXT COMMENT '片段内容',
    mode VARCHAR
(
    32
) COMMENT '创作模式: CONTINUE/REWRITE/DIALOGUE/EXPAND',
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
    KEY idx_user_id
(
    user_id
),
    KEY idx_mode
(
    mode
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='小说创作片段表';

-- 小说内容向量表
CREATE TABLE IF NOT EXISTS t_novel_content_vector
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
    '所属小说ID',
    chapter_id
    BIGINT
    COMMENT
    '章节ID',
    content_type
    VARCHAR
(
    32
) COMMENT '内容类型: CHAPTER/OUTLINE',
    content_text TEXT COMMENT '内容文本',
    vector_id VARCHAR
(
    128
) COMMENT '向量数据库中的ID',
    embedding_status VARCHAR
(
    32
) DEFAULT 'PENDING' COMMENT '向量化状态: PENDING/PROCESSING/COMPLETED/FAILED',
    fail_reason VARCHAR
(
    512
) COMMENT '失败原因',
    vector_dimension INT COMMENT '向量维度',
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
    KEY idx_chapter_id
(
    chapter_id
),
    KEY idx_content_type
(
    content_type
),
    KEY idx_embedding_status
(
    embedding_status
),
    KEY idx_vector_id
(
    vector_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='小说内容向量表';

-- ========================================================
-- 4. Agent 模块 - 小说创作相关表
-- ========================================================

-- 小说表
CREATE TABLE IF NOT EXISTS agent_novel
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    user_id
    BIGINT
    NOT
    NULL
    COMMENT
    '作者用户ID',
    title
    VARCHAR
(
    256
) NOT NULL COMMENT '小说标题',
    description TEXT COMMENT '小说简介',
    cover_url VARCHAR
(
    512
) COMMENT '封面图片URL',
    status VARCHAR
(
    32
) DEFAULT 'ONGOING' COMMENT '状态: ONGOING/COMPLETED/PAUSED',
    word_count BIGINT DEFAULT 0 COMMENT '总字数',
    chapter_count INT DEFAULT 0 COMMENT '总章节数',
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
    KEY idx_user_id
(
    user_id
),
    KEY idx_status
(
    status
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='小说表';

-- 小说大纲表
CREATE TABLE IF NOT EXISTS agent_novel_outline
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
    outline_content
    TEXT
    COMMENT
    '大纲内容',
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
    KEY idx_novel_id
(
    novel_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='小说大纲表';

-- 小说章节表
CREATE TABLE IF NOT EXISTS agent_novel_chapter
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
    chapter_number
    INT
    NOT
    NULL
    COMMENT
    '章节数(第几章)',
    title
    VARCHAR
(
    256
) COMMENT '章节标题',
    content LONGTEXT COMMENT '章节内容',
    word_count INT DEFAULT 0 COMMENT '字数',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    status VARCHAR
(
    32
) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED',
    need_republish TINYINT DEFAULT 0 COMMENT '发布后是否有改动，需要重新发布。0-无需重新发布，1-需要重新发布',
    vector_stored TINYINT DEFAULT 0 COMMENT '是否已完成向量数据库存储。0-未存储，1-已存储',
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
    KEY idx_chapter_number
(
    chapter_number
),
    KEY idx_status
(
    status
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='小说章节表';

-- 章节大纲表
CREATE TABLE IF NOT EXISTS agent_chapter_outline
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
    '所属小说ID',
    chapter_id
    BIGINT
    COMMENT
    '章节ID',
    outline_content
    TEXT
    COMMENT
    '大纲内容',
    plot_points
    TEXT
    COMMENT
    '剧情要点(JSON格式)',
    involved_characters
    TEXT
    COMMENT
    '涉及人物(JSON格式)',
    emotion_tone
    VARCHAR
(
    64
) COMMENT '情感基调',
    scene_setting VARCHAR
(
    256
) COMMENT '场景设置',
    ai_outline_content TEXT COMMENT 'AI生成的大纲内容（与用户大纲不冲突）',
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
    KEY idx_chapter_id
(
    chapter_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='章节大纲表';

-- 小说章节历史版本表
CREATE TABLE IF NOT EXISTS agent_novel_chapter_version
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    chapter_id
    BIGINT
    NOT
    NULL
    COMMENT
    '章节ID',
    content
    LONGTEXT
    COMMENT
    '版本内容',
    word_count
    INT
    DEFAULT
    0
    COMMENT
    '字数',
    change_diff
    TEXT
    COMMENT
    '与上一版本的diff',
    created_by
    BIGINT
    COMMENT
    '创建人ID',
    create_time
    DATETIME
    DEFAULT
    CURRENT_TIMESTAMP
    COMMENT
    '创建时间',
    deleted
    TINYINT
    DEFAULT
    0
    COMMENT
    '逻辑删除: 0-正常, 1-已删除',
    PRIMARY
    KEY
(
    id
),
    KEY idx_chapter_id
(
    chapter_id
),
    KEY idx_created_by
(
    created_by
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='小说章节历史版本表';

-- 小说人物表
CREATE TABLE IF NOT EXISTS agent_novel_character
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
    '所属小说ID',
    name
    VARCHAR
(
    128
) NOT NULL COMMENT '人物姓名',
    alias VARCHAR
(
    256
) COMMENT '别名/绰号',
    avatar_url VARCHAR
(
    512
) COMMENT '人物头像URL',
    gender VARCHAR
(
    16
) COMMENT '性别: MALE/FEMALE/OTHER',
    age INT COMMENT '年龄',
    appearance TEXT COMMENT '外貌特征描述',
    personality TEXT COMMENT '性格特点',
    background TEXT COMMENT '背景故事',
    goals TEXT COMMENT '目标/动机',
    abilities TEXT COMMENT '能力/技能',
    notes TEXT COMMENT '备注',
    role_type VARCHAR
(
    32
) COMMENT '角色类型: PROTAGONIST/SUPPORTING/ANTAGONIST/OTHER',
    color VARCHAR
(
    16
) COMMENT '代表色(如#ff6b6b)',
    identity VARCHAR
(
    128
) COMMENT '身份/职业',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
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
    KEY idx_name
(
    name
),
    KEY idx_role_type
(
    role_type
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='小说人物表';

-- 人物关系表
CREATE TABLE IF NOT EXISTS agent_novel_character_relation
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
    '所属小说ID',
    character_id
    BIGINT
    NOT
    NULL
    COMMENT
    '人物ID(主体)',
    target_id
    BIGINT
    NOT
    NULL
    COMMENT
    '目标人物ID(客体)',
    relation_type
    VARCHAR
(
    32
) COMMENT '关系类型: FRIEND/ENEMY/LOVER/FAMILY/MASTER/COLLEAGUE/OTHER',
    description VARCHAR
(
    512
) COMMENT '关系描述',
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
    KEY idx_character_id
(
    character_id
),
    KEY idx_target_id
(
    target_id
),
    KEY idx_relation_type
(
    relation_type
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='人物关系表';

-- 小说Agent配置表
CREATE TABLE IF NOT EXISTS agent_novel_agent_config
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
    author_agent_id
    VARCHAR
(
    64
) COMMENT '作者Agent ID',
    editor_agent_id VARCHAR
(
    64
) COMMENT '编辑Agent ID',
    qa_agent_id VARCHAR
(
    64
) COMMENT '问答Agent ID',
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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='小说Agent配置表';

-- ========================================================
-- 5. System 模块 - OSS上传日志表
-- ========================================================

-- OSS上传日志表
CREATE TABLE IF NOT EXISTS sys_oss_upload_log
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    user_id
    BIGINT
    COMMENT
    '上传用户ID',
    biz_type
    VARCHAR
(
    64
) COMMENT '业务类型: avatar/knowledge等',
    biz_id BIGINT COMMENT '关联业务ID',
    original_filename VARCHAR
(
    256
) COMMENT '原始文件名',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    content_type VARCHAR
(
    128
) COMMENT '文件MIME类型',
    file_url VARCHAR
(
    512
) COMMENT '文件访问URL',
    storage_type VARCHAR
(
    32
) COMMENT '存储类型: LOCAL/ALIYUN',
    status VARCHAR
(
    32
) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DELETED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY
(
    id
),
    KEY idx_user_id
(
    user_id
),
    KEY idx_biz_type
(
    biz_type
),
    KEY idx_biz_id
(
    biz_id
),
    KEY idx_status
(
    status
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='OSS上传日志表';

-- ========================================================
-- 6. AI 模块 - LLM 调用记录表
-- ========================================================

-- LLM 调用记录表
CREATE TABLE IF NOT EXISTS ai_llm_call_log
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '主键ID',
    call_scene
    VARCHAR
(
    32
) COMMENT '调用场景: REWRITE/AUDIT/REGENERATE',
    agent_id BIGINT COMMENT 'Agent ID',
    agent_name VARCHAR
(
    128
) COMMENT 'Agent 名称',
    llm_provider_id BIGINT COMMENT 'LLM 提供方 ID',
    llm_provider_name VARCHAR
(
    64
) COMMENT 'LLM 提供方名称',
    model_name VARCHAR
(
    64
) COMMENT '模型名称',
    request_content TEXT COMMENT '请求内容摘要',
    response_content TEXT COMMENT '响应内容摘要',
    prompt_tokens INT DEFAULT 0 COMMENT '输入 Token 数',
    completion_tokens INT DEFAULT 0 COMMENT '输出 Token 数',
    total_tokens INT DEFAULT 0 COMMENT '总 Token 数',
    processing_time_ms BIGINT DEFAULT 0 COMMENT '处理耗时(毫秒)',
    status VARCHAR
(
    16
) DEFAULT 'SUCCESS' COMMENT '调用状态: SUCCESS/FAILED',
    error_message VARCHAR
(
    1024
) COMMENT '错误信息',
    is_streaming TINYINT DEFAULT 0 COMMENT '是否流式调用: 0-否, 1-是',
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
    KEY idx_call_scene
(
    call_scene
),
    KEY idx_agent_id
(
    agent_id
),
    KEY idx_llm_provider_id
(
    llm_provider_id
),
    KEY idx_status
(
    status
),
    KEY idx_create_time
(
    create_time
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='LLM调用记录表';


-- ========================================================
-- 7. Agent 模块 - AI 问答相关表
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

-- ========================================================
-- 增量更新：agent_novel_settings 增加问答配置字段
-- ========================================================
ALTER TABLE agent_novel_settings
    ADD COLUMN qa_include_outline TINYINT DEFAULT 1 COMMENT 'AI问答时是否携带全文大纲: 0-否, 1-是';
ALTER TABLE agent_novel_settings
    ADD COLUMN qa_context_length INT DEFAULT 10 COMMENT 'AI问答时携带的历史消息条数（0表示不携带）';

-- ========================================================
-- 增量更新：agent_novel_agent_config 增加问答 Agent 字段
-- ========================================================
ALTER TABLE agent_novel_agent_config
    ADD COLUMN qa_agent_id VARCHAR(64) COMMENT '问答Agent ID';
