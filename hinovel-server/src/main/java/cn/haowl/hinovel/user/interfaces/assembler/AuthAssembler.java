package cn.haowl.hinovel.user.interfaces.assembler;

import cn.haowl.hinovel.user.application.command.LoginCommand;
import cn.haowl.hinovel.user.application.command.RegisterCommand;
import cn.haowl.hinovel.user.interfaces.dto.LoginRequest;
import cn.haowl.hinovel.user.interfaces.dto.RegisterRequest;

/**
 * 认证装配器。
 *
 * <p>负责 DTO 与领域对象之间的转换。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public final class AuthAssembler {

    private AuthAssembler() {
    }

    /**
     * 将注册请求转换为注册命令。
     *
     * @param request 注册请求
     * @return 注册命令
     */
    public static RegisterCommand toRegisterCommand(RegisterRequest request) {
        return RegisterCommand.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
    }

    /**
     * 将登录请求转换为登录命令。
     *
     * @param request 登录请求
     * @return 登录命令
     */
    public static LoginCommand toLoginCommand(LoginRequest request) {
        return LoginCommand.builder()
                .account(request.getAccount())
                .password(request.getPassword())
                .build();
    }
}
