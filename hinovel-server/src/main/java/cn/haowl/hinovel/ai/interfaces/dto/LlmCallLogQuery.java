package cn.haowl.hinovel.ai.interfaces.dto;

import cn.haowl.hinovel.ai.constant.LlmCallSceneEnum;
import cn.haowl.hinovel.ai.constant.LlmCallStatusEnum;
import cn.haowl.hinovel.common.constant.CommonConstants;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * LLM 调用记录查询对象。
 *
 * 收敛分页与筛选条件，供分页查询和消耗统计共用；参数名与原有接口保持一致。
 * userId 属于强制数据隔离条件，由调用方从登录态单独传入，不接受前端绑定。
 *
 * @author : haowl
 * @date : 2026/11/12 15:10
 * @desc : LLM 调用记录的分页与统计查询条件
 */
@Data
public class LlmCallLogQuery {

    /**
     * 调用记录每页默认条数。
     */
    private static final int DEFAULT_LOG_PAGE_SIZE = 20;

    /**
     * 时间筛选参数格式。
     */
    private static final String FILTER_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 页码（从 1 开始）。
     */
    private int pageNum = CommonConstants.DEFAULT_PAGE_NUM;

    /**
     * 每页条数。
     */
    private int pageSize = DEFAULT_LOG_PAGE_SIZE;

    /**
     * 调用场景（可选，为空则不过滤）。
     *
     * @see LlmCallSceneEnum
     */
    private String callScene;

    /**
     * 调用状态（可选，为空则不过滤）。
     *
     * @see LlmCallStatusEnum
     */
    private String status;

    /**
     * 开始时间（可选）。
     */
    @DateTimeFormat(pattern = FILTER_TIME_FORMAT)
    private LocalDateTime startTime;

    /**
     * 结束时间（可选）。
     */
    @DateTimeFormat(pattern = FILTER_TIME_FORMAT)
    private LocalDateTime endTime;
}
