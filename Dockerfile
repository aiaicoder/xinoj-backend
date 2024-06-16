# Docker 镜像构建
# @author <a href="https://github.com/liyupi">程序员小新</a>
#
FROM openjdk:8-jdk-alpine

# 定义构建参数、环境变量 JAVA_OPTS 和 PARAMS
ARG VERSION=""
ENV JAVA_OPTS=""
ENV PARAMS=""

# 安装 tzdata 包以支持时区设置
RUN apk add --no-cache tzdata

# 设置容器的时区为 Asia/Shanghai，并将其复制到 /etc/localtime 文件，同时设置时区信息到 /etc/timezone 文件
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

# Copy local code to the container image.
COPY ./xinoj-backend-0.0.1-SNAPSHOT.jar /app.jar

# 定义容器启动时执行的入口点
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $PARAMS -jar /app.jar $PARAMS"]