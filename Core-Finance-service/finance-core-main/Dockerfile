# Multi-stage build for Core Finance Service
# Stage 1: Build stage - Maven compilation
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Copy source code
COPY src ./src

# Build with retry logic for unreliable Maven Central
RUN --mount=type=cache,target=/root/.m2 \
    for attempt in 1 2 3; do \
      echo "Build attempt $attempt..."; \
      mvn clean package -DskipTests -q && break; \
      if [ $attempt -lt 3 ]; then \
        echo "Retrying in 30s..."; \
        sleep 30; \
      else \
        echo "Build failed after 3 attempts"; \
        exit 1; \
      fi; \
    done

# Stage 2: Runtime stage - Lightweight JRE
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -D -s /bin/sh -u 1001 -G appgroup appuser

# Set working directory
WORKDIR /app

# Copy JAR from build stage (renaming to app.jar for consistency)
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check (matches root actuator path)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Start the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
