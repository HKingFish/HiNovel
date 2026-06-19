package cn.haowl.hinovel.common.constant;

/**
 * 通用常量类。
 *
 * <p>定义系统通用的常量配置。</p>
 *
 * @author haowl
 * @since 2024
 */
public final class CommonConstants {

    public static final String MSG_KEY_SUCCESS = "200";

    private CommonConstants() {
    }

    // ==================== HTTP 状态码 ====================

    /**
     * HTTP 状态码：成功。
     */
    public static final int HTTP_STATUS_OK = 200;

    /**
     * HTTP 状态码：已创建。
     */
    public static final int HTTP_STATUS_CREATED = 201;

    /**
     * HTTP 状态码：请求错误。
     */
    public static final int HTTP_STATUS_BAD_REQUEST = 400;

    /**
     * HTTP 状态码：未授权。
     */
    public static final int HTTP_STATUS_UNAUTHORIZED = 401;

    /**
     * HTTP 状态码：禁止访问。
     */
    public static final int HTTP_STATUS_FORBIDDEN = 403;

    /**
     * HTTP 状态码：资源未找到。
     */
    public static final int HTTP_STATUS_NOT_FOUND = 404;

    /**
     * HTTP 状态码：服务器内部错误。
     */
    public static final int HTTP_STATUS_INTERNAL_ERROR = 500;

    // ==================== 通用响应消息 ====================

    /**
     * 成功消息。
     */
    public static final String MSG_SUCCESS = "操作成功";

    /**
     * 失败消息。
     */
    public static final String MSG_FAIL = "操作失败";

    /**
     * 未授权消息。
     */
    public static final String MSG_UNAUTHORIZED = "未授权";

    /**
     * 禁止访问消息。
     */
    public static final String MSG_FORBIDDEN = "禁止访问";

    /**
     * 资源不存在消息。
     */
    public static final String MSG_NOT_FOUND = "资源不存在";

    /**
     * 系统内部错误消息。
     */
    public static final String MSG_INTERNAL_ERROR = "系统内部错误";

    // ==================== 时间常量（毫秒） ====================

    /**
     * 一秒（毫秒）。
     */
    public static final long ONE_SECOND_MS = 1000L;

    /**
     * 一分钟（毫秒）。
     */
    public static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;

    /**
     * 一小时（毫秒）。
     */
    public static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;

    /**
     * 一天（毫秒）。
     */
    public static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;

    // ==================== 时间常量（秒） ====================

    /**
     * 一分钟（秒）。
     */
    public static final int ONE_MINUTE_SECONDS = 60;

    /**
     * 一小时（秒）。
     */
    public static final int ONE_HOUR_SECONDS = 60 * ONE_MINUTE_SECONDS;

    /**
     * 一天（秒）。
     */
    public static final int ONE_DAY_SECONDS = 24 * ONE_HOUR_SECONDS;

    // ==================== 分页默认值 ====================

    /**
     * 默认页码。
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小。
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小。
     */
    public static final int MAX_PAGE_SIZE = 100;

    // ==================== 字符串长度限制 ====================

    /**
     * 长度限制：50。
     */
    public static final int LENGTH_50 = 50;

    /**
     * 长度限制：100。
     */
    public static final int LENGTH_100 = 100;

    /**
     * 长度限制：200。
     */
    public static final int LENGTH_200 = 200;

    /**
     * 长度限制：500。
     */
    public static final int LENGTH_500 = 500;

    /**
     * 长度限制：1000。
     */
    public static final int LENGTH_1000 = 1000;

    /**
     * 长度限制：2000。
     */
    public static final int LENGTH_2000 = 2000;

    /**
     * 长度限制：5000。
     */
    public static final int LENGTH_5000 = 5000;

    // ==================== 通用状态 ====================

    /**
     * 状态：激活。
     */
    public static final String STATUS_ACTIVE = "ACTIVE";

    /**
     * 状态：未激活。
     */
    public static final String STATUS_INACTIVE = "INACTIVE";

    /**
     * 状态：待处理。
     */
    public static final String STATUS_PENDING = "PENDING";

    /**
     * 状态：处理中。
     */
    public static final String STATUS_PROCESSING = "PROCESSING";

    /**
     * 状态：成功。
     */
    public static final String STATUS_SUCCESS = "SUCCESS";

    /**
     * 状态：失败。
     */
    public static final String STATUS_FAILED = "FAILED";

    /**
     * 状态：已删除。
     */
    public static final String STATUS_DELETED = "DELETED";

    // ==================== 角色类型 ====================

    /**
     * 角色：普通用户。
     */
    public static final String ROLE_USER = "USER";

    /**
     * 角色：管理员。
     */
    public static final String ROLE_ADMIN = "ADMIN";

    /**
     * 角色：系统。
     */
    public static final String ROLE_SYSTEM = "SYSTEM";

    /**
     * 角色：助手。
     */
    public static final String ROLE_ASSISTANT = "ASSISTANT";

    // ==================== 布尔值字符串 ====================

    /**
     * 布尔值：是。
     */
    public static final String YES = "Y";

    /**
     * 布尔值：否。
     */
    public static final String NO = "N";

    // ==================== 分隔符 ====================

    /**
     * 分隔符：逗号。
     */
    public static final String SEPARATOR_COMMA = ",";

    /**
     * 分隔符：分号。
     */
    public static final String SEPARATOR_SEMICOLON = ";";

    /**
     * 分隔符：冒号。
     */
    public static final String SEPARATOR_COLON = ":";

    /**
     * 分隔符：斜杠。
     */
    public static final String SEPARATOR_SLASH = "/";

    /**
     * 分隔符：连字符。
     */
    public static final String SEPARATOR_HYPHEN = "-";

    /**
     * 分隔符：下划线。
     */
    public static final String SEPARATOR_UNDERSCORE = "_";

    // ==================== 系统常量 ====================

    /**
     * 系统用户 ID（用于未登录场景）。
     */
    public static final Long SYSTEM_USER_ID = 0L;

    /**
     * 逻辑删除：未删除。
     */
    public static final Integer DELETED_FALSE = 0;

    /**
     * 逻辑删除：已删除。
     */
    public static final Integer DELETED_TRUE = 1;


    /**
     * 配置项启用判断常量
     */
    public static final int ENABLED = 1;

    /**
     * 配置项禁用判断常量
     */
    public static final int DISABLED = 0;

    // ==================== SQL 常量 ====================

    /**
     * SQL LIMIT 1 子句，用于查询单条记录。
     */
    public static final String LIMIT_ONE = "LIMIT 1";

    /**
     * 生成 SQL LIMIT 子句（带偏移量）。
     *
     * @param offset   偏移量
     * @param pageSize 每页大小
     * @return LIMIT offset, pageSize
     */
    public static String limitOf(int offset, int pageSize) {
        return "LIMIT " + offset + ", " + pageSize;
    }

    /**
     * 生成 SQL LIMIT 子句（仅限制条数）。
     *
     * @param limit 最大返回条数
     * @return LIMIT limit
     */
    public static String limitOf(int limit) {
        return "LIMIT " + limit;
    }


}
