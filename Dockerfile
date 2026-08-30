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
# Update this line to match your application's runtime port
EXPOSE 8888
# Restricts Java heap to 70% of available container RAM to prevent OOM crashes
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-jar", "app.jar"]