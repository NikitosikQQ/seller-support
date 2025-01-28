FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml ./

RUN mvn -B dependency:resolve

COPY src ./src

RUN mvn -B -f pom.xml clean install -DskipTests -Dcheckstyle.skip

FROM openjdk:21-jdk

WORKDIR /app

COPY --from=builder /app/target/assignment-1.0.0.jar app.jar

# Указываем команду запуска
CMD ["java", "-jar", "app.jar"]
