package cn.haowl.hinovel.infra.oss.application.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS 上传日志查询条件。
 *
 * <p>表示上传日志查询的读操作请求。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssUploadLogQuery {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 业务类型。
     */
    private String bizType;

    /**
     * 业务ID。
     */
    private Long bizId;
}
