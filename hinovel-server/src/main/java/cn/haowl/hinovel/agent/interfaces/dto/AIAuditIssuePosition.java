package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 审核问题在正文中的位置
 */
@Data
public class AIAuditIssuePosition {

    /**
     * 起始偏移。
     */
    private int start;

    /**
     * 结束偏移。
     */
    private int end;
}
