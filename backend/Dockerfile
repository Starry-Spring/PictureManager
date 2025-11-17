# 构建阶段
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app

# 复制pom文件并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:17-jdk-slim
WORKDIR /app

# 安装必要的工具
RUN apt-get update && apt-get install -y --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

# 创建非root用户（安全考虑）
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

# 复制构建的jar包
COPY --from=build /app/target/*.jar app.jar

# 创建上传目录
USER root
RUN mkdir -p /app/uploads && chown spring:spring /app/uploads
USER spring

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]