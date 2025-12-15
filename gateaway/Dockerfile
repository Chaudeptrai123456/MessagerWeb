# ================================
# Stage 1: Build
# ================================
FROM gradle:8.4.0-jdk21-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper và config trước để cache
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Đảm bảo gradlew có quyền chạy
RUN chmod +x gradlew

# Pre-download dependencies (không chạy test để nhanh hơn)
RUN ./gradlew build -x test --no-daemon || true

# Copy toàn bộ source
COPY src src

# Build jar
RUN ./gradlew bootJar --no-daemon

# ================================
# Stage 2: Runtime
# ================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy jar từ stage build
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
