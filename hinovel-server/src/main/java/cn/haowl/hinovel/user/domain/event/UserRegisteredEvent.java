package cn.haowl.hinovel.user.domain.event;

/**
 * 用户注册事件。
 *
 * <p>用户注册成功后发布的领域事件。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public class UserRegisteredEvent extends DomainEvent {

    private final Long userId;
    private final String username;
    private final String email;

    public UserRegisteredEvent(Long userId, String username, String email) {
        super();
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
