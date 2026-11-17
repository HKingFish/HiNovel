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
    CONSISTENCY("consistency", "一致性"),
    LOGIC("logic", "逻辑性"),
    CHARACTER("character", "人物"),
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
