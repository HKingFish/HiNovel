package cn.haowl.hinovel.infra.oss.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.infra.oss.application.service.OssApplicationService;
import cn.haowl.hinovel.infra.oss.constant.OssConstants;
import cn.haowl.hinovel.infra.oss.domain.command.UploadCommand;
import cn.haowl.hinovel.infra.oss.domain.entity.OssUploadLog;
import cn.haowl.hinovel.infra.oss.domain.query.OssUploadLogQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传接口。
 *
 * 通过 OssProvider 统一处理文件上传，支持本地和阿里云 OSS。
 *
 * @Author : haowl
 * @Date : 2026/3/12 22:30
 */
@Tag(name = "文件管理", description = "文件上传、查询上传记录")
@RestController
@RequestMapping("/api/infra/oss")
public class FileController {

    /**
     * OSS 应用服务。
     */
    @Autowired
    private OssApplicationService ossApplicationService;

    /**
     * 上传文件。
     */
    @Operation(summary = "上传文件", description = "上传文件到 OSS，返回访问 URL，支持 pdf/txt/md/png/jpg/jpeg")
    @PostMapping("/upload")
    public ApiResponse<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", defaultValue = OssConstants.DEFAULT_BIZ_TYPE) String bizType,
            @RequestParam(value = "bizId", required = false) Long bizId) {
        UploadCommand command = UploadCommand.builder()
                .file(file)
                .bizType(bizType)
                .bizId(bizId)
            .userId(StpUtil.getLoginIdAsLong())
                .build();
        return ApiResponse.success(new UploadResponse(ossApplicationService.upload(command)));
    }

    /**
     * 删除文件。
     */
    @Operation(summary = "删除文件", description = "删除指定 URL 的文件")
    @DeleteMapping("/delete")
    public ApiResponse<Void> delete(@RequestParam("fileUrl") String fileUrl) {
        ossApplicationService.delete(fileUrl);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 查询上传记录。
     */
    @Operation(summary = "查询上传记录", description = "按用户和业务类型查询上传记录")
    @GetMapping("/logs")
    public ApiResponse<List<OssUploadLog>> listLogs(
            @RequestParam(value = "bizType", required = false) String bizType) {
        OssUploadLogQuery query = OssUploadLogQuery.builder()
            .userId(StpUtil.getLoginIdAsLong())
            .bizType(bizType)
            .build();
        return ApiResponse.success(ossApplicationService.listUploadLogs(query));
    }

    /**
     * 文件上传响应。
     */
    @Value
    public static class UploadResponse {

        /**
         * 文件访问 URL。
         */
        String url;
    }
}
