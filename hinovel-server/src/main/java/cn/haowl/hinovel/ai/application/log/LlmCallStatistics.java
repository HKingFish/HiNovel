package cn.haowl.hinovel.ai.application.log;

import lombok.Value;

/**
 * @author : haowl
 * @date : 2026/11/17 21:04
 * @desc : 指定查询范围内的 LLM 调用统计结果
 */
@Value
public class LlmCallStatistics {

    /**
     * 成功调用次数。
     */
    long successCount;

    /**
     * 失败调用次数。
     */
    long failCount;

    /**
     * 输入与输出的 Token 总数。
     */
    long totalTokens;

    /**
     * 总处理耗时（毫秒）。
     */
    long totalDuration;
}
