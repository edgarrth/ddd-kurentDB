FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN ./mvnw -q -DskipTests package || mvn -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/target/payment-processing-kurrentdb-java25-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
