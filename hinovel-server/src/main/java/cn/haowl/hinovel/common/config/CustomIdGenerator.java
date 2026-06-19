package cn.haowl.hinovel.common.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义 ID 生成器（16 位）。
 *
 * <p>生成规则：时间戳（秒级，10位） + 机器位（1位） + 序列号（5位）。
 * 使用自定义纪元（2024-01-01）减少时间戳位数，可支持到 2055 年左右。</p>
 *
 * <p>机器位通过配置 hinovel.machine-id 指定（0-9），默认为 1，
 * 用于多实例部署时区分不同节点。</p>
 *
 * <p>单机每秒可生成 99999 个不重复 ID，满足当前业务需求。</p>
 *
 * @author haowl
 */
@Component
public class CustomIdGenerator implements IdentifierGenerator {

    /**
     * 自定义纪元：2024-01-01 00:00:00 UTC 的秒数
     */
    private static final long CUSTOM_EPOCH_SECONDS = 1704067200L;

    /**
     * 序列号上限（5 位，最大 99999）
     */
    private static final int SEQUENCE_MAX = 99999;

    /**
     * 机器 ID（0-9，通过配置指定）
     */
    private final int machineId;

    /**
     * 原子序列号计数器
     */
    private final AtomicInteger sequence = new AtomicInteger(0);

    /**
     * 上一次生成 ID 的秒数
     */
    private volatile long lastSecond = -1L;

    public CustomIdGenerator(@Value("${hinovel.machine-id:1}") int machineId) {
        this.machineId = machineId;
    }

    @Override
    public Number nextId(Object entity) {
        long currentSecond = System.currentTimeMillis() / 1000 - CUSTOM_EPOCH_SECONDS;

        synchronized (this) {
            if (currentSecond != lastSecond) {
                lastSecond = currentSecond;
                sequence.set(0);
            }
            int seq = sequence.incrementAndGet();
            if (seq > SEQUENCE_MAX) {
                while (currentSecond == lastSecond) {
                    currentSecond = System.currentTimeMillis() / 1000 - CUSTOM_EPOCH_SECONDS;
                }
                lastSecond = currentSecond;
                sequence.set(1);
                seq = 1;
            }
            // 时间戳（10位） + 序列号（5位） = 15位
            return currentSecond * 100000L + seq;
        }
    }
}
