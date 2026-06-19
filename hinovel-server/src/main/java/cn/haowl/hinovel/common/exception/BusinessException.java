package cn.haowl.hinovel.common.exception;

import cn.haowl.hinovel.common.response.ErrorCode;
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
     * 使用错误码构造业务异常。
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用错误码和自定义消息构造业务异常。
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
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
    }
}
