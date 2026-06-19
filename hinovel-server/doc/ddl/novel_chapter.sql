-- 小说章节表
CREATE TABLE IF NOT EXISTS `novel_chapter` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `novel_id` BIGINT NOT NULL COMMENT '小说ID',
    `number` INT NOT NULL COMMENT '章节序号',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '章节标题',
    `content` LONGTEXT DEFAULT NULL COMMENT '章节内容',
    `word_count` INT DEFAULT 0 COMMENT '字数',
    `is_published` TINYINT(1) DEFAULT 0 COMMENT '是否已发布 0-未发布 1-已发布',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_novel_id` (`novel_id`),
    KEY `idx_number` (`number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说章节表';

-- 小说章节历史版本表
CREATE TABLE IF NOT EXISTS `novel_chapter_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `chapter_id` BIGINT NOT NULL COMMENT '章节ID',
    `content` LONGTEXT DEFAULT NULL COMMENT '版本内容',
    `word_count` INT DEFAULT 0 COMMENT '字数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_chapter_id` (`chapter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说章节历史版本表';

-- 小说大纲表
CREATE TABLE IF NOT EXISTS `novel_outline` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `novel_id` BIGINT NOT NULL COMMENT '小说ID',
    `outline_content` LONGTEXT DEFAULT NULL COMMENT '大纲内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_novel_id` (`novel_id`),
    KEY `idx_novel_id` (`novel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说大纲表';
