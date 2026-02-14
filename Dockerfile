FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

COPY gradlew .

COPY gradle gradle

COPY build.gradle .

COPY settings.gradle .

COPY src src

RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# Copy GeoIP database if it exists
COPY src/main/resources/geoip /app/geoip

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
