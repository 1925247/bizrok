# Multi-stage Dockerfile for BizRok Backend

# Stage 1: Build Stage
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (for caching)
COPY pom.xml .
RUN mvn dependency:resolve-plugins dependency:resolve -Dmaven.repo.local=/root/.m2/repository

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -Dmaven.repo.local=/root/.m2/repository

# Stage 2: Runtime Stage
FROM eclipse-temurin:21-jre-jammy

# Install required system packages
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    libopencv-dev \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs

# Copy the JAR file from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Copy configuration files
COPY src/main/resources/ /app/resources/

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseContainerSupport"

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]