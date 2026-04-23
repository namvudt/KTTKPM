# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Sao chép pom.xml và source code
COPY pom.xml .
COPY src ./src

# Build ứng dụng, bỏ qua test để tăng tốc độ
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Sao chép file jar từ stage build sang stage run
COPY --from=build /app/target/patient-service-1.0.0.jar app.jar

# Expose port mặc định của Spring Boot
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
