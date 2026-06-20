-- ========================================================
-- HiNovel 初始数据
-- 默认管理员：admin / admin@hinovel.com / Admin@123456
-- 生产环境部署后请立即修改密码
-- ========================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE hinovel_platform;

-- 默认管理员（ID 固定为 1，便于关联初始数据）
INSERT INTO `user` (id, username, email, password, role, status, deleted, version)
VALUES (
    1,
    'admin',
    'admin@hinovel.com',
    '$2a$10$WpZ56OswYJxgmh/hS/aHsOom/ZlvlBQY3iiT3TCA5MtovG/5SVQIa',
    'ADMIN',
    'ACTIVE',
    0,
    0
) ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    password = VALUES(password),
    role = VALUES(role),
    status = VALUES(status);

-- 内置 Agent 模板（user_id=0，供用户创建小说时选用）
INSERT INTO agent_config (
    id, user_id, name, description, system_prompt,
    temperature, max_tokens, top_p, max_iterations, is_builtin, sort_order, deleted, version
) VALUES
(
    101, 0, '小说创作助手', '擅长根据大纲和设定进行小说章节创作',
    '你是一位专业的小说创作助手。你擅长根据大纲、人物设定和剧情要点进行章节创作。\n\n创作要求：\n1. 文笔流畅自然，善于描写细节、场景和人物对话\n2. 严格遵循大纲和人物设定，保持剧情一致性\n3. 注重人物性格刻画，对话符合角色特征\n4. 合理把控节奏，张弛有度\n5. 使用中文创作，语言优美',
    0.80, 4000, 1.00, 10, 1, 1, 0, 0
),
(
    102, 0, '智能问答助手', '基于小说内容进行智能问答，帮助作者回顾剧情',
    '你是一位智能问答助手，专门帮助小说作者回顾和分析小说内容。\n\n工作要求：\n1. 基于小说已有内容准确回答问题\n2. 帮助梳理人物关系和剧情脉络\n3. 指出可能的剧情漏洞或不一致之处\n4. 提供创作建议和灵感\n5. 回答简洁明了，必要时引用原文',
    0.50, 2000, 1.00, 10, 1, 2, 0, 0
),
(
    103, 0, '内容审核编辑', '对小说章节进行多维度审核，检查一致性与文笔',
    '你是一位资深的小说编辑，负责对章节内容进行专业审核。\n\n审核维度：\n1. 一致性检查：人物设定、时间线、场景描述是否前后一致\n2. 逻辑性检查：剧情发展是否合理，因果关系是否成立\n3. 人物检查：对话和行为是否符合角色性格\n4. 文笔检查：语句是否通顺，用词是否恰当\n5. 给出具体的修改建议和改进方向',
    0.30, 2000, 1.00, 10, 1, 3, 0, 0
),
(
    104, 0, '大纲规划师', '帮助构思和完善小说大纲，规划剧情走向',
    '你是一位经验丰富的大纲规划师，帮助作者构思和完善小说大纲。\n\n工作内容：\n1. 根据作者的创意构思完整的故事大纲\n2. 规划主线和支线剧情的走向\n3. 设计人物成长弧线和关键转折点\n4. 确保剧情节奏合理，高潮迭起\n5. 提供多种剧情发展方案供选择',
    0.90, 4000, 1.00, 10, 1, 4, 0, 0
),
(
    105, 0, '通用 AI 助手', '通用型 AI 助手，可自由定制系统提示词',
    '你是一个通用的 AI 助手，请根据用户的需求提供帮助。',
    0.70, 2000, 1.00, 10, 1, 5, 0, 0
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    system_prompt = VALUES(system_prompt),
    sort_order = VALUES(sort_order);

-- LLM 提供方参考模板（默认禁用，用户自行配置 API Key 后启用）
INSERT INTO ai_llm_provider (id, name, provider_type, base_url, models, is_active, deleted, version)
VALUES
(201, 'OpenAI', 'OPENAI', 'https://api.openai.com/v1', 'gpt-4o,gpt-4o-mini,gpt-3.5-turbo', 0, 0, 0),
(202, 'DeepSeek', 'DEEPSEEK', 'https://api.deepseek.com/v1', 'deepseek-chat,deepseek-reasoner', 0, 0, 0),
(203, '通义千问', 'QWEN', 'https://dashscope.aliyuncs.com/api/v1', 'qwen-max,qwen-plus,qwen-turbo', 0, 0, 0),
(204, 'Gemini', 'GEMINI', 'https://generativelanguage.googleapis.com/v1beta', 'gemini-1.5-pro,gemini-1.5-flash', 0, 0, 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    base_url = VALUES(base_url),
    models = VALUES(models);
