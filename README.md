# API Design & Observability (Spring Boot 3)

A small orders API that shows the details reviewers look for in a production-ready REST service.

| Concern | Implementation |
|---------|----------------|
| Versioned resources | `/api/v1/orders` |
| Correct status codes | `201 Created` + `Location`, `404`, `400` |
| Input validation | Bean Validation on a `record` request |
| Consistent errors | RFC 7807 `ProblemDetail` from a `@RestControllerAdvice` |
| Correlation id | `CorrelationIdFilter`: reads/creates `X-Correlation-Id`, puts it in the MDC and the response |
| Metrics | Micrometer counter `orders.created`, exposed on `/actuator/metrics` |
| Health | `/actuator/health` |

## Run

```bash
mvn test
mvn spring-boot:run
curl -i -X POST localhost:8080/api/v1/orders -H 'Content-Type: application/json' \
     -H 'X-Correlation-Id: demo-123' -d '{"customerId":"c-1","sku":"sku-9","quantity":2}'
curl localhost:8080/actuator/metrics/orders.created
```

## Interview notes

- **Idempotency**: GET, PUT and DELETE are idempotent; POST is not. For safe retries on POST, accept an `Idempotency-Key` header.
- **Versioning**: URI (`/v1`) is the simplest to operate. Keep v1 alive while clients migrate (expand/contract), then deprecate.
- **Errors**: one format for every error (`ProblemDetail`), never leak stack traces.
- **Observability = logs + metrics + traces**. The correlation id ties logs from all services for one request; with Micrometer Tracing/OpenTelemetry you also get trace and span ids.
- Always clear the MDC in a `finally`: threads are reused and the id would leak into the next request.
- `@Async` and other threads do not inherit the MDC; copy it with a `TaskDecorator`.
