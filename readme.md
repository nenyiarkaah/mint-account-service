# mint-account-service

A Scala microservice for managing financial accounts, extracted from the Mint monolith. Provides a REST API for creating, updating, deleting, and querying accounts stored in MS SQL Server.

Built with Akka HTTP following the patterns from [CRUD Microservice with AkkaHttp](https://medium.com/se-notes-by-alexey-novakov/crud-microservice-with-akkahttp-c914059bcf9f).

![Version](https://img.shields.io/badge/version-2.0.1-blue)
![Scala](https://img.shields.io/badge/scala-2.12.8-red)
![Java](https://img.shields.io/badge/java-11-orange)

## Prerequisites

- Java 11 — set up with: `sdk use java 11.0.25-tem`
- SBT
- Docker Desktop ([Mac](https://docs.docker.com/docker-for-mac/install/), [Windows](https://docs.docker.com/docker-for-windows/install/), [Linux](https://docs.docker.com/engine/install/)) — required for E2E tests

## Running Locally

```bash
sbt compile
sbt run
```

The service starts on the port configured in `application.conf` (default: `8080`). Local environment variables can be set in `.env`.

To auto-create the database schema on startup, set `featureToggles.createSchema = true` in config.

## Architecture

The service follows a strict three-layer architecture:

```
akkahttp/       → HTTP routes (CommandRoutes, QueryRoutes)
services/       → Business logic (AccountService)
repositories/   → Data access (AccountRepository via Slick)
```

Supporting packages:

| Package | Purpose |
|---|---|
| `models/` | Domain types (`Account`, `CommandResult`, `ImportStatus`, `AccountTypes`) |
| `configs/` | PureConfig-backed `AppConfig` with Refined type constraints |
| `modules/` | `AkkaModule`: MacWire wiring point that composes DB, services, and routes |
| `Exceptions/` | Sealed `UserError` ADT (`InvalidAccount`, `UnknownSortField`) |
| `json/` | `GenericJsonWriter[T]` abstraction over Spray JSON formatters |

Entry point: `AkkaMain.scala` → delegates to `AkkaModule.init()`.

## Configuration

Runtime config is loaded from `src/main/resources/application.conf` via PureConfig. Override any value with an environment variable or a `.env` file in the project root.

Key configuration options:

| Key | Description |
|---|---|
| `jdbc.url` | JDBC connection string (validated against a JDBC regex at startup) |
| `jdbc.connectionTimeout` | Connection timeout in ms — must be between 0 and 100 000 |
| `jdbc.maximumPoolSize` | HikariCP pool size — must be between 0 and 100 |
| `featureToggles.createSchema` | Auto-create the DB schema on startup (`true`/`false`) |

Invalid config causes a startup failure, not a runtime error.

## Testing

```bash
sbt compileScalaStyle          # Lint / code style
sbt +ut:test                   # Unit + integration tests
sbt +e2e:test                  # End-to-end tests (requires Docker + source test.env)
sbt "ut:testOnly *ClassName*"  # Run a single test class
```

**Test types:**

| Layer | Location | Approach |
|---|---|---|
| Lint | — | ScalaStyle, runs automatically before unit tests |
| Unit | `src/test/` (services/) | ScalaTest AsyncWordSpec + Mockito; mocks the repository |
| Integration | `src/test/` (akkahttp/) | Akka HTTP `ScalatestRouteTest`; tests routes with wired services |
| E2E | `src/e2e/` | TestContainers (real MS SQL Server); full stack, no mocks |

E2E tests require Docker. The SQL Server container starts once per suite; schema is created and torn down in `beforeAll`/`afterAll`.

## Docker

```bash
sbt docker:publishLocal             # Build local image
sbt docker:dockerBuildWithBuildx    # Build multi-arch image (ARM + AMD64)
```

To run with Docker Compose:

```bash
docker-compose -f src/e2e/resources/docker-compose.yml up -d
docker-compose -f src/e2e/resources/docker-compose.yml down -v
```

## API

All endpoints are prefixed with `/api/accounts`.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/accounts` | List accounts. Query params: `sort`, `page`, `pageSize` |
| `GET` | `/api/accounts/:id` | Get account by ID |
| `POST` | `/api/accounts` | Create a new account |
| `PUT` | `/api/accounts/:id` | Update an account |
| `DELETE` | `/api/accounts/:id` | Delete an account |
| `GET` | `/api/accounts/existingtypeofaccounts` | Get distinct account types |
| `GET` | `/api/accounts/isconfiguredforimports?id=:id` | Check if account is configured for imports |
| `GET` | `/api/accounts/health` | Health check — returns `{status, db, buildInfo, uptimeMs}` |
| `GET` | `/api/accounts/metrics` | Dropwizard metrics — counters, timers, JVM gauges as JSON |

### Account Model

```json
{
  "id": 1,
  "name": "My Account",
  "accountType": "Savings",
  "company": "Some Bank",
  "isActive": true,
  "isConfiguredForImport": false
}
```

### Sort Fields

The `sort` query parameter accepts: `id`, `name`, `accountType`, `company`, `isActive`, `isConfiguredForImport`.

### Responses

| Status | Meaning |
|---|---|
| `200 OK` | Success, returns JSON |
| `412 Precondition Failed` | Validation error (e.g. duplicate name, missing required field) |
| `500 Internal Server Error` | Unexpected failure |

## Observability

### Metrics

`GET /api/accounts/metrics` returns a JSON snapshot of all Dropwizard metrics:

```json
{
  "counters": {
    "service.insert.success": { "count": 12 },
    "service.insert.failure": { "count": 0 }
  },
  "timers": {
    "service.insert.timer": { "count": 12, "meanMs": 3.2, "p99Ms": 18.0, "meanRate": 1.1 }
  },
  "gauges": {
    "jvm.memory.heap.used": { "value": 48234496 }
  }
}
```

Counters tracked: `service.insert.success/failure`, `service.update.success/failure`, `service.delete.success`.  
Timers tracked: `service.insert.timer`, `service.selectAll.timer`, `repo.selectAll/insert/update/delete/healthCheck.timer`.  
JVM gauges: heap, GC, thread states (registered automatically on startup).

### Health check

`GET /api/accounts/health` returns:

```json
{
  "status": "UP",
  "db": "UP",
  "buildInfo": { "name": "mint-account", "version": "2.0.1", "scalaVersion": "2.12.20", "sbtVersion": "1.x" },
  "uptimeMs": 34021
}
```

Returns `200` when DB is reachable, `503` when it is not.

### Structured logging

Logs are written as plain text by default. Set `LOG_FORMAT=json` at runtime to switch to JSON lines (via Logstash Logback Encoder):

```bash
LOG_FORMAT=json sbt run
```

Each log line in JSON mode includes `timestamp`, `level`, `message`, `logger`, and any MDC fields (`requestId`, `httpMethod`, `path`, `accountId`, `accountName`) set by the route and service layers.

### Distributed tracing (OpenTelemetry)

Attach the [OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases) with no code changes:

```bash
java \
  -javaagent:/path/to/opentelemetry-javaagent.jar \
  -Dotel.service.name=mint-account-service \
  -Dotel.exporter.otlp.endpoint=http://collector:4317 \
  -jar target/universal/stage/bin/mint-account
```

Key environment variables:

| Variable | Description |
|---|---|
| `OTEL_SERVICE_NAME` | Service name reported to the tracing backend |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OTLP collector endpoint (gRPC) |
| `OTEL_TRACES_EXPORTER` | `otlp` (default), `zipkin`, `jaeger`, or `none` |
| `OTEL_METRICS_EXPORTER` | `none` to avoid conflicts with Dropwizard metrics |

The agent auto-instruments Akka HTTP server spans, JDBC calls (via Slick/HikariCP), and thread pool activity.

## Contributing

1. Fork the repository and create a feature branch
2. Make your changes — every code change must have a corresponding test
3. Run the full test suite before opening a PR:
   ```bash
   sbt +ut:test
   source test.env && sbt +e2e:test
   ```
4. Open a pull request against `main`
