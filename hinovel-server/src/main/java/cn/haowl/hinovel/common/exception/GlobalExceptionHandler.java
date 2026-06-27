package cn.haowl.hinovel.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants;
import cn.haowl.hinovel.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>统一处理业务异常、Sa-Token 认证异常、参数校验异常。</p>
 *
 * @author haowl
 * @since 2024
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param e 业务异常
     * @return API 响应
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理 Sa-Token 未登录异常。
     *
     * <p>Token 无效、过期、未传时触发。</p>
     *
     * @param e 未登录异常
     * @return API 响应
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleNotLoginException(NotLoginException e) {
        log.warn("Sa-Token 未登录: {}", e.getMessage());
        return ApiResponse.error(
            GlobalErrorCodeConstants.UNAUTHORIZED.getCode(),
            GlobalErrorCodeConstants.UNAUTHORIZED.getMsg()
        );
    }

    /**
     * 处理 Sa-Token 角色不足异常。
     *
     * @param e 角色不足异常
     * @return API 响应
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleNotRoleException(NotRoleException e) {
        log.warn("Sa-Token 角色不足: {}", e.getMessage());
        return ApiResponse.error(
            GlobalErrorCodeConstants.FORBIDDEN.getCode(),
            GlobalErrorCodeConstants.FORBIDDEN.getMsg()
        );
    }

    /**
     * 处理 Sa-Token 权限不足异常。
     *
     * @param e 权限不足异常
     * @return API 响应
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("Sa-Token 权限不足: {}", e.getMessage());
        return ApiResponse.error(
            GlobalErrorCodeConstants.FORBIDDEN.getCode(),
            GlobalErrorCodeConstants.FORBIDDEN.getMsg()
        );
    }

    /**
     * 处理参数校验异常。
     *
     * <p>@Valid 触发的校验失败。</p>
     *
     * @param e 参数校验异常
     * @return API 响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return ApiResponse.error(
            GlobalErrorCodeConstants.PARAM_ERROR.getCode(),
            message
        );
    }

    /**
     * 兜底异常处理。
     *
     * <p>避免内部错误信息泄露。</p>
     *
     * @param e 异常
     * @return API 响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.error(
            GlobalErrorCodeConstants.INTERNAL_ERROR.getCode(),
            GlobalErrorCodeConstants.INTERNAL_ERROR.getMsg()
        );
    }
}
