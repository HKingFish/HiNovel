package cn.haowl.hinovel.agent.interfaces.dto;

import lombok.Data;

/**
 * @author : haowl
 * @date : 2026/11/12 18:39
 * @desc : 重命名问答会话的请求参数
 */
@Data
public class RenameSessionRequest {

    /** 新标题。 */
    private String title;
}
