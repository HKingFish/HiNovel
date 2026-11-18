package cn.haowl.hinovel.novel.interfaces.dto;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.novel.constant.NovelStatusEnum;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants.PARAM_INVALID;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

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

    /**
     * 将可选的状态编码转换为小说状态。
     */
    public NovelStatusEnum parseStatus() {
        if (status == null) {
            return null;
        }
        try {
            return NovelStatusEnum.valueOf(status);
        } catch (IllegalArgumentException ignored) {
            throw exception(PARAM_INVALID, "小说状态");
        }
    }
}
