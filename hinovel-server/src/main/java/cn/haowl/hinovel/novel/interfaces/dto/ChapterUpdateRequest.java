package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.novel.constant.NovelChapterStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新章节请求
 */
@Data
public class ChapterUpdateRequest {

    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    private String content;

    private Integer sortOrder;

    private NovelChapterStatus status;

    /**
     * 是否创建版本记录（自动保存时传false，手动保存传true）
     */
    private Boolean createVersion;
}
