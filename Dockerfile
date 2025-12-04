FROM eclipse-temurin:21-jre-alpine
WORKDIR /
ARG JAR_FILE=/build/libs/cloud-storage-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
