FROM maven:3.9.11-eclipse-temurin-25 AS build

WORKDIR /build

# копируем весь монорепозиторий
COPY . .

# Сборка только workout-service (+ собрать parent)
RUN mvn -pl workout-service -am clean package -DskipTests


FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /build/workout-service/target/*.jar app.jar

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "app.jar"]
