package cn.haowl.hinovel.infra.enums;

import cn.haowl.hinovel.common.exception.ErrorCode;

import static cn.haowl.hinovel.common.exception.ServiceErrorCodeRange.INFRA_MQ_ERROR_CODE_MIN;
import static cn.haowl.hinovel.common.exception.ServiceErrorCodeRange.INFRA_OSS_ERROR_CODE_MIN;

/**
 * Infra 基础设施模块错误码常量接口。
 *
 * <p>定义基础设施模块相关的错误码。
 * 错误码范围：[1-005-000-000 ~ 1-006-000-000)</p>
 *
 * @author haowl
 * @see ErrorCode
 * @since 2024
 */
public interface InfraErrorCodeConstants {

    // ==================== Infra OSS 存储模块 [1-005-001-xxx] ====================

    /**
     * 文件上传失败。
     */
    ErrorCode OSS_UPLOAD_FAILED = new ErrorCode(
        INFRA_OSS_ERROR_CODE_MIN + 1,
        "文件上传失败，请稍后重试"
    );

    /**
     * 文件删除失败。
     */
    ErrorCode OSS_DELETE_FAILED = new ErrorCode(
        INFRA_OSS_ERROR_CODE_MIN + 2,
        "文件删除失败"
    );

    /**
     * 文件不存在。
     */
    ErrorCode OSS_FILE_NOT_EXISTS = new ErrorCode(
        INFRA_OSS_ERROR_CODE_MIN + 3,
        "文件不存在"
    );

    /**
     * 文件大小超出限制。
     */
    ErrorCode OSS_FILE_TOO_LARGE = new ErrorCode(
        INFRA_OSS_ERROR_CODE_MIN + 4,
        "文件大小超出限制"
    );

    /**
     * 文件类型不允许上传。
     */
    ErrorCode OSS_FILE_TYPE_NOT_ALLOWED = new ErrorCode(
        INFRA_OSS_ERROR_CODE_MIN + 5,
        "文件类型不允许上传"
    );

    /**
     * OSS 配置无效。
     */
    ErrorCode OSS_CONFIG_INVALID = new ErrorCode(
        INFRA_OSS_ERROR_CODE_MIN + 6,
        "OSS 配置无效"
    );

    /**
     * OSS 连接失败。
     */
    ErrorCode OSS_CONNECTION_FAILED = new ErrorCode(
        INFRA_OSS_ERROR_CODE_MIN + 7,
        "OSS 连接失败，请检查配置"
    );

    /**
     * 文件访问被拒绝。
     */
    ErrorCode OSS_ACCESS_DENIED = new ErrorCode(
        INFRA_OSS_ERROR_CODE_MIN + 8,
        "文件访问被拒绝"
    );

    // ==================== Infra MQ 消息队列模块 [1-005-002-xxx] ====================

    /**
     * 消息发送失败。
     */
    ErrorCode MQ_SEND_FAILED = new ErrorCode(
        INFRA_MQ_ERROR_CODE_MIN + 1,
        "消息发送失败"
    );

    /**
     * 消息消费失败。
     */
    ErrorCode MQ_CONSUME_FAILED = new ErrorCode(
        INFRA_MQ_ERROR_CODE_MIN + 2,
        "消息消费失败"
    );

    /**
     * MQ 连接失败。
     */
    ErrorCode MQ_CONNECTION_FAILED = new ErrorCode(
        INFRA_MQ_ERROR_CODE_MIN + 3,
        "消息队列连接失败"
    );

    /**
     * MQ 配置无效。
     */
    ErrorCode MQ_CONFIG_INVALID = new ErrorCode(
        INFRA_MQ_ERROR_CODE_MIN + 4,
        "消息队列配置无效"
    );

    /**
     * 消息超时未收到响应。
     */
    ErrorCode MQ_MESSAGE_TIMEOUT = new ErrorCode(
        INFRA_MQ_ERROR_CODE_MIN + 5,
        "消息超时未收到响应"
    );
}
