package cn.haowl.hinovel.user.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录命令。
 *
 * <p>表示用户登录的写操作请求。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginCommand {

    /**
     * 账号（用户名或邮箱）。
     */
    private String account;

    /**
     * 密码。
     */
    private String password;
}
