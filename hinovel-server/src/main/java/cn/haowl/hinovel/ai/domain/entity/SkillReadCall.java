package cn.haowl.hinovel.ai.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : haowl
 * @date : 2026/11/12 15:44
 * @desc : 调用日志中的一次 Skill 读取
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillReadCall {

    /** 从 1 起的调用发起顺序。 */
    private int sequence;

    /** 请求读取的 Skill 名称。 */
    private String skillName;

    /** 读取结果。 */
    private Status status;

    /** 读取失败时的错误码。 */
    private String errorCode;

    /** Skill 读取状态。 */
    public enum Status {
        SUCCESS, FAILED
    }
}
