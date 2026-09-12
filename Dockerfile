# Etapa de compilacion: no requiere JDK ni Maven instalados en la maquina anfitriona.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

# Etapa de ejecucion: solo el JRE y el jar resultante.
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --create-home --shell /bin/bash spring
USER spring

COPY --from=build /workspace/target/tvmaze-middleware-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
