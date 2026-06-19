package cn.haowl.hinovel.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * 环境变量校验器。
 *
 * <p>在 Spring 容器初始化前校验关键环境变量是否已配置，
 * 缺失则直接终止启动（fail fast），避免带默认值硬跑导致线上事故。</p>
 *
 * @author haowl
 */
@Slf4j
public class EnvConfigValidator implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    /** 必须配置的环境变量列表（缺失任意一个则启动失败） */
    private static final String[] REQUIRED_ENV_KEYS = {
            "MYSQL_ROOT_PASSWORD",
            "API_KEY_SECRET"
    };

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();
        List<String> missingKeys = new ArrayList<>();

        for (String key : REQUIRED_ENV_KEYS) {
            String value = env.getProperty(key);
            if (value == null || value.isBlank()) {
                missingKeys.add(key);
            }
        }

        if (!missingKeys.isEmpty()) {
            String errorMessage = String.format(
                    "关键环境变量缺失，应用启动终止。缺失项：%s。请在 .env 文件或系统环境变量中配置后重试。",
                    String.join(", ", missingKeys)
            );
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        log.info("环境变量校验通过，所有必需配置项已就绪");
    }
}
