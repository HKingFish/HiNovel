package cn.haowl.hinovel.user.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.user.application.command.LoginCommand;
import cn.haowl.hinovel.user.application.command.RegisterCommand;
import cn.haowl.hinovel.user.application.service.AuthApplicationService;
import cn.haowl.hinovel.user.interfaces.assembler.AuthAssembler;
import cn.haowl.hinovel.user.interfaces.dto.LoginRequest;
import cn.haowl.hinovel.user.interfaces.dto.RegisterRequest;
import cn.haowl.hinovel.user.interfaces.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口。
 *
 * <p>提供注册、登录、登出功能，基于 Sa-Token 管理 Session。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Tag(name = "认证管理", description = "用户注册、登录、登出及当前用户信息查询")
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;

    /**
     * 用户注册。
     */
    @Operation(summary = "用户注册", description = "注册新账号，用户名仅允许字母、数字、下划线")
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        RegisterCommand command = AuthAssembler.toRegisterCommand(request);
        authApplicationService.register(command);
        return ApiResponse.success("注册成功", null);
    }

    /**
     * 用户登录。
     */
    @Operation(summary = "用户登录", description = "邮箱+密码登录，返回 Sa-Token 访问令牌")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = AuthAssembler.toLoginCommand(request);
        return ApiResponse.success(authApplicationService.login(command));
    }

    /**
     * 用户登出。
     */
    @Operation(summary = "用户登出", description = "销毁当前 Sa-Token Session，需在请求头携带 satoken")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authApplicationService.logout();
        return ApiResponse.success("登出成功", null);
    }

    /**
     * 获取当前登录用户信息。
     */
    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的基本信息，需在请求头携带 satoken")
    @GetMapping("/me")
    public ApiResponse<TokenResponse.UserInfo> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(authApplicationService.getCurrentUserInfo(userId));
    }
}
