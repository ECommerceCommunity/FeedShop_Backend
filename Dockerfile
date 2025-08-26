# Cloud Run 최적화 Spring Boot Dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 애플리케이션 복사
COPY build/libs/*.jar app.jar

# Cloud Run에서 중요: PORT 환경변수는 시스템이 자동 설정
# Spring Boot가 해당 포트에서 리슨하도록 설정
ENTRYPOINT ["java", \
           "-Dserver.address=0.0.0.0", \
           "-Dserver.port=${PORT:-8080}", \
           "-jar", "app.jar"]
