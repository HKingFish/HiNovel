package cn.haowl.hinovel.user.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册命令。
 *
 * <p>表示用户注册的写操作请求。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCommand {

    /**
     * 用户名。
     */
    private String username;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 密码。
     */
    private String password;
}
