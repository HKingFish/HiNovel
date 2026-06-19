package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.constant.CommonConstants;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 小说章节历史版本实体。
 *
 * <p>记录章节每次内容变更的快照，支持版本回溯和内容对比。</p>
 *
 * @author haowl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("novel_chapter_version")
public class NovelChapterVersion {

    /**
     * 发布标记：未发布。
     */
    public static final int UNPUBLISHED = 0;

    /**
     * 发布标记：已发布。
     */
    public static final int PUBLISHED = 1;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 章节 ID。
     */
    private Long chapterId;

    /**
     * 版本内容。
     */
    private String content;

    /**
     * 字数。
     */
    private Integer wordCount;

    /**
     * 与上一版本的 diff。
     */
    private String changeDiff;

    /**
     * 版本备注。
     */
    private String remark;

    /**
     * 是否为已发布的版本（0-未发布，1-已发布）。
     *
     * @see #UNPUBLISHED
     * @see #PUBLISHED
     */
    private Integer published;

    /**
     * 内容 MD5 值，用于快速比对内容是否变化。
     */
    private String contentMd5;

    /**
     * 创建人 ID。
     */
    private Long createdBy;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 逻辑删除标志。
     *
     * @see CommonConstants#DELETED_FALSE
     * @see CommonConstants#DELETED_TRUE
     */
    @TableLogic
    private Integer deleted;

    // ==================== 工厂方法 ====================

    /**
     * 从章节创建版本快照。
     *
     * @param chapterId 章节 ID
     * @param content   版本内容
     * @param remark    版本备注（可为 null）
     * @param published 是否为发布版本（0-否，1-是）
     * @return 版本实体
     */
    public static NovelChapterVersion createFromChapter(Long chapterId, String content,
                                                        String remark, int published) {
        Objects.requireNonNull(chapterId, "章节 ID 不能为空");
        String safeContent = content != null ? content : "";

        return NovelChapterVersion.builder()
                .chapterId(chapterId)
                .content(safeContent)
                .wordCount(calculateWordCount(safeContent))
                .contentMd5(calculateMd5(safeContent))
                .remark(remark)
                .published(published)
                .createTime(LocalDateTime.now())
                .deleted(CommonConstants.DELETED_FALSE)
                .build();
    }

    // ==================== 业务方法 ====================

    /**
     * 标记为已发布版本。
     */
    public void markAsPublished() {
        this.published = PUBLISHED;
    }

    /**
     * 更新版本备注。
     *
     * @param remark 新备注
     */
    public void updateRemark(String remark) {
        this.remark = remark;
    }

    // ==================== 状态判断 ====================

    /**
     * 判断是否为已发布版本。
     *
     * @return 是否已发布
     */
    public boolean checkPublished() {
        return Objects.equals(this.published, PUBLISHED);
    }

    /**
     * 判断版本是否属于指定章节。
     *
     * @param chapterId 章节 ID
     * @return 是否属于该章节
     */
    public boolean belongsToChapter(Long chapterId) {
        return Objects.equals(this.chapterId, chapterId);
    }

    /**
     * 判断内容是否与指定 MD5 相同（用于去重）。
     *
     * @param md5 目标 MD5 值
     * @return 是否相同
     */
    public boolean contentEquals(String md5) {
        return Objects.equals(this.contentMd5, md5);
    }

    // ==================== 工具方法 ====================

    /**
     * 计算中文内容字数（去除空白字符后的长度）。
     *
     * @param content 内容
     * @return 字数
     */
    private static int calculateWordCount(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        return content.replaceAll("\\s+", "").length();
    }

    /**
     * 计算字符串的 MD5 值。
     *
     * @param input 输入字符串
     * @return MD5 十六进制字符串
     */
    private static String calculateMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }
}
