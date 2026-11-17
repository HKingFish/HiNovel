package cn.haowl.hinovel.infra.oss.domain.entity;

import cn.haowl.hinovel.infra.oss.constant.OssStorageTypeEnum;
import cn.haowl.hinovel.infra.oss.constant.OssUploadStatusEnum;
import cn.haowl.hinovel.infra.oss.util.OssFileUtils;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.EnumUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * OSS 上传日志实体。
 *
 * 记录每次文件上传的元数据，支持按用户和业务类型查询。
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
     * 文件存储类型。
     *
     * @see OssStorageTypeEnum
     */
    private String storageType;

    /**
     * 上传记录状态。
     *
     * @see OssUploadStatusEnum
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
     * 标记为已删除。
     */
    public void markAsDeleted() {
        this.status = OssUploadStatusEnum.DELETED.name();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否已删除。
     *
     * @return 是否已删除
     */
    public boolean isDeleted() {
        return OssUploadStatusEnum.DELETED == EnumUtils.getEnum(OssUploadStatusEnum.class, this.status);
    }

    /**
     * 判断是否激活。
     *
     * @return 是否激活
     */
    public boolean isActive() {
        return OssUploadStatusEnum.ACTIVE == EnumUtils.getEnum(OssUploadStatusEnum.class, this.status);
    }

    /**
     * 判断是否为本地存储。
     *
     * @return 是否为本地存储
     */
    public boolean isLocalStorage() {
        return OssStorageTypeEnum.LOCAL == EnumUtils.getEnum(OssStorageTypeEnum.class, this.storageType);
    }

    /**
     * 判断是否为阿里云存储。
     *
     * @return 是否为阿里云存储
     */
    public boolean isAliyunStorage() {
        return OssStorageTypeEnum.ALIYUN == EnumUtils.getEnum(OssStorageTypeEnum.class, this.storageType);
    }

    /**
     * 获取文件扩展名。
     *
     * @return 文件扩展名
     */
    public String getFileExtension() {
        return OssFileUtils.getExtension(originalFilename, "");
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
