# ================================
# Stage 1: Build
# ================================
FROM gradle:8.4.0-jdk21-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper và config trước để cache
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Tải dependencies (cache)
RUN ./gradlew build -x test --no-daemon || true

# Copy source code
COPY src src

# Build jar
RUN ./gradlew bootJar --no-daemon

# ================================
# Stage 2: Runtime
# ================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# Nếu cần keys thì mount volume thay vì COPY
# COPY src/main/resources/keys ./keys

EXPOSE 9999

ENTRYPOINT ["java", "-jar", "app.jar"]
