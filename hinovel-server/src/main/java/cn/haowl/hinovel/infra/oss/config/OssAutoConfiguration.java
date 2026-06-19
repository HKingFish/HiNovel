package cn.haowl.hinovel.infra.oss.config;

import cn.haowl.hinovel.infra.oss.provider.AliyunOssProvider;
import cn.haowl.hinovel.infra.oss.provider.LocalOssProvider;
import cn.haowl.hinovel.infra.oss.provider.OssProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * OSS 自动配置类。
 *
 * <p>根据 hinovel.oss.type 配置项，按条件激活对应的 OssProvider 实现。</p>
 *
 * @author haowl
 * @since 2024
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssAutoConfiguration {

    /**
     * 本地存储实现（默认，hinovel.oss.type=LOCAL 时激活）。
     *
     * @param ossProperties OSS 配置属性
     * @return LocalOssProvider 实例
     */
    @Bean
    @ConditionalOnMissingBean(OssProvider.class)
    @ConditionalOnProperty(prefix = "hinovel.oss", name = "type", havingValue = "LOCAL", matchIfMissing = true)
    public OssProvider localOssProvider(OssProperties ossProperties) {
        return new LocalOssProvider(ossProperties);
    }

    /**
     * 阿里云 OSS 实现（hinovel.oss.type=ALIYUN 时激活）。
     *
     * @param ossProperties OSS 配置属性
     * @return AliyunOssProvider 实例
     */
    @Bean
    @ConditionalOnMissingBean(OssProvider.class)
    @ConditionalOnProperty(prefix = "hinovel.oss", name = "type", havingValue = "ALIYUN")
    public OssProvider aliyunOssProvider(OssProperties ossProperties) {
        return new AliyunOssProvider(ossProperties);
    }

    /**
     * 本地存储静态资源映射配置。
     *
     * <p>将 /files/** 请求映射到本地存储目录，仅 LOCAL 模式下生效。</p>
     *
     * @param ossProperties OSS 配置属性
     * @return WebMvcConfigurer 实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "hinovel.oss", name = "type", havingValue = "LOCAL", matchIfMissing = true)
    public WebMvcConfigurer localFileResourceConfigurer(OssProperties ossProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String basePath = ossProperties.getLocal().getBasePath();
                registry.addResourceHandler("/files/**")
                        .addResourceLocations("file:" + basePath + "/");
            }
        };
    }
}
