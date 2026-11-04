# ==============================================================================
# Multi-stage Dockerfile for VMS Spring Boot Backend on Render
# ==============================================================================

# Stage 1: Build the application using Maven and OpenJDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependency layer
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and package jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal JRE runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/vms_backend-0.0.1-SNAPSHOT.jar app.jar

# Render injects PORT automatically (defaults to 8080 locally)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]

