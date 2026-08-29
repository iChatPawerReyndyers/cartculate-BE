# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
# Grant execution permissions and use the wrapper script instead
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
