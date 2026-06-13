#  Proyecto

El bounded context es `AgilePM`. El agregado principal es `Product`, modelado como event-sourced aggregate.

Comandos implementados:

- `initiate(...)`
- `changeDescription(...)`
- `changeProductOwner(...)`
- `requestDiscussion()`
- `attachDiscussion(...)`
- `timeOutDiscussionRequest()`
- `planSprint(...)`
- `scheduleRelease(...)`

Eventos persistidos en KurrentDB:

- `ProductInitiated`
- `ProductDescriptionChanged`
- `ProductOwnerChanged`
- `ProductDiscussionRequested`
- `ProductDiscussionAttached`
- `ProductDiscussionRequestTimedOut`
- `SprintPlanned`
- `ReleaseScheduled`

## Arquitectura

```text
REST Controller
    |
    v
Application Ports
    |-- ProductCommands  -> actor protocol command-side
    |-- ProductQueries   -> read-side query protocol
    |
    v
ProductActor
    |
    v
Domain Aggregate: Product
    |
    v
EventStreamStore port
    |
    v
KurrentEventStreamStore adapter
    |
    v
KurrentDB
```

## Estructura

```text
.
├── infraestructura
│   ├── docker
│   │   ├── Dockerfile
│   │   └── docker-compose.yml
│   └── http
│       ├── requests.http
│       └── responses.md
├── src/main/java/com/example/agilepm
│   ├── adapter
│   │   ├── in/rest
│   │   └── out/kurrentdb
│   ├── application
│   │   ├── actor
│   │   ├── port/in
│   │   ├── port/out
│   │   └── service
│   ├── config
│   └── domain
│       ├── event
│       └── model
└── src/main/resources/application.yml
```

## Requisitos

- Java 25
- Maven 3.9+
- Docker / Docker Compose

## Levantar infraestructura y API

Desde la raíz del proyecto:

```bash
docker compose -f infraestructura/docker/docker-compose.yml up --build
```

La API quedará disponible en:

```text
http://localhost:8080
```

KurrentDB quedará disponible en:

```text
http://localhost:2113
```

## Ejecutar local sin Docker para la API

Primero levanta solo KurrentDB:

```bash
docker compose -f infraestructura/docker/docker-compose.yml up kurrentdb
```

Luego ejecuta la aplicación:

```bash
mvn spring-boot:run
```

`application.yml` usa esta conexión por defecto:

```yaml
kurrentdb:
  connection-string: ${KURRENTDB_CONNECTION_STRING:kurrentdb://localhost:2113?tls=false}
```

## Endpoints REST

### Iniciar producto

```http
POST /agilepm/v1/tenants/{tenantId}/products
```

```json
{
  "productId": "product-001",
  "name": "AgilePM Platform",
  "description": "Product used to manage Scrum planning",
  "productOwnerId": "owner-001"
}
```

### Cambiar descripción

```http
PATCH /agilepm/v1/tenants/{tenantId}/products/{productId}/description
```

```json
{
  "description": "New product description"
}
```

### Cambiar product owner

```http
PATCH /agilepm/v1/tenants/{tenantId}/products/{productId}/owner
```

```json
{
  "productOwnerId": "owner-002"
}
```

### Solicitar discusión

```http
POST /agilepm/v1/tenants/{tenantId}/products/{productId}/discussion-requests
```

### Adjuntar discusión

```http
PUT /agilepm/v1/tenants/{tenantId}/products/{productId}/discussion
```

```json
{
  "discussionId": "discussion-abc"
}
```

### Planificar sprint

```http
POST /agilepm/v1/tenants/{tenantId}/products/{productId}/sprints
```

```json
{
  "sprintId": "sprint-001",
  "name": "Sprint 1",
  "startsOn": "2026-07-01",
  "endsOn": "2026-07-15"
}
```

### Programar release

```http
POST /agilepm/v1/tenants/{tenantId}/products/{productId}/releases
```

```json
{
  "releaseId": "release-2026-q3",
  "name": "Q3 Release",
  "scheduledFor": "2026-09-30"
}
```

### Consultar read model

```http
GET /agilepm/v1/tenants/{tenantId}/products/{productId}
```

## Decisiones de diseño

### DDD

- `Product` es un agregado con reglas de negocio y estado privado.
- Los cambios del agregado se expresan como hechos de dominio, no como updates SQL.
- La reconstitución del agregado se hace aplicando el stream de eventos.

### CQRS

- `ProductCommands` contiene solo comandos.
- `ProductQueries` contiene solo consultas.
- El read model es separado y está representado por `ProductView`.

### Event Sourcing

- Cada instancia de `Product` usa un stream propio:

```text
agilepm-product-{tenantId}-{productId}
```

- KurrentDB persiste los eventos en orden.
- La concurrencia se controla con expected revision.

### Actores estilo DomoActors

- Cada `ProductActor` representa una instancia lógica de agregado.
- El actor procesa mensajes uno por uno desde su mailbox.
- Esto evita race conditions sobre el agregado, siguiendo la idea del artículo: protocolo tipado, mensajes secuenciales y estado protegido.
