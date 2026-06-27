package cn.haowl.hinovel.user.domain.valueobject;

import cn.haowl.hinovel.common.exception.BusinessException;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;
import static cn.haowl.hinovel.user.enums.UserErrorCodeConstants.EMAIL_FORMAT_ERROR;

/**
 * 邮箱值对象。
 *
 * <p>表示用户的邮箱地址，封装邮箱验证逻辑。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
public final class Email implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    /**
     * 创建邮箱值对象。
     *
     * @param email 邮箱地址
     * @return 邮箱值对象
     * @throws BusinessException 邮箱格式无效时抛出
     */
    public static Email of(String email) {
        if (email == null || email.isBlank()) {
            throw exception(EMAIL_FORMAT_ERROR);
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw exception(EMAIL_FORMAT_ERROR);
        }
        return new Email(email.toLowerCase().trim());
    }

    /**
     * 获取邮箱值。
     *
     * @return 邮箱地址
     */
    public String getValue() {
        return value;
    }

    /**
     * 判断邮箱是否属于指定域名。
     *
     * @param domain 域名
     * @return 是否属于该域名
     */
    public boolean belongsToDomain(String domain) {
        return value.endsWith("@" + domain);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Email email = (Email) obj;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
