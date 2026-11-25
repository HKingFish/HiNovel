package cn.haowl.hinovel.common.config;

import cn.haowl.hinovel.common.constant.CommonConstants;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 自定义数字 ID 生成器。
 *
 * 生成规则为相对纪元的秒数、机器位（1 位）和序列号（5 位）。
 *
 * 机器位通过配置 hinovel.machine-id 指定（0-9），默认为 1，
 * 用于多实例部署时区分不同节点。
 *
 * @author haowl
 */
@Component
public class CustomIdGenerator implements IdentifierGenerator {

    /**
     * 自定义纪元（2024-01-01 00:00:00 UTC）。
     */
    private static final long CUSTOM_EPOCH_SECONDS = 1704067200L;

    /**
     * 序列号上限。
     */
    private static final int SEQUENCE_MAX = 99999;

    /**
     * 机器位支持的节点数。
     */
    private static final int MACHINE_COUNT = 10;

    /**
     * ID 中预留五位序列号。
     */
    private static final long SEQUENCE_FACTOR = 100000L;

    /**
     * 机器 ID（0-9，通过配置指定）。
     */
    private final int machineId;

    /**
     * 当前逻辑秒的序列号。
     */
    private int sequence;

    /**
     * 最近一次生成 ID 的逻辑秒。
     */
    private long lastSecond = -1L;

    /**
     * 按配置初始化 ID 生成器。
     */
    public CustomIdGenerator(@Value("${hinovel.machine-id:1}") int machineId) {
        if (machineId < 0 || machineId >= MACHINE_COUNT) {
            throw new IllegalArgumentException("hinovel.machine-id 必须在 0 到 9 之间");
        }
        this.machineId = machineId;
    }

    /**
     * 生成包含机器位的递增 ID。
     */
    @Override
    public synchronized Number nextId(Object entity) {
        long currentSecond = System.currentTimeMillis() / CommonConstants.ONE_SECOND_MS - CUSTOM_EPOCH_SECONDS;

        // 时钟回拨时沿用逻辑秒，序列用完后推进一秒。
        if (currentSecond > lastSecond || sequence >= SEQUENCE_MAX) {
            lastSecond = Math.max(currentSecond, lastSecond + 1);
            sequence = 0;
        }
        sequence++;
        return (lastSecond * MACHINE_COUNT + machineId) * SEQUENCE_FACTOR + sequence;
    }
}
