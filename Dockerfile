FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/assignment-1.0.0.jar app.jar
CMD ["java", "-jar", "app.jar"]