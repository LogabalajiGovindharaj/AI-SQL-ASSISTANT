# Build stage: uses an image with Maven preinstalled (no wrapper needed)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Cache dependencies in their own layer before copying source
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B -Dmaven.test.skip=true package

# Runtime stage: slim JRE only, no build tooling
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/assistant-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
