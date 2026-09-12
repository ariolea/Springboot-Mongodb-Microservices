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

## Endpoints

### Buscar shows por criterio

```
GET /api/v1/shows/search?q={search_query}
```

Consulta `http://api.tvmaze.com/search/shows?q=query` y devuelve un arreglo de shows con
`id`, `name`, `channel`, `summary` y `genres`. El criterio se acepta tanto en `q` como en
`search_query`.

```bash
curl "http://localhost:8080/api/v1/shows/search?q=girls"
```

```json
[
  {
    "id": 139,
    "name": "Girls",
    "channel": "HBO",
    "summary": "<p>This Emmy winning series is a comic look at the assorted humiliations and rare triumphs of a group of girls in their 20s.</p>",
    "genres": ["Drama", "Romance"]
  }
]
```

- `channel` resuelve al nombre de la cadena (`network.name`) y, cuando el show solo existe en
  streaming, al de la plataforma (`webChannel.name`). Es `null` si TVmaze no reporta ninguno.
- `summary` se entrega tal como lo publica TVmaze, incluyendo sus etiquetas HTML.
- Una busqueda sin coincidencias devuelve `200 OK` con un arreglo vacio.

### Obtener un show por id

```
GET /api/v1/shows/{show_id}
```

Consulta `https://api.tvmaze.com/shows/{show_id}` y devuelve el objeto show completo.

```bash
curl "http://localhost:8080/api/v1/shows/1"
```

```json
{
  "id": 1,
  "url": "https://www.tvmaze.com/shows/1/under-the-dome",
  "name": "Under the Dome",
  "type": "Scripted",
  "language": "English",
  "genres": ["Drama", "Science-Fiction", "Thriller"],
  "status": "Ended",
  "runtime": 60,
  "schedule": { "time": "22:00", "days": ["Thursday"] },
  "rating": { "average": 6.6 },
  "network": { "id": 2, "name": "CBS", "country": { "name": "United States", "code": "US", "timezone": "America/New_York" } },
  "externals": { "tvrage": 25988, "thetvdb": 264492, "imdb": "tt1553656" },
  "summary": "<p><b>Under the Dome</b> is the story of a small town...</p>",
  "_links": { "self": { "href": "https://api.tvmaze.com/shows/1" } }
}
```

## Manejo de errores

Todos los errores comparten el mismo cuerpo:

```json
{
  "timestamp": "2026-09-12T10:15:30.123-06:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El criterio de busqueda es obligatorio. Envielo en el parametro q o search_query.",
  "path": "/api/v1/shows/search"
}
```

| Codigo | Cuando ocurre |
| ------ | ------------- |
| `400 Bad Request` | Criterio de busqueda ausente o vacio; id de show no numerico o no positivo. |
| `404 Not Found` | TVmaze no conoce el `show_id` solicitado. |
| `429 Too Many Requests` | TVmaze rechazo la peticion por su limite de solicitudes. |
| `502 Bad Gateway` | TVmaze respondio con error 5xx, agoto el timeout o devolvio un cuerpo invalido. |
| `500 Internal Server Error` | Falla inesperada del middleware. |

## Configuracion

Valores en `src/main/resources/application.yml`, sobreescribibles por variable de entorno:

| Propiedad | Variable | Valor por defecto |
| --------- | -------- | ----------------- |
| `tvmaze.base-url` | `TVMAZE_BASE_URL` | `https://api.tvmaze.com` |
| `server.port` | `SERVER_PORT` | `8080` |
| `tvmaze.connect-timeout` | — | `3s` |
| `tvmaze.read-timeout` | — | `10s` |
