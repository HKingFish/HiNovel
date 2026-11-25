package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.constant.NovelChapterStatusEnum;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新章节请求
 */
@Data
public class ChapterUpdateRequest {

    /**
     * 标题。
     */
    @Size(max = CommonConstants.LENGTH_200, message = "标题长度不能超过200字符")
    private String title;

    /**
     * 正文。
     */
    private String content;

    /**
     * 排序号。
     */
    private Integer sortOrder;

    /**
     * 章节发布状态。
     *
     * @see NovelChapterStatusEnum
     */
    private NovelChapterStatusEnum status;

    /**
     * 是否创建版本记录（自动保存时传false，手动保存传true）
     */
    private Boolean createVersion;
}
