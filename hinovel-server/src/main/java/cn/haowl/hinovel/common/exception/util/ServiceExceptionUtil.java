package cn.haowl.hinovel.common.exception.util;

import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.exception.ErrorCode;
import cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link BusinessException} 工具类。
 *
 * <p>用于格式化异常信息提示，使用 {@code {}} 作为占位符。
 *
 * @author haowl
 * @see BusinessException
 * @see ErrorCode
 * @since 2024
 */
@Slf4j
public final class ServiceExceptionUtil {

    private ServiceExceptionUtil() {
    }

    /**
     * 根据错误码创建业务异常。
     *
     * @param errorCode 错误码
     * @return 业务异常
     */
    public static BusinessException exception(ErrorCode errorCode) {
        return exception0(errorCode.getCode(), errorCode.getMsg());
    }

    /**
     * 根据错误码和参数创建业务异常。
     *
     * <p>错误消息中的 {@code {}} 占位符会被参数替换。
     *
     * @param errorCode 错误码
     * @param params    参数列表
     * @return 业务异常
     */
    public static BusinessException exception(ErrorCode errorCode, Object... params) {
        return exception0(errorCode.getCode(), errorCode.getMsg(), params);
    }

    /**
     * 根据错误码和消息创建业务异常。
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException exception0(Integer code, String message) {
        return new BusinessException(code, message);
    }

    /**
     * 根据错误码、消息模板和参数创建业务异常。
     *
     * @param code           错误码
     * @param messagePattern 消息模板（使用 {} 作为占位符）
     * @param params         参数列表
     * @return 业务异常
     */
    public static BusinessException exception0(Integer code, String messagePattern, Object... params) {
        String message = doFormat(code, messagePattern, params);
        return new BusinessException(code, message);
    }

    /**
     * 创建参数校验异常。
     *
     * @param messagePattern 参数校验消息模板
     * @param params         参数列表
     * @return 业务异常
     */
    public static BusinessException invalidParamException(String messagePattern, Object... params) {
        return exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), messagePattern, params);
    }

    /**
     * 将错误消息使用参数进行格式化。
     *
     * @param code           错误码
     * @param messagePattern 消息模板（使用 {} 作为占位符）
     * @param params         参数列表
     * @return 格式化后的消息
     */
    public static String doFormat(int code, String messagePattern, Object... params) {
        StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);
        int i = 0;
        int j;
        int l;
        for (l = 0; l < params.length; l++) {
            j = messagePattern.indexOf("{}", i);
            if (j == -1) {
                log.error("[doFormat][参数过多：错误码({})|错误内容({})|参数({})", code, messagePattern, params);
                if (i == 0) {
                    return messagePattern;
                } else {
                    sbuf.append(messagePattern.substring(i));
                    return sbuf.toString();
                }
            } else {
                sbuf.append(messagePattern, i, j);
                sbuf.append(params[l]);
                i = j + 2;
            }
        }
        if (messagePattern.indexOf("{}", i) != -1) {
            log.error("[doFormat][参数过少：错误码({})|错误内容({})|参数({})", code, messagePattern, params);
        }
        sbuf.append(messagePattern.substring(i));
        return sbuf.toString();
    }

}
