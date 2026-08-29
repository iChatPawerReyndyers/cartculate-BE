# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .

# Install dos2unix to purge Windows CRLF line endings from the wrapper script
RUN apt-get update && apt-get install -y dos2unix && \
    dos2unix mvnw && \
    chmod +x mvnw && \
    ./mvnw clean package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
