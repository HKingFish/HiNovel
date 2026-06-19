package cn.haowl.hinovel.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体基类。
 *
 * <p>包含通用审计字段：id、creator、updater、createTime、updateTime、deleted、version。</p>
 *
 * @author haowl
 * @since 2024
 */
@Data
public abstract class BaseEntity {

    /**
     * 主键 ID（雪花算法生成）。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建人 ID。
     */
    @TableField(fill = FieldFill.INSERT)
    private Long creator;

    /**
     * 更新人 ID。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志。
     *
     * <p>0-正常, 1-已删除。</p>
     */
    @TableLogic
    private Integer deleted;

    /**
     * 乐观锁版本号。
     */
    @Version
    private Integer version;
}
