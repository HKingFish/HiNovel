package cn.haowl.hinovel.common.response;

import cn.haowl.hinovel.common.constant.CommonConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一 API 响应体。
 *
 * <p>所有接口返回值统一使用此结构。</p>
 *
 * @author haowl
 * @since 2024
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 响应码。
     */
    private int code;

    /**
     * 响应消息。
     */
    private String message;

    /**
     * 响应数据。
     */
    private T data;

    /**
     * 响应时间戳。
     */
    private String timestamp;

    /**
     * 私有构造方法。
     *
     * @param code    响应码
     * @param message 响应消息
     * @param data    响应数据
     */
    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * 成功响应（无数据）。
     *
     * @param <T> 数据类型
     * @return API 响应
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(CommonConstants.HTTP_STATUS_OK, CommonConstants.MSG_SUCCESS, null);
    }

    /**
     * 成功响应（带数据）。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return API 响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(CommonConstants.HTTP_STATUS_OK, CommonConstants.MSG_SUCCESS, data);
    }

    /**
     * 成功响应（自定义消息）。
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return API 响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(CommonConstants.HTTP_STATUS_OK, message, data);
    }

    /**
     * 失败响应（自定义错误码和消息）。
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return API 响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 失败响应（使用 ErrorCode 枚举）。
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return API 响应
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败响应（使用 ErrorCode 枚举，自定义消息）。
     *
     * @param errorCode 错误码枚举
     * @param message   错误消息
     * @param <T>       数据类型
     * @return API 响应
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getCode(), message, null);
    }
}
