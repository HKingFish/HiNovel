package cn.haowl.hinovel.agent.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : AI 流式问答的请求参数
 */
@Data
public class AiChatRequest {

    /**
     * 小说 ID。
     */
    @NotNull
    private Long novelId;

    /**
     * 会话 ID。
     */
    @NotNull
    private Long sessionId;

    /**
     * 用户问题。
     */
    @NotBlank
    private String question;
}
