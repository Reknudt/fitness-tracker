FROM maven:3.9.11-eclipse-temurin-25 AS build

WORKDIR /build

# копируем весь монорепозиторий
COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/user-service/target/*.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]
