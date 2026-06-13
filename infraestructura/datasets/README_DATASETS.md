# Carga de datos de prueba - Payment Processing

Esta carpeta contiene datasets y un script para generar eventos de prueba en KurrentDB usando la API REST del microservicio.

## Archivos

```text
infraestructura/datasets/
├── merchants.json
├── payments.json
├── load-test-data.sh
└── README_DATASETS.md
```

## Importante

Esta PoC usa Event Sourcing, por lo tanto no se insertan registros directamente en una base de datos. Los datos de prueba se cargan ejecutando comandos REST que generan eventos de dominio en KurrentDB.

Ejemplo de eventos generados:

```text
PaymentInitiated
PaymentAuthorized
PaymentCaptured
PaymentRefunded
PaymentFailed
PaymentCancelled
```

## Prerrequisitos

Debes tener instalado:

```bash
docker
maven
java 25
curl
python3
```

## 1. Levantar KurrentDB

Desde la raíz del proyecto:

```bash
cd infraestructura
docker compose up -d
```

Validar que KurrentDB esté activo:

```bash
docker ps
```

La consola de KurrentDB queda disponible en:

```text
http://localhost:2113
```

## 2. Levantar el microservicio

Desde la raíz del proyecto:

```bash
mvn spring-boot:run
```

El servicio queda disponible en:

```text
http://localhost:8080
```

Swagger queda disponible en:

```text
http://localhost:8080/swagger-ui.html
```

## 3. Ejecutar la carga de datos

Desde la raíz del proyecto:

```bash
chmod +x infraestructura/datasets/load-test-data.sh
./infraestructura/datasets/load-test-data.sh
```

El script leerá el archivo:

```text
infraestructura/datasets/payments.json
```

y ejecutará los endpoints REST necesarios para crear pagos con distintos escenarios.

## Escenarios incluidos

| Escenario | Flujo que ejecuta | Eventos generados |
|---|---|---|
| `AUTHORIZED` | Inicia y autoriza el pago | `PaymentInitiated`, `PaymentAuthorized` |
| `CAPTURED` | Inicia, autoriza y captura el pago | `PaymentInitiated`, `PaymentAuthorized`, `PaymentCaptured` |
| `CAPTURED_WITH_REFUND` | Inicia, autoriza, captura y reembolsa parcialmente | `PaymentInitiated`, `PaymentAuthorized`, `PaymentCaptured`, `PaymentRefunded` |
| `FAILED` | Inicia el pago y lo marca como fallido | `PaymentInitiated`, `PaymentFailed` |
| `CANCELLED` | Inicia el pago y lo cancela | `PaymentInitiated`, `PaymentCancelled` |

## 4. Usar una URL diferente

Si el microservicio corre en otro host o puerto:

```bash
API_BASE_URL=http://localhost:9090 ./infraestructura/datasets/load-test-data.sh
```

## 5. Usar otro archivo de dataset

Puedes crear otro archivo JSON con el mismo formato de `payments.json` y ejecutarlo así:

```bash
DATASET_FILE=infraestructura/datasets/my-payments.json ./infraestructura/datasets/load-test-data.sh
```

## 6. Consultar pagos generados

El script imprime en consola los `paymentId` creados.

Con un `paymentId`, puedes consultar el estado actual reconstruido desde eventos:

```bash
curl http://localhost:8080/payments/v1/payments/{paymentId}
```

También puedes consultar el historial de eventos:

```bash
curl http://localhost:8080/payments/v1/payments/{paymentId}/events
```

## 7. Validar en KurrentDB

Ingresa a:

```text
http://localhost:2113
```

Busca streams con nombres similares a:

```text
payment-{paymentId}
```

Cada stream representa el historial event-sourced de un pago.

## Ejemplo de ejecución esperada

```text
Loading 10 payment scenarios from infraestructura/datasets/payments.json into http://localhost:8080
Created payment 6d51a2bc-7e8e-4b74-9de5-561f6e7e2f54 scenario=CAPTURED
Created payment 12fdcb47-0fe5-4c93-b801-5ef2264950fd scenario=AUTHORIZED
Created payment 5b7f6e99-d6d7-4a52-a87b-20fc9b67e3d9 scenario=CAPTURED_WITH_REFUND
Done. Loaded 10 payment scenarios.
Open KurrentDB UI: http://localhost:2113
```
