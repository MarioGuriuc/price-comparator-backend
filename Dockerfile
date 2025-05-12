FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package

FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

COPY --from=build /app/target/app.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
