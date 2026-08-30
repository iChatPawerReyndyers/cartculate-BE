# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
# Use the global maven tool inside the container directly
RUN mvn clean package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
