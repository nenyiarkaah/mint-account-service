# CLAUDE.md
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

`mint-account-service` is a Scala microservice built with **Akka HTTP** and **Slick** (MS SQL Server), providing CRUD operations for account management. It uses **Cats** for functional error handling, **MacWire** for compile-time dependency injection, and **PureConfig + Refined** for validated configuration.

# Rules

- **Always run the full test suite** after every code change before considering a task done:
    - `sbt +ut:test` — unit tests (services/) and integration tests (akkahttp/), both in `src/test/scala`
    - `sbt +e2e:test` — end-to-end tests (requires Docker + `source test.env`)
- **Every code change must have a test**: either update an existing test or add a new one. No code change is complete without corresponding test coverage.

## Environment

Requires Java 11: `sdk use java 11.0.25-tem`

## Commands

```bash
# Compile
sbt compile

# Unit + integration tests (both live in src/test/scala, run together)
sbt +ut:test

# End-to-end tests
sbt +e2e:test

# Run a single test
sbt "ut:testOnly *ClassName*"

# Lint
sbt compileScalaStyle

# Docker build
sbt docker:stage
```

## Architecture

The service follows a strict three-layer architecture:

```
akkahttp/          → HTTP routes (CommandRoutes, QueryRoutes)
services/          → Business logic (AccountService)
repositories/      → Data access (AccountRepository via Slick)
```

Supporting packages:
- `models/` — Domain types (`Account`, `CommandResult`, `ImportStatus`, `AccountTypes`)
- `configs/` — PureConfig-backed `AppConfig` with Refined type constraints
- `modules/` — `AkkaModule`: single wiring point (MacWire) that composes DB, services, and routes
- `Exceptions/` — Sealed `UserError` ADT (`InvalidAccount`, `UnknownSortField`)
- `json/` — `GenericJsonWriter[T]` abstraction over Spray JSON formatters

Entry point: `AkkaMain.scala` → delegates to `AkkaModule.init()`.

## Key Patterns

**Functional error handling**: Service methods compose as `Future`-based Cats MonadError chains. Validation is pipelined: `validateAccount → validateAccountDoesNotExist → repo.insert`. Errors surface as typed exceptions (`InvalidAccount`, etc.) and are mapped to HTTP responses in routes.

**Trait algebras**: `AccountService` implements both `Alg[Account]` (CRUD contract) and `AlgAccount` (domain-specific queries). Route tests mock at this trait boundary.

**Refined types in config**: `JdbcConfig` validates URL against a JDBC regex, connection timeout is `Interval[0, 100000]`, pool size is `Interval[0, 100]`. Invalid config fails at startup, not at runtime.

**Sorting strategy map**: `AccountRepository` uses a `Map[String, Rep[_] => Ordered]` to translate sort field strings to type-safe Slick column orderings. Adding a new sort field requires updating this map.

**CORS as trait mixin**: `CORSHandler` is a reusable trait mixed into routes — not middleware in the traditional sense.

## Testing Strategy

| Layer | Location | Command | Approach |
|---|---|---|---|
| Lint | — | `sbt compileScalaStyle` | ScalaStyle, auto-runs before `ut:test` |
| Unit | `src/test/` (services/) | `sbt +ut:test` | ScalaTest AsyncWordSpec + Mockito; mocks repo |
| Integration | `src/test/` (akkahttp/) | `sbt +ut:test` | Akka HTTP `ScalatestRouteTest`; tests routes with wired services |
| E2E | `src/e2e/` | `sbt +e2e:test` | TestContainers (real MS SQL Server); full stack, no mocks |

There is no `it:` sbt scope — unit and integration tests share the `ut` configuration and run together via `sbt +ut:test`.

E2E tests require Docker. The SQL Server container is started `ForAllTestContainer` — once per suite. Schema is created/torn down in `beforeAll`/`afterAll`.

## Configuration

Runtime config is loaded from `src/main/resources/application.conf` via PureConfig, with environment variable overrides. Local development uses `.env` / `test.env` files. Feature toggle `featureToggles.createSchema` controls whether the schema is auto-created on startup.

## Knowledge Graph (RAG)

A pre-built knowledge graph of this codebase lives in `graphify-out/`:

- `graphify-out/graph.json` — nodes, edges, and community memberships for all 168 entities (130 AST-extracted from code, 48 semantic from docs)
- `graphify-out/GRAPH_REPORT.md` — community map, god nodes, surprising connections, and knowledge gaps
- `graphify-out/graph.html` — interactive visual (open in browser)

**When to use it:** Before answering questions about codebase structure, tracing a dependency, or finding where a concept lives — query `graph.json` first. Key god nodes (highest cross-cutting connections): `AccountService` (12 edges), `RequestSupport` (10), `AccountRepository` (9). Communities map directly to the three-layer architecture: *Account Repository (Data Access)*, *Account Service (Business Logic)*, *Command Routes (Write API)*, *Query Routes (Read API)*.

To update the graph after code changes: `/graphify . --update`
