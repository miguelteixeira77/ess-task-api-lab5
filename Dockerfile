# ---------- Stage 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

# Gera o fat-jar com manifest Main-Class
RUN mvn -DskipTests package assembly:single

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/target/*jar-with-dependencies.jar app.jar
COPY cert.pem /app/cert.pem
COPY key.pem /app/key.pem

EXPOSE 80 443
ENTRYPOINT ["java", "-jar", "app.jar"]
