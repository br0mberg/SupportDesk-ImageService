FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /image-service

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY ./src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /image-service

COPY --from=builder /image-service/target/image-service-*.jar ./image-service-app.jar

EXPOSE 8082 50051

ENTRYPOINT ["java", "-jar", "image-service-app.jar"]