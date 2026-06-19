package cn.haowl.hinovel.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author : haowl
 * @Date : 2026/3/13 22:13
 * @Desc :
 */
@Data
@Component
@ConfigurationProperties("hinovel.ai")
public class AiProperties {

    // TODO : 配置
    private Qdrant qdrant = new Qdrant();

    @Data
    public static class Qdrant {
        /**
         * Qdrant gRPC 主机地址
         */
        private String host = "localhost";
        /**
         * Qdrant gRPC 端口
         */
        private int port = 6334;
    }


}