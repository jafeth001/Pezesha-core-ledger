# Multi-stage Dockerfile
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy sources and build
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src
RUN mvn -B -e -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar /app/app.jar"]
