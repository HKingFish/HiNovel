package cn.haowl.hinovel.ai.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.ai.application.log.LlmCallLogService;
import cn.haowl.hinovel.ai.domain.entity.LlmCallLog;
import cn.haowl.hinovel.ai.interfaces.dto.LlmCallLogQuery;
import cn.haowl.hinovel.common.response.ApiResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * LLM 调用记录控制器。
 *
 * <p>提供 LLM 调用记录的分页查询、详情查看和消耗统计接口，
 * 供前端调用记录管理页面使用。</p>
 *
 * @author haowl
 */
@Tag(name = "LLM 调用记录", description = "LLM 调用记录的查询与统计")
@RestController
@RequestMapping("/api/ai/llm-call-logs")
public class LlmCallLogController {

    /** LLM 调用记录服务。 */
    @Autowired
    private LlmCallLogService llmCallLogService;

    /**
     * 分页查询调用记录。
     *
     * @param query 查询条件（页码、每页条数、场景、状态、时间范围）
     * @return 分页结果
     */
    @Operation(summary = "分页查询调用记录", description = "查询当前用户的调用记录，支持按场景、状态和时间范围筛选")
    @GetMapping
    public ApiResponse<IPage<LlmCallLog>> listByPage(@ModelAttribute LlmCallLogQuery query) {
        return ApiResponse.success(
                llmCallLogService.listByPage(query, StpUtil.getLoginIdAsLong()));
    }

    /**
     * 查询调用消耗统计。
     *
     * @param query 查询条件（场景、状态、时间范围）
     * @return 统计结果（successCount、failCount、totalTokens、totalDuration）
     */
    @Operation(summary = "查询调用消耗统计", description = "统计指定时间范围内的调用次数、Token 消耗和总耗时")
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(@ModelAttribute LlmCallLogQuery query) {
        return ApiResponse.success(
                llmCallLogService.getCallStatistics(query, StpUtil.getLoginIdAsLong()));
    }
}
