FROM eclipse-temurin:25-jdk-alpine

ENV TZ=Europe/Berlin
RUN apk add --no-cache ffmpeg curl tzdata \
    && cp /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo ${TZ} > /etc/timezone

WORKDIR /app

COPY target/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl --fail http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]