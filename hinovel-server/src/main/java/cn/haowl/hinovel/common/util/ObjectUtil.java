package cn.haowl.hinovel.common.util;

import java.util.function.Consumer;

/**
 * @author : haowl
 * @date : 2026/11/18 10:21
 * @desc : 按对象空值状态执行操作
 */
public final class ObjectUtil {
    /**
     * 工具类通过静态方法调用。
     */
    private ObjectUtil() {
    }

    /**
     * 对象为 null 时执行操作。
     *
     * @param value 待检查对象
     * @param action 空值时执行的操作
     */
    public static void doIfNull(Object value, Runnable action) {
        if (value == null) {
            action.run();
        }
    }

    /**
     * 对象非空时将其传给调用方指定的操作。
     *
     * @param value 待检查对象
     * @param action 接收非空值的操作
     */
    public static <T> void doIfNonNull(T value, Consumer<? super T> action) {
        if (value != null) {
            action.accept(value);
        }
    }
}
