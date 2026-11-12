package cn.haowl.hinovel.agent.enums;

import cn.haowl.hinovel.common.exception.ErrorCode;

import static cn.haowl.hinovel.common.exception.ServiceErrorCodeRange.AGENT_SKILL_ERROR_CODE_MIN;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : Skill API 错误码
 */
public interface SkillErrorCodeConstants {

    /** ID 不在内置目录中。 */
    ErrorCode SKILL_NOT_EXISTS = new ErrorCode(AGENT_SKILL_ERROR_CODE_MIN + 1, "内置 Skill 不存在");
}
