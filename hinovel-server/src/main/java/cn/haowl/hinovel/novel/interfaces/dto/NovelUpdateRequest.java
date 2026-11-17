package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.constant.NovelStatusEnum;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新小说请求
 */
@Data
public class NovelUpdateRequest {

    /**
     * 标题。
     */
    @Size(max = CommonConstants.LENGTH_200, message = "标题长度不能超过200字符")
    private String title;

    /**
     * 小说简介。
     */
    @Size(max = CommonConstants.LENGTH_2000, message = "简介长度不能超过2000字符")
    private String description;

    /**
     * 小说封面 URL。
     */
    private String coverUrl;

    /**
     * 状态编码。
     *
     * @see NovelStatusEnum
     */
    private String status;
}
