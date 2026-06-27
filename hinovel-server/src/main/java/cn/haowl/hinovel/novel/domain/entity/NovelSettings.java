package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.constant.CommonConstants;
import cn.haowl.hinovel.common.entity.BaseEntity;
import cn.haowl.hinovel.novel.constant.NovelConstants;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说配置实体。
 *
 * <p>统一管理小说级别和用户级别的 AI 功能配置。
 * 当 novelId = 0 时表示用户级别默认配置，novelId > 0 时表示小说级别配置。
 * 小说级别配置优先级高于用户默认配置。</p>
 *
 * @author haowl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("novel_settings")
public class NovelSettings extends BaseEntity {

    /**
     * 用户级别默认配置的 novelId 占位值
     */
    public static final long USER_DEFAULT_NOVEL_ID = 0L;

    /**
     * 小说 ID（0 表示用户级别默认配置）
     */
    private Long novelId;

    /**
     * 用户 ID（用户级别默认配置时必填）
     */
    private Long userId;

    /**
     * 改写后自动审核（0-否，1-是）
     *
     * @see CommonConstants#DISABLED
     * @see CommonConstants#ENABLED
     */
    private Integer autoAuditAfterRewrite;

    /**
     * 发布后自动生成 AI 大纲（0-否，1-是）
     *
     * @see CommonConstants#DISABLED
     * @see CommonConstants#ENABLED
     */
    private Integer autoOutlineAfterPublish;

    /**
     * 发布后自动存储向量（0-否，1-是）
     *
     * @see CommonConstants#DISABLED
     * @see CommonConstants#ENABLED
     */
    private Integer autoVectorAfterPublish;

    /**
     * AI 改写时携带前几章内容作为前情提要（0 表示不携带）
     *
     * @see NovelConstants#DEFAULT_REWRITE_CONTEXT_CHAPTERS
     */
    private Integer rewriteContextChapters;

    /**
     * AI 改写时是否携带全文大纲（0-否，1-是）
     *
     * @see CommonConstants#DISABLED
     * @see CommonConstants#ENABLED
     */
    private Integer rewriteIncludeOutline;

    /**
     * AI 审核时是否携带全文大纲（0-否，1-是）
     *
     * @see CommonConstants#DISABLED
     * @see CommonConstants#ENABLED
     */
    private Integer auditIncludeOutline;

    /**
     * AI 问答时是否携带全文大纲（0-否，1-是）
     *
     * @see CommonConstants#DISABLED
     * @see CommonConstants#ENABLED
     */
    private Integer qaIncludeOutline;

    /**
     * AI 问答时携带的历史消息条数（0 表示不携带）
     *
     * @see NovelConstants#DEFAULT_QA_CONTEXT_LENGTH
     */
    private Integer qaContextLength;

    /**
     * 是否自动保存内容变化（0-否，1-是）
     * 开启后编辑器内容变化时自动保存到服务器，关闭后需手动点击保存
     *
     * @see CommonConstants#DISABLED
     * @see CommonConstants#ENABLED
     */
    private Integer autoSaveContent;

    /**
     * 判断是否为用户级别默认配置。
     *
     * @return 是否为用户默认配置
     */
    @JsonIgnore
    public boolean isUserDefault() {
        return novelId != null && USER_DEFAULT_NOVEL_ID == novelId;
    }

    /**
     * 创建默认配置实例（所有配置项使用默认值）。
     *
     * @return 默认配置
     */
    public static NovelSettings createDefault() {
        NovelSettings settings = new NovelSettings();
        settings.setAutoAuditAfterRewrite(CommonConstants.DISABLED);
        settings.setAutoOutlineAfterPublish(CommonConstants.DISABLED);
        settings.setAutoVectorAfterPublish(CommonConstants.DISABLED);
        settings.setRewriteContextChapters(NovelConstants.DEFAULT_REWRITE_CONTEXT_CHAPTERS);
        settings.setRewriteIncludeOutline(CommonConstants.ENABLED);
        settings.setAuditIncludeOutline(CommonConstants.ENABLED);
        settings.setQaIncludeOutline(CommonConstants.ENABLED);
        settings.setQaContextLength(NovelConstants.DEFAULT_QA_CONTEXT_LENGTH);
        settings.setAutoSaveContent(CommonConstants.ENABLED);
        return settings;
    }

    /**
     * 将小说级别配置与用户默认配置合并（小说级别优先）。
     *
     * <p>如果小说级别某个字段为 null，则使用用户默认配置的值。</p>
     *
     * @param novelLevel  小说级别配置（可为 null）
     * @param userDefault 用户默认配置（可为 null）
     * @return 合并后的有效配置
     */
    public static NovelSettings merge(NovelSettings novelLevel, NovelSettings userDefault) {
        NovelSettings base = createDefault();

        // 先用用户默认配置覆盖系统默认值
        if (userDefault != null) {
            applyNonNull(base, userDefault);
        }
        // 再用小说级别配置覆盖
        if (novelLevel != null) {
            applyNonNull(base, novelLevel);
        }
        return base;
    }

    /**
     * 将 source 中非 null 的字段覆盖到 target。
     */
    private static void applyNonNull(NovelSettings target, NovelSettings source) {
        if (source.getAutoAuditAfterRewrite() != null) {
            target.setAutoAuditAfterRewrite(source.getAutoAuditAfterRewrite());
        }
        if (source.getAutoOutlineAfterPublish() != null) {
            target.setAutoOutlineAfterPublish(source.getAutoOutlineAfterPublish());
        }
        if (source.getAutoVectorAfterPublish() != null) {
            target.setAutoVectorAfterPublish(source.getAutoVectorAfterPublish());
        }
        if (source.getRewriteContextChapters() != null) {
            target.setRewriteContextChapters(source.getRewriteContextChapters());
        }
        if (source.getRewriteIncludeOutline() != null) {
            target.setRewriteIncludeOutline(source.getRewriteIncludeOutline());
        }
        if (source.getAuditIncludeOutline() != null) {
            target.setAuditIncludeOutline(source.getAuditIncludeOutline());
        }
        if (source.getQaIncludeOutline() != null) {
            target.setQaIncludeOutline(source.getQaIncludeOutline());
        }
        if (source.getQaContextLength() != null) {
            target.setQaContextLength(source.getQaContextLength());
        }
        if (source.getAutoSaveContent() != null) {
            target.setAutoSaveContent(source.getAutoSaveContent());
        }
    }
}
