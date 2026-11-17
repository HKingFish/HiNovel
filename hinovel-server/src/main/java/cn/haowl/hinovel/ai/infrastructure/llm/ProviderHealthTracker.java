package cn.haowl.hinovel.ai.infrastructure.llm;

import cn.haowl.hinovel.ai.constant.AiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LLM 提供方健康状态追踪器。
 *
 * 记录每个 Provider 的连续失败次数，超过阈值后标记为不健康，
 * 冷却期过后自动尝试恢复。轻量级实现，无需引入 Resilience4j 等重框架。
 *
 * @author haowl
 */
@Slf4j
@Component
public class ProviderHealthTracker {

    /**
     * 按提供方 ID 记录连续失败次数。
     */
    private final ConcurrentHashMap<Long, AtomicInteger> failureCountMap = new ConcurrentHashMap<>();

    /**
     * 按提供方 ID 记录进入冷却期的时间。
     */
    private final ConcurrentHashMap<Long, Instant> unhealthyTimestampMap = new ConcurrentHashMap<>();

    /**
     * 记录一次调用成功，重置失败计数。
     *
     * @param providerId 提供方 ID
     */
    public void recordSuccess(Long providerId) {
        failureCountMap.remove(providerId);
        if (unhealthyTimestampMap.remove(providerId) != null) {
            log.info("[recordSuccess][提供方恢复健康，providerId={}]", providerId);
        }
    }

    /**
     * 记录一次调用失败，累加失败计数。
     *
     * @param providerId 提供方 ID
     */
    public void recordFailure(Long providerId) {
        int count = failureCountMap
                .computeIfAbsent(providerId, k -> new AtomicInteger(0))
                .incrementAndGet();
        if (count >= AiConstants.PROVIDER_FAILURE_THRESHOLD
                && !unhealthyTimestampMap.containsKey(providerId)) {
            unhealthyTimestampMap.put(providerId, Instant.now());
            log.warn("[recordFailure][连续失败达到阈值，进入冷却期，次数={}，providerId={}]", count, providerId);
        }
    }

    /**
     * 判断 Provider 是否健康可用。
     *
     * 不健康的 Provider 在冷却期过后会自动尝试恢复（返回健康），
     * 如果恢复后再次失败，会重新进入不健康状态。
     *
     * @param providerId 提供方 ID
     * @return 是否健康
     */
    public boolean isHealthy(Long providerId) {
        Instant unhealthySince = unhealthyTimestampMap.get(providerId);
        if (unhealthySince == null) {
            return true;
        }
        // 冷却期过后自动尝试恢复
        long elapsedSeconds = Instant.now().getEpochSecond() - unhealthySince.getEpochSecond();
        if (elapsedSeconds >= AiConstants.PROVIDER_RECOVERY_COOLDOWN_SECONDS) {
            log.info("[isHealthy][提供方冷却期结束，允许重试，providerId={}]", providerId);
            unhealthyTimestampMap.remove(providerId);
            failureCountMap.remove(providerId);
            return true;
        }
        return false;
    }
}
