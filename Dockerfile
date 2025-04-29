# ----------------------------
# BUILD STAGE: Build Java app
# ----------------------------
FROM maven:3.9.9-amazoncorretto-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# --------------------------------------
# RUNTIME STAGE: Use Playwright Java image
# --------------------------------------
FROM mcr.microsoft.com/playwright/java:v1.32.0-focal

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]