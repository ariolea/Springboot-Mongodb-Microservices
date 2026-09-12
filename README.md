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
`id`, `name`, `channel`, `summary`, `genres` y el arreglo `comments` guardado para cada show.
El criterio se acepta tanto en `q` como en `search_query`.

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
    "genres": ["Drama", "Romance"],
    "comments": [
      { "comment": "Muy buena.", "rating": 5 },
      { "comment": "Se cae al final.", "rating": 2 }
    ]
  }
]
```

- `channel` resuelve al nombre de la cadena (`network.name`) y, cuando el show solo existe en
  streaming, al de la plataforma (`webChannel.name`). Es `null` si TVmaze no reporta ninguno.
- `summary` se entrega tal como lo publica TVmaze, incluyendo sus etiquetas HTML.
- `comments` trae los comentarios guardados para ese show; es un arreglo vacio si no hay.
  Los comentarios de todos los shows del resultado se resuelven en una sola consulta a MongoDB.
- Una busqueda sin coincidencias devuelve `200 OK` con un arreglo vacio.

### Obtener un show por id

```
GET /api/v1/shows/{show_id}
```

Devuelve el objeto show completo, con el arreglo `comments` agregado al mismo nivel que el
resto de sus campos. Antes de consumir el API valida la cache en MongoDB
(ver [MongoDB](#mongodb)); solo si el id no esta registrado consulta
`https://api.tvmaze.com/shows/{show_id}` y guarda el resultado.

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
  "_links": { "self": { "href": "https://api.tvmaze.com/shows/1" } },
  "comments": [
    { "comment": "Muy buena.", "rating": 5 }
  ]
}
```

### Registrar una calificacion y un comentario

```
POST /api/v1/comments
```

Guarda en la coleccion `comments` de MongoDB un comentario ligado al id del show y devuelve
el estado de la peticion.

```bash
curl -X POST "http://localhost:8080/api/v1/comments" \
  -H "Content-Type: application/json" \
  -d '{"show_id": 1, "comment": "Excelente serie, el final se siente apresurado.", "rating": 4}'
```

Respuesta `201 Created`:

```json
{
  "status": 201,
  "message": "Comentario registrado para el show 1.",
  "comment_id": "6706f1c0a2e4b0a1c2d3e4f5",
  "show_id": 1,
  "rating": 4,
  "created_at": "2026-09-12T10:15:30.123Z"
}
```

- `show_id` tambien se acepta como `showId`.
- `rating` debe ser un entero entre 0 y 5; fuera de ese rango la peticion responde `400` con el
  motivo exacto y no se escribe nada en la base.
- El comentario se guarda aunque el show todavia no este en la cache: el registro no depende de
  la disponibilidad de TVmaze.

## MongoDB

La consulta de un show usa MongoDB como cache del API de TVmaze. En cada peticion:

1. Se busca el documento con `_id = show_id` en la coleccion `shows`.
2. **Si existe**, se devuelve ese objeto y **no se consume el API de TVmaze**.
3. **Si no existe**, se consulta TVmaze, el resultado se guarda en `shows` y despues se responde.

La respuesta es identica en ambos casos, y el log distingue el camino tomado
(`recuperado de TVmaze y guardado en MongoDB` frente a `servido desde la cache de MongoDB`).

Documento de la coleccion `shows`:

```json
{
  "_id": 1,
  "show": { "id": 1, "name": "Under the Dome", "...": "objeto completo de TVmaze" },
  "cachedAt": "2026-09-12T10:15:30.123Z"
}
```

El `_id` del documento es el id del show, de modo que la cache se consulta por clave primaria
y no puede haber duplicados.

Documento de la coleccion `comments`:

```json
{
  "_id": "6706f1c0a2e4b0a1c2d3e4f5",
  "showId": 1,
  "comment": "Excelente serie, el final se siente apresurado.",
  "rating": 4,
  "createdAt": "2026-09-12T10:15:30.123Z"
}
```

### Conexion

Se configura con la variable `MONGODB_URI`; sin ella se usa
`mongodb://localhost:27017/tvmaze_middleware`. Para MongoDB Atlas:

1. **Database Access**: crea un usuario con rol *Read and write to any database*.
2. **Network Access**: agrega tu IP (o `0.0.0.0/0` para una demo).
3. **Connect → Drivers**: copia la cadena `mongodb+srv://...` y agregale el nombre de la base.

```bash
# Windows (PowerShell)
$env:MONGODB_URI = "mongodb+srv://usuario:password@cluster0.xxxxx.mongodb.net/tvmaze_middleware?retryWrites=true&w=majority"

# Linux / macOS
export MONGODB_URI="mongodb+srv://usuario:password@cluster0.xxxxx.mongodb.net/tvmaze_middleware?retryWrites=true&w=majority"
```

Tambien se puede copiar `.env.example` a `.env`, que esta en `.gitignore`: la cadena de
conexion nunca se escribe en el repositorio.

La aplicacion arranca aunque MongoDB no responda. Las operaciones que necesitan la base
devuelven `503` en unos segundos (`MongoConfig` baja a 5 s la espera del driver, que por
defecto es de 30) y la busqueda de shows, que no toca Mongo, sigue funcionando.

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
| `400 Bad Request` | Criterio de busqueda ausente o vacio; id de show no numerico o no positivo; comentario sin `show_id`/`comment`, con `rating` fuera de 0-5 o con JSON malformado. |
| `404 Not Found` | TVmaze no conoce el `show_id` solicitado. |
| `429 Too Many Requests` | TVmaze rechazo la peticion por su limite de solicitudes. |
| `502 Bad Gateway` | TVmaze respondio con error 5xx, agoto el timeout o devolvio un cuerpo invalido. |
| `503 Service Unavailable` | MongoDB no esta disponible o rechazo la operacion. |
| `500 Internal Server Error` | Falla inesperada del middleware. |

## Configuracion

Valores en `src/main/resources/application.yml`, sobreescribibles por variable de entorno:

| Propiedad | Variable | Valor por defecto |
| --------- | -------- | ----------------- |
| `tvmaze.base-url` | `TVMAZE_BASE_URL` | `https://api.tvmaze.com` |
| `server.port` | `SERVER_PORT` | `8080` |
| `tvmaze.connect-timeout` | — | `3s` |
| `tvmaze.read-timeout` | — | `10s` |
