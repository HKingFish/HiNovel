-- 首版 Agent Skill schema 升级。新环境使用 schema.sql 和 seed.sql。
-- 发布前备份，统计 agent_config、agent_chat_message、ai_llm_call_log 行数并确认 ALTER 执行窗口。
-- 未传审核名单时，只升级 schema 并列出候选。名单与执行方式见文件末尾。

USE hinovel_platform;

DROP PROCEDURE IF EXISTS hinovel_upgrade_agent_skill_schema;
DELIMITER //
CREATE PROCEDURE hinovel_upgrade_agent_skill_schema()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- 旧版使用独立 ID 和数据库 Skill。发布前核对旧数据及资源 ID 映射。
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'agent_skill_binding' AND column_name = 'id'
    ) OR EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name IN ('agent_skill', 'agent_skill_file')
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Legacy Skill schema detected. Review ID mapping and back up legacy tables before upgrade.';
    END IF;

    CREATE TABLE IF NOT EXISTS agent_skill_binding (
        agent_id BIGINT NOT NULL,
        skill_id BIGINT NOT NULL,
        PRIMARY KEY (agent_id, skill_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'agent_chat_message' AND column_name = 'loaded_skill_names'
    ) THEN
        ALTER TABLE agent_chat_message ADD COLUMN loaded_skill_names JSON NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'ai_llm_call_log' AND column_name = 'skill_read_calls'
    ) THEN
        ALTER TABLE ai_llm_call_log ADD COLUMN skill_read_calls JSON NULL;
    END IF;

    IF @agent_skill_backfill_ids IS NOT NULL THEN
        IF JSON_TYPE(@agent_skill_backfill_ids) <> 'ARRAY' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Backfill Agent IDs must be a JSON array.';
        END IF;
        START TRANSACTION;
        IF EXISTS (
            SELECT 1
            FROM JSON_TABLE(@agent_skill_backfill_ids, '$[*]'
                COLUMNS (agent_id BIGINT PATH '$' ERROR ON EMPTY ERROR ON ERROR)) AS approved
            LEFT JOIN agent_config AS a ON a.id = approved.agent_id AND a.deleted = 0
            WHERE a.id IS NULL
        ) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The approved list includes an unknown or deleted Agent.';
        END IF;
        INSERT INTO agent_skill_binding (agent_id, skill_id)
        SELECT DISTINCT approved.agent_id, 3
        FROM JSON_TABLE(@agent_skill_backfill_ids, '$[*]'
            COLUMNS (agent_id BIGINT PATH '$' ERROR ON EMPTY ERROR ON ERROR)) AS approved
        ON DUPLICATE KEY UPDATE skill_id = 3;
        COMMIT;
    END IF;
END//
DELIMITER ;

CALL hinovel_upgrade_agent_skill_schema();
DROP PROCEDURE hinovel_upgrade_agent_skill_schema;

-- 审核存量补挂候选。核对模板、作者入口和实际承担写作或润色的配置。
-- 编辑 Agent 的纯审核、总结入口和问答入口的普通问答无需默认挂载。
SELECT a.id, a.name, a.role_id
FROM agent_config AS a
WHERE a.deleted = 0
  AND (
    (a.id = 101 AND a.is_builtin = 1)
    OR a.role_id = 101
    OR EXISTS (
        SELECT 1 FROM novel_agent_config AS cfg
        WHERE cfg.author_agent_id = a.id AND cfg.deleted = 0
    )
  )
ORDER BY a.id;

-- 主责人审核候选和其他实际负责写作或润色的配置。
-- 核对发布登记，在仍未开放 Agent 配置写入时，于同一个 mysql 会话执行：
-- SET @agent_skill_backfill_ids = JSON_ARRAY(101, 12345); -- 替换为核定的实际 Agent ID
-- SOURCE upgrade-20261110-agent-skill.sql
-- SET @agent_skill_backfill_ids = NULL;
-- 记录名单和执行结果后恢复写入。发布重试先核对登记，已登记时保持名单变量为空。
