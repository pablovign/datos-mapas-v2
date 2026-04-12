# Fase 1: Compilación usando Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Fase 2: Ejecución usando Java 21
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Agregamos una variable para que Spring escuche en el puerto de Render
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]