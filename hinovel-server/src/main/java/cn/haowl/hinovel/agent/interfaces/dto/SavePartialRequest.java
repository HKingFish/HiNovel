package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 旧客户端中断确认的请求参数
 */
@Data
public class SavePartialRequest {

    /** 会话 ID。 */
    private Long sessionId;

    /** 小说 ID。 */
    private Long novelId;

    /** 已生成的部分回复内容。 */
    private String content;
}
