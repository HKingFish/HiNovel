package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

/**
 * 更新批注请求。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 承载更新后的批注内容
 */
@Data
public class UpdateAnnotationRequest {

    /**
     * 批注内容
     */
    private String annotationContent;
}
