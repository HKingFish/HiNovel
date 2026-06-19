package cn.haowl.hinovel.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 扩展配置
 * 注册本地 OSS 静态资源映射，使 /files/** 能直接访问磁盘文件
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 本地 OSS 存储根目录，从配置文件读取，避免跨模块依赖
     */
    @Value("${hinovel.oss.local.base-path:./data/hinovel/uploads}")
    private String ossLocalBasePath;

    /**
     * 将 /files/** 映射到本地 OSS 存储目录
     * 访问示例：http://localhost:8080/files/knowledge/xxx.txt
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以 / 结尾，file: 协议要求路径末尾有斜杠
        String resourceLocation = "file:" + ossLocalBasePath.replace("\\", "/");
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }
        registry.addResourceHandler("/files/**")
                .addResourceLocations(resourceLocation);
    }
}
