package cn.haowl.hinovel.user.domain.event;

/**
 * 用户登录事件。
 *
 * 用户登录成功后发布的领域事件。
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public class UserLoggedInEvent extends DomainEvent {

    /**
     * 登录用户 ID。
     */
    private final Long userId;
    /**
     * 登录用户名。
     */
    private final String username;

    /**
     * 创建用户登录事件。
     */
    public UserLoggedInEvent(Long userId, String username) {
        super();
        this.userId = userId;
        this.username = username;
    }

    /**
     * 返回登录用户 ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 返回登录用户名。
     */
    public String getUsername() {
        return username;
    }
}
