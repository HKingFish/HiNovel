-- ========================================================
-- HiNovel 人物图谱模块数据库变更脚本
-- 添加 role_type, color, identity 字段
-- ========================================================

-- 修改 agent_novel_character 表，添加新字段
ALTER TABLE agent_novel_character
    ADD COLUMN role_type VARCHAR(20) NULL COMMENT '角色类型：PROTAGONIST-主角，SUPPORTING-配角，ANTAGONIST-反派，OTHER-其他',
    ADD COLUMN color VARCHAR(7) NULL COMMENT '代表色，用于图谱显示，如 #ff6b6b',
    ADD COLUMN identity VARCHAR(100) NULL COMMENT '身份/职业，如"学生"、"医生"';

-- 添加索引优化查询
CREATE INDEX idx_character_novel_id ON agent_novel_character(novel_id);
CREATE INDEX idx_character_role_type ON agent_novel_character(role_type);

-- 更新现有数据（可选：给已有角色设置默认值）
-- UPDATE agent_novel_character SET role_type = 'OTHER', color = '#6366f1' WHERE role_type IS NULL;
