package cn.haowl.hinovel.ai.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.ai.application.log.LlmCallLogService;
import cn.haowl.hinovel.ai.domain.entity.LlmCallLog;
import cn.haowl.hinovel.common.response.ApiResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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
@Slf4j
@RestController
@RequestMapping("/api/ai/llm-call-logs")
@RequiredArgsConstructor
public class LlmCallLogController {

    private final LlmCallLogService llmCallLogService;

    /**
     * 分页查询调用记录。
     *
     * @param pageNum   页码（默认 1）
     * @param pageSize  每页条数（默认 20）
     * @param callScene 调用场景（可选）
     * @param status    调用状态（可选）
     * @param startTime 开始时间（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param endTime   结束时间（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @return 分页结果
     */
    @Operation(summary = "分页查询调用记录", description = "查询当前用户的调用记录，支持按场景、状态和时间范围筛选")
    @GetMapping
    public ApiResponse<IPage<LlmCallLog>> listByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String callScene,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(
                llmCallLogService.listByPage(pageNum, pageSize, callScene, status,
                        startTime, endTime, userId));
    }

    /**
     * 查询调用消耗统计。
     *
     * @param callScene 调用场景（可选）
     * @param status    调用状态（可选）
     * @param startTime 开始时间（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param endTime   结束时间（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @return 统计结果（successCount、failCount、totalTokens、totalDuration）
     */
    @Operation(summary = "查询调用消耗统计", description = "统计指定时间范围内的调用次数、Token 消耗和总耗时")
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String callScene,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(
                llmCallLogService.getCallStatistics(callScene, status,
                        startTime, endTime, userId));
    }
}
