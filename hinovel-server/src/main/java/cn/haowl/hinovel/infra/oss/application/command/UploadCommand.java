package cn.haowl.hinovel.infra.oss.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传命令。
 *
 * <p>表示文件上传的写操作请求。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadCommand {

    /**
     * 上传的文件。
     */
    private MultipartFile file;

    /**
     * 业务类型。
     */
    private String bizType;

    /**
     * 业务ID。
     */
    private Long bizId;

    /**
     * 用户ID。
     */
    private Long userId;
}
