package cn.haowl.hinovel.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * Web MVC 公共配置
 * 包含国际化、TraceId 响应头注入、HTTP 请求日志拦截器注册
 */
@Configuration
@RequiredArgsConstructor
public class I18nConfig implements WebMvcConfigurer {


    /**
     * 配置消息源，加载中英文资源文件
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("i18n/messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(false);
        source.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return source;
    }

    /**
     * 基于 Accept-Language 请求头解析语言环境，默认中文
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return resolver;
    }

//    /**
//     * 注册拦截器：
//     * 1. LocaleChangeInterceptor：支持通过 lang 参数切换语言
//     * 2. TraceIdResponseInterceptor：将 TLog TraceId 注入响应头
//     * 3. HttpLogInterceptor：异步记录 HTTP 请求日志
//     */
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
//        localeInterceptor.setParamName("lang");
//        registry.addInterceptor(localeInterceptor);
//
//        registry.addInterceptor(new TraceIdResponseInterceptor());
//
//        registry.addInterceptor(httpLogInterceptor);
//    }
}
