package cn.haowl.hinovel.common.exception;

import lombok.Getter;

/**
 * 错误码对象。
 *
 * <p>错误码采用 10 位数字格式：1-SSS-MMM-EEE
 * <ul>
 *   <li>第 1 位：1，表示业务级别异常</li>
 *   <li>第 2-4 位（SSS）：系统类型，如 001=user, 002=novel, 003=agent</li>
 *   <li>第 5-7 位（MMM）：模块细分，每个系统内有多个模块</li>
 *   <li>第 8-10 位（EEE）：错误序号，每个模块内自增</li>
 * </ul>
 * </p>
 *
 * <p>此设计参考 ruoyi-vue-pro 的错误码规范，便于错误追踪和国际化。</p>
 *
 * @author haowl
 * @see ServiceErrorCodeRange
 * @since 2024
 */
@Getter
public class ErrorCode {

    /**
     * 错误码。
     * <p>格式：1-SSS-MMM-EEE</p>
     */
    private final Integer code;

    /**
     * 错误提示信息。
     */
    private final String msg;

    /**
     * 构造错误码对象。
     *
     * @param code 错误码
     * @param msg  错误提示信息
     */
    public ErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取错误码的补位字符串（10 位，不足前补 0）。
     *
     * @return 补位后的错误码字符串
     */
    public String getCodeString() {
        return String.format("%010d", code);
    }

    @Override
    public String toString() {
        return "ErrorCode{code=" + code + ", msg='" + msg + "'}";
    }
}
