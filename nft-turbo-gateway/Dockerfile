# 使用 eclipse-temurin:21-jdk-jammy
FROM eclipse-temurin:21-jdk-jammy

# 暴露端口 8081
EXPOSE 8481
RUN mkdir -p /app
COPY src/main/resources/logback-spring.xml /app
COPY target/nft-turbo-gateway.jar /app
COPY src/main/resources/application.yml /app
#COPY init.sh /app
WORKDIR /app

# 设置环境变量 JAVA_OPTS，包含时区、内存、日志、管理端点等配置
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.net.preferIPv4Stack=true  -XX:+UseContainerSupport -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# 定义容器启动时执行的命令
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/prod/./urandom  -jar /app/nft-turbo-gateway.jar  --logging.config=/app/logback-spring.xml --server.port=8481"  ]
