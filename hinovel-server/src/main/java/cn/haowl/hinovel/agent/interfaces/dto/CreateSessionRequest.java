package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 创建问答会话的请求参数
 */
@Data
public class CreateSessionRequest {

    /** 小说 ID。 */
    private Long novelId;

    /** 会话标题（可选）。 */
    private String title;
}
