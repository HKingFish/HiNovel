package cn.haowl.hinovel.agent.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : haowl
 * @date : 2026/11/17 19:23
 * @desc : 章节审核问题严重程度
 */
@Getter
@AllArgsConstructor
public enum AuditSeverityEnum {
    /**
     * 低严重程度。
     */
    LOW("low", "低"),
    /**
     * 中等严重程度。
     */
    MEDIUM("medium", "中"),
    /**
     * 高严重程度。
     */
    HIGH("high", "高");

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
