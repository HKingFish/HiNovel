package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

/**
 * AI 改写结果请求。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 承载保存到批注的 AI 改写结果
 */
@Data
public class AiRewriteResultRequest {

    /**
     * AI 改写结果文本
     */
    private String rewriteResult;
}
