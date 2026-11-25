package cn.haowl.hinovel.infra.oss.util;

/**
 * @author : haowl
 * @date : 2026/11/17 21:06
 * @desc : OSS 文件名处理
 */
public final class OssFileUtils {

    /**
     * 禁止实例化文件名工具类。
     */
    private OssFileUtils() {
    }

    /**
     * 取小写扩展名，无分隔点时使用默认值。
     */
    public static String getExtension(String filename, String defaultExtension) {
        if (filename == null) {
            return defaultExtension;
        }
        int separator = filename.lastIndexOf('.');
        return separator < 0 ? defaultExtension : filename.substring(separator + 1).toLowerCase();
    }
}
