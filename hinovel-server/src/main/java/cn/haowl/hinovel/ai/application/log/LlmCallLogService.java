package cn.haowl.hinovel.ai.application.log;

import cn.haowl.hinovel.ai.constant.AiConstants;
import cn.haowl.hinovel.ai.domain.entity.LlmCallLog;
import cn.haowl.hinovel.ai.infrastructure.mapper.LlmCallLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM 调用记录服务。
 *
 * <p>提供调用记录的异步写入、分页查询和消耗统计能力。
 * 写入操作使用 {@code @Async} 异步执行，避免阻塞主业务流程。</p>
 *
 * @author haowl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmCallLogService {

    private final LlmCallLogMapper llmCallLogMapper;

    /**
     * 异步保存调用记录。
     *
     * <p>截断超长内容后入库，异常时仅打印日志，不影响主业务。</p>
     *
     * @param callLog 调用记录实体
     */
    @Async
    public void saveAsync(LlmCallLog callLog) {
        try {
            truncateContent(callLog);
            llmCallLogMapper.insert(callLog);
        } catch (Exception e) {
            log.error("保存 LLM 调用记录失败，场景={}，异常信息：{}",
                    callLog.getCallScene(), e.getMessage());
        }
    }

    /**
     * 分页查询调用记录（按用户隔离，支持时间范围筛选）。
     *
     * @param pageNum   页码（从 1 开始）
     * @param pageSize  每页条数
     * @param callScene 调用场景（可选，为空则不过滤）
     * @param status    调用状态（可选，为空则不过滤）
     * @param startTime 开始时间（可选，为空则不过滤）
     * @param endTime   结束时间（可选，为空则不过滤）
     * @param userId    当前用户 ID
     * @return 分页结果
     */
    public IPage<LlmCallLog> listByPage(int pageNum, int pageSize,
                                        String callScene, String status,
                                        LocalDateTime startTime, LocalDateTime endTime,
                                        Long userId) {
        LambdaQueryWrapper<LlmCallLog> wrapper = buildQueryWrapper(
                callScene, status, startTime, endTime, userId);
        wrapper.orderByDesc(LlmCallLog::getCreateTime);
        return llmCallLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 统计指定时间范围内的调用消耗汇总。
     *
     * @param callScene 调用场景（可选）
     * @param status    调用状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param userId    当前用户 ID
     * @return 包含 successCount、failCount、totalTokens、totalDuration 的统计结果
     */
    public Map<String, Object> getCallStatistics(String callScene, String status,
                                                 LocalDateTime startTime, LocalDateTime endTime,
                                                 Long userId) {
        LambdaQueryWrapper<LlmCallLog> wrapper = buildQueryWrapper(
                callScene, status, startTime, endTime, userId);
        List<LlmCallLog> logs = llmCallLogMapper.selectList(wrapper);

        long successCount = logs.stream()
                .filter(l -> LlmCallLog.STATUS_SUCCESS.equals(l.getStatus()))
                .count();
        long failCount = logs.stream()
                .filter(l -> LlmCallLog.STATUS_FAILED.equals(l.getStatus()))
                .count();
        long totalTokens = logs.stream()
                .mapToLong(l -> l.getTotalTokens() != null ? l.getTotalTokens() : 0)
                .sum();
        long totalDuration = logs.stream()
                .mapToLong(l -> l.getProcessingTimeMs() != null ? l.getProcessingTimeMs() : 0)
                .sum();

        Map<String, Object> result = new HashMap<>(4);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalTokens", totalTokens);
        result.put("totalDuration", totalDuration);
        return result;
    }

    /**
     * 查询所有调用记录（不分页）。
     *
     * @return 调用记录列表
     */
    public List<LlmCallLog> listAll() {
        LambdaQueryWrapper<LlmCallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(LlmCallLog::getCreateTime);
        return llmCallLogMapper.selectList(wrapper);
    }

    /**
     * 根据 ID 查询调用记录详情。
     *
     * @param id 记录 ID
     * @return 调用记录实体
     */
    public LlmCallLog getById(Long id) {
        return llmCallLogMapper.selectById(id);
    }

    /**
     * 构建通用查询条件。
     *
     * @param callScene 调用场景
     * @param status    调用状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param userId    用户 ID
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<LlmCallLog> buildQueryWrapper(String callScene, String status,
                                                             LocalDateTime startTime,
                                                             LocalDateTime endTime,
                                                             Long userId) {
        LambdaQueryWrapper<LlmCallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmCallLog::getUserId, userId);
        if (callScene != null && !callScene.isBlank()) {
            wrapper.eq(LlmCallLog::getCallScene, callScene);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(LlmCallLog::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(LlmCallLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(LlmCallLog::getCreateTime, endTime);
        }
        return wrapper;
    }

    /**
     * 截断超长的请求/响应内容，避免数据库字段溢出。
     *
     * @param callLog 调用记录实体
     */
    private void truncateContent(LlmCallLog callLog) {
        if (callLog.getRequestContent() != null
                && callLog.getRequestContent().length() > AiConstants.MAX_CONTENT_LENGTH) {
            callLog.setRequestContent(
                    callLog.getRequestContent().substring(0, AiConstants.MAX_CONTENT_LENGTH)
                            + "...(已截断)");
        }
        if (callLog.getResponseContent() != null
                && callLog.getResponseContent().length() > AiConstants.MAX_CONTENT_LENGTH) {
            callLog.setResponseContent(
                    callLog.getResponseContent().substring(0, AiConstants.MAX_CONTENT_LENGTH)
                            + "...(已截断)");
        }
    }
}
