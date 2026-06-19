package cn.haowl.hinovel.common.handler;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.constant.CommonConstants;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 审计字段自动填充处理器。
 *
 * <p>自动填充 creator、updater、createTime、updateTime、deleted 字段。</p>
 *
 * @author haowl
 * @since 2024
 */
@Slf4j
@Component
public class HiNovelMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Long currentUserId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        this.strictInsertFill(metaObject, "creator", Long.class, currentUserId);
        this.strictInsertFill(metaObject, "updater", Long.class, currentUserId);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "deleted", Integer.class, CommonConstants.DELETED_FALSE);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long currentUserId = getCurrentUserId();
        this.strictUpdateFill(metaObject, "updater", Long.class, currentUserId);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 获取当前登录用户 ID。
     *
     * <p>未登录时返回系统默认 ID。</p>
     *
     * @return 当前用户 ID 或系统默认 ID
     */
    private Long getCurrentUserId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            log.debug("获取当前用户 ID 失败，使用系统默认 ID: {}", e.getMessage());
        }
        return CommonConstants.SYSTEM_USER_ID;
    }
}
