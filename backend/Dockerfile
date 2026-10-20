FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/backend-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
