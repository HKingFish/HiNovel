# 构建阶段
ARG NODE_IMAGE=node:20-alpine
ARG NGINX_IMAGE=nginx:1.25-alpine
FROM ${NODE_IMAGE} AS builder
WORKDIR /app
COPY package.json .

RUN npm install

COPY . .
RUN npm run build

# 运行阶段（Nginx）
FROM ${NGINX_IMAGE}
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
