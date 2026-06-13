# Payment Processing with Java 25, DDD, Hexagonal Architecture, DomoActors-style Actors and KurrentDB

Este proyecto es una PoC de **Payment Processing** construida en **Java 25**. Implementa una arquitectura basada en **Domain-Driven Design**, **Arquitectura Hexagonal**, **CQRS**, **Event Sourcing** y un modelo de actores inspirado en DomoActors para procesar comandos de forma secuencial por agregado.

## Objetivo del dominio

El microservicio gestiona el ciclo de vida de un pago:

1. Iniciar un pago.
2. Autorizarlo.
3. Capturarlo.
4. Fallarlo.
5. Cancelarlo.
6. Reembolsarlo.
7. Consultar su estado reconstruido desde eventos.
8. Consultar el historial de eventos.

## Aggregate principal

El agregado raíz es `Payment`.

Estados posibles:

- `NEW`
- `AUTHORIZED`
- `CAPTURED`
- `FAILED`
- `REFUNDED`
- `CANCELLED`

## Comandos

- `InitiatePaymentCommand`
- `AuthorizePaymentCommand`
- `CapturePaymentCommand`
- `FailPaymentCommand`
- `RefundPaymentCommand`
- `CancelPaymentCommand`

## Eventos de dominio

- `PaymentInitiated`
- `PaymentAuthorized`
- `PaymentCaptured`
- `PaymentFailed`
- `PaymentRefunded`
- `PaymentCancelled`

Cada cambio de estado se guarda como evento en KurrentDB. El estado actual del pago no se persiste como una fila tradicional, sino que se reconstruye aplicando su historial de eventos.

## Arquitectura

```text
src/main/java/com/example/payments
├── domain
│   ├── model
│   ├── commands
│   ├── events
│   └── ports
├── application
│   ├── actors
│   └── service
├── infrastructure
│   └── kurrentdb
├── adapters
│   └── rest
└── config
```

## Flujo

```text
REST Controller
    ↓
Application Service
    ↓
Payment Actor
    ↓
Payment Aggregate
    ↓
Domain Events
    ↓
KurrentDB Event Store
```

## Modelo de actores

El proyecto incluye un `PaymentActorSystem` y un `PaymentActor` por `paymentId`. La finalidad es procesar los comandos de un mismo pago de forma secuencial, evitando condiciones de carrera sobre el agregado.

Este enfoque está inspirado en DomoActors, pero adaptado a Java mediante un actor simple basado en `ConcurrentHashMap` y `ReentrantLock`.

## Levantar infraestructura

```bash
cd infraestructura
docker compose up -d
```

KurrentDB quedará disponible en:

```text
http://localhost:2113
```

## Ejecutar el microservicio

```bash
mvn spring-boot:run
```

El servicio queda disponible en:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Endpoints REST

### Iniciar pago

```http
POST /payments/v1/payments
```

Request:

```json
{
  "merchantId": "merchant-001",
  "customerId": "customer-9001",
  "amount": 150.75,
  "currency": "PEN",
  "paymentMethod": "CARD",
  "orderId": "order-2026-0001"
}
```

### Autorizar pago

```http
POST /payments/v1/payments/{paymentId}/authorizations
```

```json
{
  "authorizationCode": "AUTH-123456"
}
```

### Capturar pago

```http
POST /payments/v1/payments/{paymentId}/captures
```

```json
{
  "captureReference": "CAP-987654"
}
```

### Reembolsar pago

```http
POST /payments/v1/payments/{paymentId}/refunds
```

```json
{
  "amount": 50.00,
  "reason": "Customer partial refund request"
}
```

### Cancelar pago

```http
POST /payments/v1/payments/{paymentId}/cancellations
```

```json
{
  "reason": "Customer cancelled order before capture"
}
```

### Consultar pago

```http
GET /payments/v1/payments/{paymentId}
```

### Consultar eventos

```http
GET /payments/v1/payments/{paymentId}/events
```

## Reglas de negocio principales

- Un pago inicia en estado `NEW`.
- Solo un pago `NEW` puede ser autorizado.
- Solo un pago `AUTHORIZED` puede ser capturado.
- Solo un pago `CAPTURED` puede ser reembolsado.
- Un pago `NEW` o `AUTHORIZED` puede ser cancelado.
- Un pago `CAPTURED` o `REFUNDED` no puede fallarse.

## Configuración

Archivo:

```text
src/main/resources/application.yml
```

```yaml
server:
  port: 8080

spring:
  application:
    name: payment-processing-kurrentdb-java25

kurrentdb:
  connection-string: kurrentdb://localhost:2113?tls=false
```

## Notas

Esta PoC usa Payment Processing como dominio de ejemplo. El mismo patrón puede aplicarse a dominios fintech como transferencias, billeteras digitales, créditos, conciliación, reversas o liquidación de pagos.
