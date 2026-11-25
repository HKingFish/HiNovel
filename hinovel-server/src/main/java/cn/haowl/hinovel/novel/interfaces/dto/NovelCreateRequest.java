package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.common.constant.CommonConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建小说请求
 */
@Data
public class NovelCreateRequest {

    /**
     * 标题。
     */
    @NotBlank(message = "小说标题不能为空")
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
}
