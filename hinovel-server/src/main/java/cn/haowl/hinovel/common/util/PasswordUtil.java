package cn.haowl.hinovel.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 用户密码工具类。
 *
 * <p>基于 BCrypt 单向哈希算法，提供密码加密与验证功能。
 * BCrypt 每次加密结果不同，但可通过 matches 方法验证明文与密文是否匹配。</p>
 *
 * @author haowl
 * @date 2026/3/23 16:30
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * BCrypt 编码器（线程安全，可复用）。
     */
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 对明文密码进行 BCrypt 加密。
     *
     * @param rawPassword 明文密码
     * @return BCrypt 加密后的密文
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证明文密码与加密密文是否匹配。
     *
     * @param rawPassword     明文密码
     * @param encodedPassword BCrypt 密文
     * @return 匹配返回 true，否则返回 false
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    public static void main(String[] args) {


        System.out.println(PasswordUtil.encode("123456"));
//        System.out.println(PasswordUtil.matches("Admin@123456", "$2a$10$WpZ56OswYJxgmh/hS/aHsOom/ZlvlBQY3iiT3TCA5MtovG/5SVQIa"));
    }
}
