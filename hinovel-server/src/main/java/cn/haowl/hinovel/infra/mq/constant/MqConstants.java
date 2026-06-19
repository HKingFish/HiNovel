package cn.haowl.hinovel.infra.mq.constant;

/**
 * 消息队列常量类。
 *
 * <p>定义消息队列相关的常量配置。</p>
 *
 * @author haowl
 * @since 2024
 */
public final class MqConstants {

    private MqConstants() {
    }

    // ==================== Exchange 名称 ====================

    /**
     * 默认 Exchange。
     */
    public static final String DEFAULT_EXCHANGE = "hinovel.default";

    /**
     * 延迟 Exchange。
     */
    public static final String DELAY_EXCHANGE = "hinovel.delay";

    /**
     * 死信 Exchange。
     */
    public static final String DLX_EXCHANGE = "hinovel.dlx";

    // ==================== Queue 名称 ====================

    /**
     * Agent 执行队列。
     */
    public static final String QUEUE_AGENT_EXECUTE = "hinovel.agent.execute";

    /**
     * Agent 回调队列。
     */
    public static final String QUEUE_AGENT_CALLBACK = "hinovel.agent.callback";

    /**
     * 邮件发送队列。
     */
    public static final String QUEUE_EMAIL_SEND = "hinovel.email.send";

    /**
     * 系统通知队列。
     */
    public static final String QUEUE_NOTIFICATION = "hinovel.notification";

    /**
     * 死信队列。
     */
    public static final String QUEUE_DLQ = "hinovel.dlq";

    // ==================== Routing Key ====================

    /**
     * Agent 执行路由键。
     */
    public static final String ROUTING_KEY_AGENT_EXECUTE = "agent.execute";

    /**
     * Agent 回调路由键。
     */
    public static final String ROUTING_KEY_AGENT_CALLBACK = "agent.callback";

    /**
     * 邮件发送路由键。
     */
    public static final String ROUTING_KEY_EMAIL_SEND = "email.send";

    /**
     * 系统通知路由键。
     */
    public static final String ROUTING_KEY_NOTIFICATION = "notification";

    // ==================== 消息头 ====================

    /**
     * 消息头：消息类型。
     */
    public static final String HEADER_MESSAGE_TYPE = "messageType";

    /**
     * 消息头：重试次数。
     */
    public static final String HEADER_RETRY_COUNT = "retryCount";

    /**
     * 消息头：原始队列。
     */
    public static final String HEADER_ORIGINAL_QUEUE = "originalQueue";

    /**
     * 消息头：异常信息。
     */
    public static final String HEADER_EXCEPTION = "exception";

    // ==================== 配置参数 ====================

    /**
     * 默认消息 TTL（毫秒）。
     */
    public static final long DEFAULT_MESSAGE_TTL = 86400000L;

    /**
     * 最大重试次数。
     */
    public static final int MAX_RETRY_COUNT = 3;

    /**
     * 重试延迟（毫秒）。
     */
    public static final long RETRY_DELAY_MS = 5000L;
}
