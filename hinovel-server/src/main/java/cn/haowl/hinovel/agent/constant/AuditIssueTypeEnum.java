package cn.haowl.hinovel.agent.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : haowl
 * @date : 2026/11/17 19:23
 * @desc : 章节审核问题类型
 */
@Getter
@AllArgsConstructor
public enum AuditIssueTypeEnum {
    /**
     * 前后设定不一致。
     */
    CONSISTENCY("consistency", "一致性"),
    /**
     * 情节逻辑问题。
     */
    LOGIC("logic", "逻辑性"),
    /**
     * 人物塑造问题。
     */
    CHARACTER("character", "人物"),
    /**
     * 情节发展问题。
     */
    PLOT("plot", "剧情");

    /**
     * 模型和接口使用的编码。
     */
    @Getter(onMethod_ = @JsonValue)
    private final String code;

    /**
     * 取值说明。
     */
    private final String description;
}
