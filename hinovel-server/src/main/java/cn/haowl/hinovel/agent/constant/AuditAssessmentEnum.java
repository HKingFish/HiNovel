package cn.haowl.hinovel.agent.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : haowl
 * @date : 2026/11/17 19:23
 * @desc : 章节审核总体结论
 */
@Getter
@AllArgsConstructor
public enum AuditAssessmentEnum {
    /**
     * 审核通过。
     */
    PASS("pass", "通过"),
    /**
     * 有问题需要关注。
     */
    WARNING("warning", "有警告"),
    /**
     * 审核未通过。
     */
    FAIL("fail", "未通过");

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
