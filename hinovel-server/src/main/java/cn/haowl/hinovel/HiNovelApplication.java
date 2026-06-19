package cn.haowl.hinovel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * HiNovel AI 小说创作平台启动入口
 * MapperScan 覆盖所有子模块的 Mapper 包，确保多模块下 MyBatis 正常工作
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan("cn.haowl.hinovel.**.mapper")
public class HiNovelApplication {

    public static void main(String[] args) {
        SpringApplication.run(HiNovelApplication.class, args);
    }
}