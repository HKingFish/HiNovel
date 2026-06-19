-- 章节批注表（novel 模块）
CREATE TABLE IF NOT EXISTS novel_chapter_annotation
(
    id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT
    COMMENT
    '批注 ID',
    chapter_id
    BIGINT
    NOT
    NULL
    COMMENT
    '章节 ID',
    novel_id
    BIGINT
    NOT
    NULL
    COMMENT
    '小说 ID',
    user_id
    BIGINT
    NOT
    NULL
    COMMENT
    '批注人 ID',
    start_offset
    INT
    NOT
    NULL
    COMMENT
    '批注起始字符偏移量',
    end_offset
    INT
    NOT
    NULL
    COMMENT
    '批注结束字符偏移量',
    original_text
    TEXT
    NOT
    NULL
    COMMENT
    '被批注的原文内容',
    annotation_content
    TEXT
    NOT
    NULL
    COMMENT
    '批注内容',
    ai_rewrite_result
    TEXT
    COMMENT
    'AI 改写结果',
    annotation_type
    VARCHAR
(
    20
) NOT NULL DEFAULT 'SELF' COMMENT '批注类型：SELF=作者自批注，REVIEW=审阅者批注',
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING=待处理，ACCEPTED=已采纳，RESOLVED=已处理',
    viewed TINYINT NOT NULL DEFAULT 0 COMMENT '是否已查看：0-未查看，1-已查看',
    creator BIGINT COMMENT '创建人 ID',
    updater BIGINT COMMENT '更新人 ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_chapter_id
(
    chapter_id
),
    INDEX idx_novel_id
(
    novel_id
),
    INDEX idx_user_id
(
    user_id
),
    INDEX idx_status
(
    status
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_unicode_ci COMMENT='章节批注表';
