package cn.haowl.hinovel.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author : haowl
 * @date : 2026/3/13 22:13
 * @desc : AI 模块配置
 */
@Data
@Component
@ConfigurationProperties("hinovel.ai")
public class AiProperties {

    /**
     * Qdrant 连接配置。
     */
    private Qdrant qdrant = new Qdrant();

    /**
     * Qdrant gRPC 连接配置。
     */
    @Data
    public static class Qdrant {

        /**
         * 默认 gRPC 端口。
         */
        private static final int DEFAULT_GRPC_PORT = 6334;

        /**
         * Qdrant gRPC 主机地址
         */
        private String host = "localhost";

        /**
         * Qdrant gRPC 端口
         */
        private int port = DEFAULT_GRPC_PORT;
    }
}
