#Stage 1, build the jar.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#Stage 2, run the jar.
#Only the jar is copied over, so the
#final image has no Maven and no source.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
