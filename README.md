# TVmaze Middleware API

API middleware construido con **Spring Boot 3.3** sobre el API publico de
[TVmaze](https://www.tvmaze.com/api).

## Requisitos

Basta con un **JDK 17 o superior**: el proyecto incluye el Maven Wrapper, que descarga
Maven por su cuenta. Alternativamente, Docker compila y ejecuta todo sin instalar nada.

## Como ejecutar

```bash
./mvnw spring-boot:run      # Linux / macOS
mvnw.cmd spring-boot:run    # Windows
```

Con Docker:

```bash
docker build -t tvmaze-middleware .
docker run --rm -p 8080:8080 tvmaze-middleware
```

La aplicacion queda disponible en `http://localhost:8080`.

- Documentacion interactiva (Swagger UI): <http://localhost:8080/swagger-ui.html>
- Contrato OpenAPI: <http://localhost:8080/v3/api-docs>
- Health check: <http://localhost:8080/actuator/health>

## Pruebas

```bash
./mvnw test
```

## Configuracion

Valores en `src/main/resources/application.yml`, sobreescribibles por variable de entorno:

| Propiedad | Variable | Valor por defecto |
| --------- | -------- | ----------------- |
| `tvmaze.base-url` | `TVMAZE_BASE_URL` | `https://api.tvmaze.com` |
| `server.port` | `SERVER_PORT` | `8080` |
| `tvmaze.connect-timeout` | — | `3s` |
| `tvmaze.read-timeout` | — | `10s` |
