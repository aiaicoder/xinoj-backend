# Docker 镜像构建
# @author <a href="https://github.com/liyupi">程序员小新</a>
#
FROM maven:3.8.1-jdk-8-slim as builder

# 定义构建参数、环境变量 JAVA_OPTS 和 PARAMS
ARG VERSION=""
ENV JAVA_OPTS=""
ENV PARAMS=""

# Copy local code to the container image.
COPY ./xinoj-backend-0.0.1-SNAPSHOT.jar /app.jar

# 定义容器启动时执行的入口点
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $PARAMS -jar /app.jar $PARAMS"]