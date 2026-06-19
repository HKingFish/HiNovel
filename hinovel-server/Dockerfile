# 多阶段构建：使用 JDK 21 构建，支持虚拟线程
ARG MAVEN_IMAGE=maven:3.9.6-eclipse-temurin-21
ARG JRE_IMAGE=eclipse-temurin:21-jre-alpine
FROM ${MAVEN_IMAGE} AS builder
WORKDIR /app

# 配置阿里云 Maven 镜像源加速依赖下载
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>\n\
<settings>\n\
  <mirrors>\n\
    <mirror>\n\
      <id>aliyun-central</id>\n\
      <mirrorOf>central</mirrorOf>\n\
      <name>阿里云中央仓库镜像</name>\n\
      <url>https://maven.aliyun.com/repository/central</url>\n\
    </mirror>\n\
    <mirror>\n\
      <id>aliyun-public</id>\n\
      <mirrorOf>*</mirrorOf>\n\
      <name>阿里云公共仓库镜像</name>\n\
      <url>https://maven.aliyun.com/repository/public</url>\n\
    </mirror>\n\
  </mirrors>\n\
</settings>' > /root/.m2/settings.xml

COPY pom.xml .
# 预下载依赖（利用 Docker 缓存层加速构建）
RUN mvn dependency:go-offline -B
COPY src/ src/
RUN mvn clean package -DskipTests -B

FROM ${JRE_IMAGE}
WORKDIR /app
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone
# 创建 OSS 本地存储目录和日志目录
RUN mkdir -p /data/hinovel/uploads /app/logs
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Shanghai", "-Dspring.profiles.active=prod", "app.jar"]
