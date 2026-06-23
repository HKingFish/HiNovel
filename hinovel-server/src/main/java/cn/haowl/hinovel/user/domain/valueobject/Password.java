package cn.haowl.hinovel.user.domain.valueobject;

import cn.haowl.hinovel.common.exception.BusinessException;
import cn.haowl.hinovel.common.exception.enums.GlobalErrorCodeConstants;
import cn.haowl.hinovel.user.enums.UserErrorCodeConstants;

import java.io.Serializable;
import java.util.Objects;

/**
 * 密码值对象。
 *
 * <p>表示用户的密码，封装密码验证逻辑。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public final class Password implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 20;

    private final String value;

    private Password(String value) {
        this.value = value;
    }

    /**
     * 创建密码值对象（明文密码）。
     *
     * @param rawPassword 明文密码
     * @return 密码值对象
     * @throws BusinessException 密码不符合要求时抛出
     */
    public static Password ofRaw(String rawPassword) {
        validatePassword(rawPassword);
        return new Password(rawPassword);
    }

    /**
     * 创建密码值对象（已加密密码）。
     *
     * @param encodedPassword 已加密密码
     * @return 密码值对象
     */
    public static Password ofEncoded(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new BusinessException(GlobalErrorCodeConstants.PARAM_ERROR);
        }
        return new Password(encodedPassword);
    }

    /**
     * 验证密码格式。
     *
     * @param password 密码
     * @throws BusinessException 密码不符合要求时抛出
     */
    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException(GlobalErrorCodeConstants.PARAM_ERROR);
        }
        if (password.length() < MIN_LENGTH) {
            throw new BusinessException(UserErrorCodeConstants.PASSWORD_FORMAT_ERROR);
        }
        if (password.length() > MAX_LENGTH) {
            throw new BusinessException(UserErrorCodeConstants.PASSWORD_FORMAT_ERROR);
        }
    }

    /**
     * 获取密码值。
     *
     * @return 密码
     */
    public String getValue() {
        return value;
    }

    /**
     * 判断密码长度是否在有效范围内。
     *
     * @return 是否有效
     */
    public boolean isValidLength() {
        return value.length() >= MIN_LENGTH && value.length() <= MAX_LENGTH;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Password password = (Password) obj;
        return Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "********";
    }
}
