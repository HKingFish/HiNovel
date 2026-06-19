package cn.haowl.hinovel.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 3 接口文档配置
 * 访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class OpenApiConfig {

    /**
     * Sa-Token Token 请求头名称
     */
    private static final String SECURITY_SCHEME_NAME = "satoken";

    @Bean
    public OpenAPI hiNovelOpenApi() {
        return new OpenAPI()
                .info(buildApiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme())
                );
    }

    private Info buildApiInfo() {
        return new Info()
                .title("HiNOVEL 智能体平台 API")
                .description("HiNOVEL 智能体平台后端接口文档，基于 Sa-Token 认证，支持 Agent 管理、对话、MCP 工具调用等功能")
                .version("1.0.0")
                .contact(new Contact()
                        .name("HiNovel Contributors")
                        .url("https://github.com/HKingFish/HiNovel/issues")
                );
    }

    /**
     * 配置 Bearer Token 认证方案（Sa-Token 默认使用请求头 satoken 传递 Token）
     */
    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("satoken")
                .description("Sa-Token 登录凭证，登录后从响应中获取 accessToken 填入此处");
    }
}
