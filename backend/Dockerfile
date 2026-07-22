# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Cache dependencies first so source-only changes don't re-download the world.
COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline
COPY src ./src
RUN mvn -q -e -B clean package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/erp-backend-0.1.0.jar app.jar
EXPOSE 8080
# Prod profile is supplied via SPRING_PROFILES_ACTIVE in docker-compose.
ENTRYPOINT ["java", "-jar", "app.jar"]
