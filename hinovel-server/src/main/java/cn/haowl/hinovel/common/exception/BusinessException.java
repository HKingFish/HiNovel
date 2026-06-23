package cn.haowl.hinovel.common.exception;

import lombok.Getter;

/**
 * 业务异常。
 *
 * <p>用于业务逻辑中抛出的可预期异常，由 {@link GlobalExceptionHandler} 统一处理。</p>
 *
 * @author haowl
 * @since 2024
 * @see GlobalExceptionHandler
 * @see ErrorCode
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码。
     */
    private final int code;

    /**
     * 错误消息。
     */
    private final String message;

    /**
     * 使用新的 ErrorCode 对象构造业务异常。
     *
     * @param errorCode 错误码对象
     */
    public BusinessException(cn.haowl.hinovel.common.exception.ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }

    /**
     * 使用新的 ErrorCode 对象和自定义消息构造业务异常。
     *
     * @param errorCode 错误码对象
     * @param message   自定义错误消息
     */
    public BusinessException(cn.haowl.hinovel.common.exception.ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
    }

    /**
     * 使用自定义错误码和消息构造业务异常。
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
