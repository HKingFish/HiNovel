# 运行阶段（Nginx）
# 说明：前端产物 dist/ 由宿主机构建（npm run build），本镜像仅托管静态文件，
#       避免在容器内执行 Vite/Rollup 构建导致 overlay2 IO 瓶颈与内存峰值卡死。
ARG NGINX_IMAGE=nginx:1.25-alpine
FROM ${NGINX_IMAGE}

# 复制宿主机构建好的前端产物到 Nginx 静态目录
COPY dist/ /usr/share/nginx/html/
# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
