-- ========================================================
-- HiNovel 数据库表结构（与 Java 实体 @TableName 一致）
-- 适用：MySQL 8.0+，字符集 utf8mb4
-- ========================================================

CREATE DATABASE IF NOT EXISTS hinovel_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE hinovel_platform;

SET NAMES utf8mb4;

-- --------------------------------------------------------
-- 用户模块
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    id           BIGINT       NOT NULL COMMENT '主键 ID',
    username     VARCHAR(64)  NOT NULL COMMENT '用户名',
    email        VARCHAR(128) NOT NULL COMMENT '邮箱',
    password     VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
    avatar_url   VARCHAR(512)          DEFAULT NULL COMMENT '头像 URL',
    role         VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    status       VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    creator      BIGINT                DEFAULT NULL COMMENT '创建人 ID',
    updater      BIGINT                DEFAULT NULL COMMENT '更新人 ID',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    version      INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_email (email),
    KEY idx_username (username),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- --------------------------------------------------------
-- 小说模块
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS novel (
    id             BIGINT       NOT NULL COMMENT '小说 ID',
    user_id        BIGINT       NOT NULL COMMENT '作者用户 ID',
    title          VARCHAR(256) NOT NULL COMMENT '小说标题',
    description    TEXT                  DEFAULT NULL COMMENT '小说简介',
    cover_url      VARCHAR(512)          DEFAULT NULL COMMENT '封面 URL',
    status         VARCHAR(16)  NOT NULL DEFAULT 'ONGOING' COMMENT '状态：ONGOING/COMPLETED/PAUSED',
    word_count     BIGINT       NOT NULL DEFAULT 0 COMMENT '总字数',
    chapter_count  INT          NOT NULL DEFAULT 0 COMMENT '总章节数',
    creator        BIGINT                DEFAULT NULL COMMENT '创建人 ID',
    updater        BIGINT                DEFAULT NULL COMMENT '更新人 ID',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说表';

CREATE TABLE IF NOT EXISTS novel_outline (
    id               BIGINT   NOT NULL COMMENT '大纲 ID',
    novel_id         BIGINT   NOT NULL COMMENT '小说 ID',
    outline_content  LONGTEXT          DEFAULT NULL COMMENT '全文大纲内容',
    creator          BIGINT            DEFAULT NULL COMMENT '创建人 ID',
    updater          BIGINT            DEFAULT NULL COMMENT '更新人 ID',
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version          INT      NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_novel_id (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说全文大纲表';

CREATE TABLE IF NOT EXISTS novel_chapter (
    id              BIGINT       NOT NULL COMMENT '章节 ID',
    novel_id        BIGINT       NOT NULL COMMENT '小说 ID',
    chapter_number  INT          NOT NULL DEFAULT 0 COMMENT '章节序号',
    title           VARCHAR(256) NOT NULL COMMENT '章节标题',
    content         LONGTEXT              DEFAULT NULL COMMENT '章节内容',
    word_count      INT          NOT NULL DEFAULT 0 COMMENT '字数',
    sort_order      INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    status          VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED',
    need_republish  TINYINT      NOT NULL DEFAULT 0 COMMENT '发布后是否有改动需重新发布',
    vector_stored   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已完成向量入库',
    creator         BIGINT                DEFAULT NULL COMMENT '创建人 ID',
    updater         BIGINT                DEFAULT NULL COMMENT '更新人 ID',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version         INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_novel_id (novel_id),
    KEY idx_chapter_number (chapter_number),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说章节表';

CREATE TABLE IF NOT EXISTS novel_chapter_version (
    id           BIGINT   NOT NULL COMMENT '版本 ID',
    chapter_id   BIGINT   NOT NULL COMMENT '章节 ID',
    content      LONGTEXT          DEFAULT NULL COMMENT '版本内容',
    word_count   INT      NOT NULL DEFAULT 0 COMMENT '字数',
    change_diff  TEXT              DEFAULT NULL COMMENT '与上一版本的 diff',
    remark       VARCHAR(512)        DEFAULT NULL COMMENT '版本备注',
    published    TINYINT  NOT NULL DEFAULT 0 COMMENT '是否为已发布版本：0-否，1-是',
    content_md5  VARCHAR(32)         DEFAULT NULL COMMENT '内容 MD5',
    created_by   BIGINT            DEFAULT NULL COMMENT '创建人 ID',
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted      TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_chapter_id (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节版本历史表';

CREATE TABLE IF NOT EXISTS novel_chapter_outline (
    id                   BIGINT   NOT NULL COMMENT '章节大纲 ID',
    novel_id             BIGINT   NOT NULL COMMENT '小说 ID',
    chapter_id           BIGINT   NOT NULL COMMENT '章节 ID',
    outline_content      TEXT              DEFAULT NULL COMMENT '用户编写的大纲',
    plot_points          TEXT              DEFAULT NULL COMMENT '剧情要点 JSON',
    involved_characters  TEXT              DEFAULT NULL COMMENT '涉及人物 JSON',
    emotion_tone         VARCHAR(64)       DEFAULT NULL COMMENT '情感基调',
    scene_setting        VARCHAR(512)      DEFAULT NULL COMMENT '场景设置',
    ai_outline_content   TEXT              DEFAULT NULL COMMENT 'AI 生成的大纲',
    creator              BIGINT            DEFAULT NULL COMMENT '创建人 ID',
    updater              BIGINT            DEFAULT NULL COMMENT '更新人 ID',
    create_time          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted              TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version              INT      NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_novel_id (novel_id),
    KEY idx_chapter_id (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节大纲表';

CREATE TABLE IF NOT EXISTS novel_character (
    id           BIGINT       NOT NULL COMMENT '人物 ID',
    novel_id     BIGINT       NOT NULL COMMENT '小说 ID',
    name         VARCHAR(128) NOT NULL COMMENT '姓名',
    alias        VARCHAR(256)          DEFAULT NULL COMMENT '别名',
    avatar_url   VARCHAR(512)          DEFAULT NULL COMMENT '头像 URL',
    gender       VARCHAR(16)           DEFAULT NULL COMMENT '性别',
    age          INT                   DEFAULT NULL COMMENT '年龄',
    appearance   TEXT                  DEFAULT NULL COMMENT '外貌',
    personality  TEXT                  DEFAULT NULL COMMENT '性格',
    background   TEXT                  DEFAULT NULL COMMENT '背景',
    goals        TEXT                  DEFAULT NULL COMMENT '目标',
    abilities    TEXT                  DEFAULT NULL COMMENT '能力',
    notes        TEXT                  DEFAULT NULL COMMENT '备注',
    role_type    VARCHAR(20)           DEFAULT NULL COMMENT '角色类型',
    color        VARCHAR(7)            DEFAULT NULL COMMENT '代表色',
    identity     VARCHAR(100)          DEFAULT NULL COMMENT '身份/职业',
    sort_order   INT          NOT NULL DEFAULT 0 COMMENT '排序',
    creator      BIGINT                DEFAULT NULL COMMENT '创建人 ID',
    updater      BIGINT                DEFAULT NULL COMMENT '更新人 ID',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version      INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_novel_id (novel_id),
    KEY idx_role_type (role_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说人物表';

CREATE TABLE IF NOT EXISTS novel_character_relation (
    id             BIGINT       NOT NULL COMMENT '关系 ID',
    novel_id       BIGINT       NOT NULL COMMENT '小说 ID',
    character_id   BIGINT       NOT NULL COMMENT '主体人物 ID',
    target_id      BIGINT       NOT NULL COMMENT '客体人物 ID',
    relation_type  VARCHAR(32)           DEFAULT NULL COMMENT '关系类型',
    description    VARCHAR(512)          DEFAULT NULL COMMENT '关系描述',
    creator        BIGINT                DEFAULT NULL COMMENT '创建人 ID',
    updater        BIGINT                DEFAULT NULL COMMENT '更新人 ID',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_novel_id (novel_id),
    KEY idx_character_id (character_id),
    KEY idx_target_id (target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人物关系表';

CREATE TABLE IF NOT EXISTS novel_settings (
    id                         BIGINT  NOT NULL COMMENT '配置 ID',
    novel_id                   BIGINT  NOT NULL COMMENT '小说 ID，0 表示用户默认配置',
    user_id                    BIGINT           DEFAULT NULL COMMENT '用户 ID',
    auto_audit_after_rewrite   TINYINT NOT NULL DEFAULT 1 COMMENT '改写后自动审核',
    auto_outline_after_publish TINYINT NOT NULL DEFAULT 1 COMMENT '发布后自动生成大纲',
    auto_vector_after_publish  TINYINT NOT NULL DEFAULT 1 COMMENT '发布后自动向量入库',
    rewrite_context_chapters   INT     NOT NULL DEFAULT 2 COMMENT '改写携带前几章',
    rewrite_include_outline    TINYINT NOT NULL DEFAULT 1 COMMENT '改写是否携带全文大纲',
    audit_include_outline      TINYINT NOT NULL DEFAULT 1 COMMENT '审核是否携带全文大纲',
    qa_include_outline         TINYINT NOT NULL DEFAULT 1 COMMENT '问答是否携带全文大纲',
    qa_context_length          INT     NOT NULL DEFAULT 10 COMMENT '问答历史消息条数',
    auto_save_content          TINYINT NOT NULL DEFAULT 1 COMMENT '是否自动保存编辑器内容',
    creator                    BIGINT           DEFAULT NULL COMMENT '创建人 ID',
    updater                    BIGINT           DEFAULT NULL COMMENT '更新人 ID',
    create_time                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                    TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version                    INT      NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_novel_id (novel_id, deleted),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说 AI 配置表';

CREATE TABLE IF NOT EXISTS novel_agent_config (
    id               BIGINT NOT NULL COMMENT '配置 ID',
    novel_id         BIGINT NOT NULL COMMENT '小说 ID',
    author_agent_id  BIGINT          DEFAULT NULL COMMENT '作者 Agent ID',
    editor_agent_id  BIGINT          DEFAULT NULL COMMENT '编辑 Agent ID',
    qa_agent_id      BIGINT          DEFAULT NULL COMMENT '问答 Agent ID',
    creator          BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    updater          BIGINT          DEFAULT NULL COMMENT '更新人 ID',
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version          INT      NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_novel_id (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说 Agent 绑定配置';

CREATE TABLE IF NOT EXISTS novel_chapter_annotation (
    id                  BIGINT       NOT NULL COMMENT '批注 ID',
    chapter_id          BIGINT       NOT NULL COMMENT '章节 ID',
    novel_id            BIGINT       NOT NULL COMMENT '小说 ID',
    user_id             BIGINT       NOT NULL COMMENT '批注人 ID',
    start_offset        INT          NOT NULL COMMENT '起始偏移',
    end_offset          INT          NOT NULL COMMENT '结束偏移',
    original_text       TEXT         NOT NULL COMMENT '原文片段',
    annotation_content  TEXT         NOT NULL COMMENT '批注内容',
    ai_rewrite_result   TEXT                  DEFAULT NULL COMMENT 'AI 改写结果',
    annotation_type     VARCHAR(20)  NOT NULL DEFAULT 'SELF' COMMENT '批注类型',
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '批注状态',
    viewed              TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已查看',
    creator             BIGINT                DEFAULT NULL COMMENT '创建人 ID',
    updater             BIGINT                DEFAULT NULL COMMENT '更新人 ID',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version             INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_chapter_id (chapter_id),
    KEY idx_novel_id (novel_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节批注表';

CREATE TABLE IF NOT EXISTS novel_memo (
    id          BIGINT   NOT NULL COMMENT '手记 ID',
    novel_id    BIGINT   NOT NULL COMMENT '小说 ID',
    user_id     BIGINT   NOT NULL COMMENT '用户 ID',
    content     LONGTEXT          DEFAULT NULL COMMENT '手记内容 Markdown',
    creator     BIGINT            DEFAULT NULL COMMENT '创建人 ID',
    updater     BIGINT            DEFAULT NULL COMMENT '更新人 ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version     INT      NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_novel_id (novel_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说创作手记';

-- --------------------------------------------------------
-- Agent 模块
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS agent_config (
    id               BIGINT        NOT NULL COMMENT 'Agent ID',
    user_id          BIGINT        NOT NULL COMMENT '所属用户 ID，0 表示内置模板',
    role_id          BIGINT                 DEFAULT NULL COMMENT '角色模板 ID',
    name             VARCHAR(128)  NOT NULL COMMENT '名称',
    description      VARCHAR(512)           DEFAULT NULL COMMENT '描述',
    system_prompt    TEXT                   DEFAULT NULL COMMENT '系统提示词',
    llm_provider_id  BIGINT                 DEFAULT NULL COMMENT 'LLM 提供方 ID',
    model_name       VARCHAR(128)           DEFAULT NULL COMMENT '模型名称',
    custom_base_url  VARCHAR(512)           DEFAULT NULL COMMENT '自定义 API 地址',
    temperature      DECIMAL(4, 2) NOT NULL DEFAULT 0.70 COMMENT '温度',
    max_tokens       INT           NOT NULL DEFAULT 2000 COMMENT '最大 Token',
    top_p            DECIMAL(4, 2) NOT NULL DEFAULT 1.00 COMMENT 'Top-P',
    max_iterations   INT           NOT NULL DEFAULT 10 COMMENT '最大迭代次数',
    is_builtin       TINYINT       NOT NULL DEFAULT 0 COMMENT '是否内置：1-是，0-否',
    sort_order       INT           NOT NULL DEFAULT 0 COMMENT '排序',
    creator          BIGINT                 DEFAULT NULL COMMENT '创建人 ID',
    updater          BIGINT                 DEFAULT NULL COMMENT '更新人 ID',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version          INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_is_builtin (is_builtin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 配置表';

CREATE TABLE IF NOT EXISTS agent_chat_session (
    id          BIGINT       NOT NULL COMMENT '会话 ID',
    novel_id    BIGINT       NOT NULL COMMENT '小说 ID',
    title       VARCHAR(256) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
    favorite    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否收藏',
    creator     BIGINT                DEFAULT NULL COMMENT '创建人 ID',
    updater     BIGINT                DEFAULT NULL COMMENT '更新人 ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_novel_id (novel_id),
    KEY idx_creator (creator)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 问答会话表';

CREATE TABLE IF NOT EXISTS agent_chat_message (
    id          BIGINT      NOT NULL COMMENT '消息 ID',
    session_id  BIGINT      NOT NULL COMMENT '会话 ID',
    novel_id    BIGINT      NOT NULL COMMENT '小说 ID',
    role        VARCHAR(32) NOT NULL COMMENT '角色：user/assistant',
    content     TEXT                 DEFAULT NULL COMMENT '消息内容',
    revoked     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否已撤回',
    creator     BIGINT               DEFAULT NULL COMMENT '创建人 ID',
    updater     BIGINT               DEFAULT NULL COMMENT '更新人 ID',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version     INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_session_id (session_id),
    KEY idx_novel_id (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 问答消息表';

-- --------------------------------------------------------
-- AI 模块
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_llm_provider (
    id            BIGINT       NOT NULL COMMENT '提供方 ID',
    name          VARCHAR(128) NOT NULL COMMENT '名称',
    provider_type VARCHAR(32)  NOT NULL COMMENT '类型',
    base_url      VARCHAR(512)          DEFAULT NULL COMMENT 'Base URL',
    api_key       VARCHAR(512)          DEFAULT NULL COMMENT 'API Key（加密存储）',
    models        TEXT                  DEFAULT NULL COMMENT '模型列表（逗号分隔）',
    user_id       BIGINT                DEFAULT NULL COMMENT '所属用户 ID',
    is_active     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用',
    creator       BIGINT                DEFAULT NULL COMMENT '创建人 ID',
    updater       BIGINT                DEFAULT NULL COMMENT '更新人 ID',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version       INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_provider_type (provider_type),
    KEY idx_user_id (user_id),
    KEY idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM 提供方表';

CREATE TABLE IF NOT EXISTS ai_llm_call_log (
    id                  BIGINT       NOT NULL COMMENT '日志 ID',
    call_scene          VARCHAR(64)           DEFAULT NULL COMMENT '调用场景',
    agent_id            BIGINT                DEFAULT NULL COMMENT 'Agent ID',
    agent_name          VARCHAR(128)          DEFAULT NULL COMMENT 'Agent 名称',
    llm_provider_id     BIGINT                DEFAULT NULL COMMENT '提供方 ID',
    llm_provider_name   VARCHAR(128)          DEFAULT NULL COMMENT '提供方名称',
    model_name          VARCHAR(128)          DEFAULT NULL COMMENT '模型名称',
    request_content     TEXT                  DEFAULT NULL COMMENT '请求摘要',
    response_content    TEXT                  DEFAULT NULL COMMENT '响应摘要',
    prompt_tokens       INT                   DEFAULT NULL COMMENT '输入 Token',
    completion_tokens   INT                   DEFAULT NULL COMMENT '输出 Token',
    total_tokens        INT                   DEFAULT NULL COMMENT '总 Token',
    processing_time_ms  BIGINT                DEFAULT NULL COMMENT '耗时毫秒',
    user_id             BIGINT                DEFAULT NULL COMMENT '所属用户 ID',
    status              VARCHAR(16)  NOT NULL DEFAULT 'SUCCESS' COMMENT '状态',
    error_message       TEXT                  DEFAULT NULL COMMENT '错误信息',
    is_streaming        TINYINT               DEFAULT 0 COMMENT '是否流式',
    creator             BIGINT                DEFAULT NULL COMMENT '创建人 ID',
    updater             BIGINT                DEFAULT NULL COMMENT '更新人 ID',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version             INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_agent_id (agent_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM 调用日志表';

CREATE TABLE IF NOT EXISTS ai_embedding_config (
    id          BIGINT       NOT NULL COMMENT '配置 ID',
    user_id     BIGINT       NOT NULL COMMENT '所属用户 ID',
    name        VARCHAR(64)  NOT NULL COMMENT '配置名称',
    model_type  VARCHAR(32)  NOT NULL DEFAULT 'openai' COMMENT '模型类型：openai/onnx',
    base_url    VARCHAR(256)          DEFAULT NULL COMMENT 'API 地址',
    api_key     VARCHAR(512)          DEFAULT NULL COMMENT 'API Key（加密存储）',
    model_name  VARCHAR(128)          DEFAULT NULL COMMENT '模型名称',
    dimensions  INT          NOT NULL DEFAULT 1024 COMMENT '向量维度',
    is_active   TINYINT      NOT NULL DEFAULT 1 COMMENT '是否激活',
    creator     BIGINT                DEFAULT NULL COMMENT '创建人 ID',
    updater     BIGINT                DEFAULT NULL COMMENT '更新人 ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Embedding 模型配置表';

-- --------------------------------------------------------
-- 基础设施模块
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS infra_oss_upload_log (
    id                 BIGINT       NOT NULL COMMENT '日志 ID',
    user_id            BIGINT                DEFAULT NULL COMMENT '上传用户 ID',
    biz_type           VARCHAR(64)           DEFAULT NULL COMMENT '业务类型',
    biz_id             BIGINT                DEFAULT NULL COMMENT '业务 ID',
    original_filename  VARCHAR(512)          DEFAULT NULL COMMENT '原始文件名',
    file_size          BIGINT                DEFAULT NULL COMMENT '文件大小字节',
    content_type       VARCHAR(128)          DEFAULT NULL COMMENT 'MIME 类型',
    file_url           VARCHAR(1024)         DEFAULT NULL COMMENT '访问 URL',
    storage_type       VARCHAR(16)           DEFAULT NULL COMMENT '存储类型',
    status             VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OSS 上传日志表';
