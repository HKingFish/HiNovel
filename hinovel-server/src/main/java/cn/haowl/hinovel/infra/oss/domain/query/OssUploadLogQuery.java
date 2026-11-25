package cn.haowl.hinovel.infra.oss.domain.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS 上传日志查询条件。
 *
 * 表示上传日志查询的读操作请求。
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
     * 上传用户 ID。
     */
    private Long userId;

    /**
     * 业务类型。
     */
    private String bizType;
}
