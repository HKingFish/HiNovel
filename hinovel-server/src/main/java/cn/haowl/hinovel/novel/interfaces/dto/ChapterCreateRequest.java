package cn.haowl.hinovel.novel.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建章节请求
 */
@Data
public class ChapterCreateRequest {

    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    @NotBlank(message = "章节标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    private String content;

    private Integer sortOrder;
}
