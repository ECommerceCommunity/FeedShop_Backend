# Multi-stage build for optimized production image
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace/app

# Copy gradle wrapper and dependency files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Grant execute permission and build application
RUN chmod +x gradlew
RUN ./gradlew clean build -x test --no-daemon

# Extract JAR layers for better caching
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

# Production stage
FROM eclipse-temurin:17-jre-alpine

# Add non-root user for security
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy application layers from builder stage
COPY --from=builder --chown=spring:spring /workspace/app/build/dependency/BOOT-INF/lib /app/lib
COPY --from=builder --chown=spring:spring /workspace/app/build/dependency/META-INF /app/META-INF
COPY --from=builder --chown=spring:spring /workspace/app/build/dependency/BOOT-INF/classes /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp app:app/lib/* com.cMall.feedShop.FeedShopApplication"]