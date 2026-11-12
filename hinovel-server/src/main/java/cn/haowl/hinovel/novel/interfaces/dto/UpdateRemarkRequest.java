package cn.haowl.hinovel.novel.interfaces.dto;

import lombok.Data;

/**
 * 更新版本备注请求。
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 承载章节版本备注内容
 */
@Data
public class UpdateRemarkRequest {

    /**
     * 备注内容
     */
    private String remark;
}
