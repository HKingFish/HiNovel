package cn.haowl.hinovel.novel.interfaces.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新小说请求
 */
@Data
public class NovelUpdateRequest {

    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    @Size(max = 2000, message = "简介长度不能超过2000字符")
    private String description;

    private String coverUrl;

    private String status;
}
