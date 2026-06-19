package cn.haowl.hinovel.user.domain.event;

/**
 * 用户登录事件。
 *
 * <p>用户登录成功后发布的领域事件。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public class UserLoggedInEvent extends DomainEvent {

    private final Long userId;
    private final String username;

    public UserLoggedInEvent(Long userId, String username) {
        super();
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
