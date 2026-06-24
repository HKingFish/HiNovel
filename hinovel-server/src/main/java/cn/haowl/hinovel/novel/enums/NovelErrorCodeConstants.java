package cn.haowl.hinovel.novel.enums;

import cn.haowl.hinovel.common.exception.ErrorCode;

import static cn.haowl.hinovel.common.exception.ServiceErrorCodeRange.*;

/**
 * Novel 模块错误码常量接口。
 *
 * <p>定义 Novel 小说模块相关的错误码。
 * 错误码范围：[1-002-000-000 ~ 1-003-000-000)</p>
 *
 * @author haowl
 * @see ErrorCode
 * @since 2024
 */
public interface NovelErrorCodeConstants {

    // ==================== Novel 小说模块 [1-002-001-xxx] ====================

    /**
     * 小说不存在。
     */
    ErrorCode NOVEL_NOT_EXISTS = new ErrorCode(
        NOVEL_NOVEL_ERROR_CODE_MIN + 1,
        "小说不存在"
    );

    /**
     * 小说名称已存在。
     */
    ErrorCode NOVEL_NAME_EXISTS = new ErrorCode(
        NOVEL_NOVEL_ERROR_CODE_MIN + 2,
        "小说名称已存在"
    );

    /**
     * 小说已发布，不能删除。
     */
    ErrorCode NOVEL_PUBLISHED_CANNOT_DELETE = new ErrorCode(
        NOVEL_NOVEL_ERROR_CODE_MIN + 3,
        "小说已发布，不能删除"
    );

    /**
     * 小说封面上传失败。
     */
    ErrorCode NOVEL_COVER_UPLOAD_FAILED = new ErrorCode(
        NOVEL_NOVEL_ERROR_CODE_MIN + 4,
        "小说封面上传失败"
    );

    /**
     * 无权操作此小说。
     */
    ErrorCode NOVEL_NOT_OWNED = new ErrorCode(
        NOVEL_NOVEL_ERROR_CODE_MIN + 5,
        "无权操作此小说"
    );

    /**
     * 小说状态不允许此操作。
     */
    ErrorCode NOVEL_STATUS_NOT_ALLOWED = new ErrorCode(
        NOVEL_NOVEL_ERROR_CODE_MIN + 6,
        "小说状态不允许此操作"
    );

    // ==================== Novel 章节模块 [1-002-002-xxx] ====================

    /**
     * 章节不存在。
     */
    ErrorCode CHAPTER_NOT_EXISTS = new ErrorCode(
        NOVEL_CHAPTER_ERROR_CODE_MIN + 1,
        "章节不存在"
    );

    /**
     * 章节标题已存在。
     */
    ErrorCode CHAPTER_TITLE_EXISTS = new ErrorCode(
        NOVEL_CHAPTER_ERROR_CODE_MIN + 2,
        "章节标题已存在"
    );

    /**
     * 章节序号已存在。
     */
    ErrorCode CHAPTER_NUMBER_EXISTS = new ErrorCode(
        NOVEL_CHAPTER_ERROR_CODE_MIN + 3,
        "章节序号已存在"
    );

    /**
     * 章节内容为空。
     */
    ErrorCode CHAPTER_CONTENT_EMPTY = new ErrorCode(
        NOVEL_CHAPTER_ERROR_CODE_MIN + 4,
        "章节内容不能为空"
    );

    /**
     * 章节内容过长。
     */
    ErrorCode CHAPTER_CONTENT_TOO_LONG = new ErrorCode(
        NOVEL_CHAPTER_ERROR_CODE_MIN + 5,
        "章节内容过长，请分拆章节"
    );

    /**
     * 章节已发布，不能修改。
     */
    ErrorCode CHAPTER_PUBLISHED_CANNOT_MODIFY = new ErrorCode(
        NOVEL_CHAPTER_ERROR_CODE_MIN + 6,
        "章节已发布，不能修改"
    );

    /**
     * 章节序号不连续。
     */
    ErrorCode CHAPTER_NUMBER_NOT_SEQUENTIAL = new ErrorCode(
        NOVEL_CHAPTER_ERROR_CODE_MIN + 7,
        "章节序号不连续"
    );

    /**
     * 章节保存失败。
     */
    ErrorCode CHAPTER_SAVE_FAILED = new ErrorCode(
        NOVEL_CHAPTER_ERROR_CODE_MIN + 8,
        "章节保存失败"
    );

    /**
     * 版本不属于该章节。
     */
    ErrorCode VERSION_NOT_BELONG_TO_CHAPTER = new ErrorCode(
        NOVEL_CHAPTER_ERROR_CODE_MIN + 9,
        "版本不属于该章节"
    );

    // ==================== Novel 大纲模块 [1-002-003-xxx] ====================

    /**
     * 大纲不存在。
     */
    ErrorCode OUTLINE_NOT_EXISTS = new ErrorCode(
        NOVEL_OUTLINE_ERROR_CODE_MIN + 1,
        "大纲不存在"
    );

    /**
     * 大纲内容为空。
     */
    ErrorCode OUTLINE_CONTENT_EMPTY = new ErrorCode(
        NOVEL_OUTLINE_ERROR_CODE_MIN + 2,
        "大纲内容不能为空"
    );

    /**
     * 大纲保存失败。
     */
    ErrorCode OUTLINE_SAVE_FAILED = new ErrorCode(
        NOVEL_OUTLINE_ERROR_CODE_MIN + 3,
        "大纲保存失败"
    );

    /**
     * 全文大纲不存在。
     */
    ErrorCode NOVEL_OUTLINE_NOT_EXISTS = new ErrorCode(
        NOVEL_OUTLINE_ERROR_CODE_MIN + 4,
        "全文大纲不存在"
    );

    // ==================== Novel 角色模块 [1-002-004-xxx] ====================

    /**
     * 角色不存在。
     */
    ErrorCode CHARACTER_NOT_EXISTS = new ErrorCode(
        NOVEL_CHARACTER_ERROR_CODE_MIN + 1,
        "角色不存在"
    );

    /**
     * 角色名称已存在。
     */
    ErrorCode CHARACTER_NAME_EXISTS = new ErrorCode(
        NOVEL_CHARACTER_ERROR_CODE_MIN + 2,
        "角色名称已存在"
    );

    /**
     * 角色头像上传失败。
     */
    ErrorCode CHARACTER_AVATAR_UPLOAD_FAILED = new ErrorCode(
        NOVEL_CHARACTER_ERROR_CODE_MIN + 3,
        "角色头像上传失败"
    );

    /**
     * 角色正在被使用，不能删除。
     */
    ErrorCode CHARACTER_IN_USE_CANNOT_DELETE = new ErrorCode(
        NOVEL_CHARACTER_ERROR_CODE_MIN + 4,
        "角色正在被使用，不能删除"
    );

    /**
     * 角色保存失败。
     */
    ErrorCode CHARACTER_SAVE_FAILED = new ErrorCode(
        NOVEL_CHARACTER_ERROR_CODE_MIN + 5,
        "角色保存失败"
    );
}
