package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : AI 改写与重新生成的响应结果
 */
@Data
public class AIAuthorResponse {

    /** 结果 ID。 */
    private String id;

    /** 生成的章节内容。 */
    private String content;

    /** 改进建议列表。 */
    private List<String> suggestions;

    /** 内容字数。 */
    private int wordCount;

    /** 处理耗时（毫秒）。 */
    private long processingTime;
}
