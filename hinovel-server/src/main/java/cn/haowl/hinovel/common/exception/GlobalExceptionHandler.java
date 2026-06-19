package cn.haowl.hinovel.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
 * <p>支持国际化：通过 Accept-Language 请求头返回对应语言的错误消息。</p>
 *
 * @author haowl
 * @since 2024
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    /**
     * 解析国际化消息。
     *
     * <p>找不到时回退到 ErrorCode 默认消息。</p>
     *
     * @param errorCode 错误码
     * @return 国际化消息
     */
    private String resolveMessage(ErrorCode errorCode) {
        try {
            return messageSource.getMessage(
                    errorCode.getMessageKey(),
                    null,
                    errorCode.getDefaultMessage(),
                    LocaleContextHolder.getLocale()
            );
        } catch (Exception e) {
            return errorCode.getDefaultMessage();
        }
    }

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
        return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), resolveMessage(ErrorCode.UNAUTHORIZED));
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
        return ApiResponse.error(ErrorCode.FORBIDDEN.getCode(), resolveMessage(ErrorCode.FORBIDDEN));
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
        return ApiResponse.error(ErrorCode.FORBIDDEN.getCode(), resolveMessage(ErrorCode.FORBIDDEN));
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
        return ApiResponse.error(ErrorCode.PARAM_ERROR.getCode(), message);
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
                ErrorCode.INTERNAL_ERROR.getCode(),
                resolveMessage(ErrorCode.INTERNAL_ERROR)
        );
    }
}
