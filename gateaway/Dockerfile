# ================================
# Stage 1: Build với Gradle
# ================================
FROM gradle:8.14.3-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# ================================
# Stage 2: Runtime
# ================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file jar đã build
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy thư mục keys vào /app/keys

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
