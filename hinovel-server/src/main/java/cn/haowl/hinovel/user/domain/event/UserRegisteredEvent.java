package cn.haowl.hinovel.user.domain.event;

/**
 * 用户注册事件。
 *
 * 用户注册成功后发布的领域事件。
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public class UserRegisteredEvent extends DomainEvent {

    /**
     * 注册用户 ID。
     */
    private final Long userId;
    /**
     * 注册用户名。
     */
    private final String username;
    /**
     * 注册邮箱。
     */
    private final String email;

    /**
     * 创建用户注册事件。
     */
    public UserRegisteredEvent(Long userId, String username, String email) {
        super();
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    /**
     * 返回注册用户 ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 返回注册用户名。
     */
    public String getUsername() {
        return username;
    }

    /**
     * 返回注册邮箱。
     */
    public String getEmail() {
        return email;
    }
}
