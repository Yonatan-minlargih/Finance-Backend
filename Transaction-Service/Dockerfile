FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
# Download dependencies for offline building
RUN mvn dependency:go-offline

COPY src ./src
# Need to skip tests when building quickly
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
WORKDIR /app

COPY --from=builder /build/target/transactional-service-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8082
EXPOSE 8082

ENTRYPOINT ["java","-jar","/app/app.jar"]
