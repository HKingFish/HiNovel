package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.domain.command.CreateChapterCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建章节请求
 */
@Data
public class ChapterCreateRequest {

    /**
     * 小说 ID。
     */
    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    /**
     * 标题。
     */
    @NotBlank(message = "章节标题不能为空")
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
     * 转换为章节创建参数。
     *
     * @return 章节创建命令
     */
    public CreateChapterCommand toCommand() {
        return CreateChapterCommand.builder()
            .novelId(novelId)
            .title(title)
            .content(content)
            .sortOrder(sortOrder)
            .build();
    }
}
