-- 章节表增加发布改动标记和向量存储标记字段
ALTER TABLE agent_novel_chapter
    ADD COLUMN need_republish TINYINT DEFAULT 0 COMMENT '发布后是否有改动，需要重新发布。0-无需重新发布，1-需要重新发布'
        AFTER status,
    ADD COLUMN vector_stored TINYINT DEFAULT 0 COMMENT '是否已完成向量数据库存储。0-未存储，1-已存储'
        AFTER need_republish;
