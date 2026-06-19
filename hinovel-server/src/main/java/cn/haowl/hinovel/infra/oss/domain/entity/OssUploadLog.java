package cn.haowl.hinovel.infra.oss.domain.entity;

import cn.haowl.hinovel.infra.oss.constant.OssConstants;
import cn.haowl.hinovel.infra.oss.constant.OssStorageType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * OSS 上传日志实体。
 *
 * <p>记录每次文件上传的元数据，支持按用户和业务类型查询。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("infra_oss_upload_log")
public class OssUploadLog {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 上传用户 ID。
     */
    private Long userId;

    /**
     * 业务类型（如 avatar、knowledge）。
     */
    private String bizType;

    /**
     * 关联业务 ID（如头像关联用户 ID，文档关联知识库 ID）。
     */
    private Long bizId;

    /**
     * 原始文件名。
     */
    private String originalFilename;

    /**
     * 文件大小（字节）。
     */
    private Long fileSize;

    /**
     * 文件 MIME 类型。
     */
    private String contentType;

    /**
     * 文件访问 URL。
     */
    private String fileUrl;

    /**
     * 存储类型：LOCAL/ALIYUN。
     */
    private String storageType;

    /**
     * 状态：ACTIVE（正常）/DELETED（已删除）。
     */
    private String status;

    /**
     * 上传时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 创建上传日志。
     *
     * @param userId           用户ID
     * @param bizType          业务类型
     * @param bizId            业务ID
     * @param originalFilename 原始文件名
     * @param fileSize         文件大小
     * @param contentType      内容类型
     * @param fileUrl          文件URL
     * @param storageType      存储类型
     * @return 上传日志实体
     */
    public static OssUploadLog create(Long userId, String bizType, Long bizId,
                                      String originalFilename, Long fileSize,
                                      String contentType, String fileUrl,
                                      String storageType) {
        OssUploadLog log = new OssUploadLog();
        log.setUserId(userId);
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setOriginalFilename(originalFilename);
        log.setFileSize(fileSize);
        log.setContentType(contentType);
        log.setFileUrl(fileUrl);
        log.setStorageType(storageType);
        log.setStatus(OssConstants.STATUS_ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        log.setCreateTime(now);
        log.setUpdateTime(now);
        return log;
    }

    /**
     * 标记为已删除。
     */
    public void markAsDeleted() {
        this.status = OssConstants.STATUS_DELETED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否已删除。
     *
     * @return 是否已删除
     */
    public boolean isDeleted() {
        return OssConstants.STATUS_DELETED.equals(this.status);
    }

    /**
     * 判断是否激活。
     *
     * @return 是否激活
     */
    public boolean isActive() {
        return OssConstants.STATUS_ACTIVE.equals(this.status);
    }

    /**
     * 判断是否为本地存储。
     *
     * @return 是否为本地存储
     */
    public boolean isLocalStorage() {
        return OssStorageType.LOCAL.name().equals(this.storageType);
    }

    /**
     * 判断是否为阿里云存储。
     *
     * @return 是否为阿里云存储
     */
    public boolean isAliyunStorage() {
        return OssStorageType.ALIYUN.name().equals(this.storageType);
    }

    /**
     * 获取文件扩展名。
     *
     * @return 文件扩展名
     */
    public String getFileExtension() {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 判断文件大小是否超过指定限制。
     *
     * @param maxSize 最大大小（字节）
     * @return 是否超过
     */
    public boolean isFileSizeExceeded(long maxSize) {
        return fileSize != null && fileSize > maxSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OssUploadLog that = (OssUploadLog) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
