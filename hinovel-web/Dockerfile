# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json .
# 使用淘宝镜像源加速依赖下载
RUN npm config set registry https://registry.npmmirror.com && npm install
COPY . .
RUN npm run build

# 运行阶段（Nginx）
FROM nginx:1.25-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
