FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY scripts/wait-for-it.sh .
RUN chmod +x wait-for-it.sh
EXPOSE 8080
ENTRYPOINT ["./wait-for-it.sh", "postgres:5432", "--", "java", "-jar", "app.jar"]
