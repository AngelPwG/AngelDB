FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /run_data

COPY --from=builder /build/target/*.jar /app/angeldb.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/angeldb.jar"]