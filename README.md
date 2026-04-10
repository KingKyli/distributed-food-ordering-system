# Distributed Food Ordering System

Distributed food delivery demo with Android client and Java backend.

The repository contains two main modules:
- `app`: native Android client (Java)
- `server`: Java TCP backend with SQLite persistence

## What This Project Is

This is an end-to-end distributed system simulation, not just a UI demo.

It supports two business roles:
- Customer: search stores, view products, buy items
- Partner: request access code, login, manage store/products

Communication is done through a custom line-based TCP protocol between app and server.

## Architecture

```text
Android UI
  -> App Services
    -> Repository / Gateway layer
      -> TCP communication
        -> MockServer
          -> ServerCommandProcessor
            -> StoreService (business rules)
              -> StoreRepository (SQLite persistence)
```

## Protocol Commands

Main server commands:
- `CLIENT_HELLO`
- `SEARCH`
- `BUY`
- `REQUEST_PARTNER_ACCESS_CODE`
- `PARTNER_LOGIN`
- `GET_CREDENTIALS`
- `ADD_STORE`
- `REMOVE_STORE`
- `ADD_PRODUCT`
- `REMOVE_PRODUCT`
- `UPDATE_PRODUCT`

## Improvements Completed (1, 2, 3)

### 1. Socket-level backend integration tests

Added protocol integration tests that open a real TCP socket against a test server and verify command-response flows:
- hello/search/buy flow
- partner code lifecycle flow

This gives better confidence than unit-only tests for protocol behavior.

### 2. CI pipeline upgrade

Updated GitHub Actions workflow to validate both modules:
- backend tests: `:server:test`
- android checks: `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`

### 3. Logging and observability hardening

Added a reusable server logging utility and replaced ad-hoc prints with leveled logs.

Highlights:
- central logger setup with env-based level (`SERVER_LOG_LEVEL`)
- structured runtime logs in server startup, client handling, and command processing
- less sensitive data in logs (access code is no longer logged)

## Existing Backend Guarantees

The backend now enforces:
- strict command input validation
- 6-digit partner access code format
- prevention of duplicate active access code requests
- repository validation for duplicate store names
- repository validation for non-negative product inventory/price
- transactional save with rollback on failure

## Project Level (Current Assessment)

Current level: **advanced student / junior production-ready foundation**.

Why this level:
- strong separation of concerns
- realistic multi-role business flows
- persistent data layer on both client and server
- automated tests including protocol integration tests
- CI checks in place

What is still missing for full production level:
- authentication and authorization hardening beyond demo access codes
- API versioning / migration strategy
- deployment automation and environment profiles
- monitoring stack (metrics, tracing, alerting)
- load and resilience testing

## Tech Stack

- Android: Java, Material Components
- Backend: Java 11, TCP sockets, SQLite JDBC
- Build: Gradle (Kotlin DSL)
- Testing: JUnit 5
- CI: GitHub Actions

## Quick Start

1. Start backend

```powershell
.\gradlew.bat :server:run
```

2. Build Android app

```powershell
.\gradlew.bat :app:assembleDebug
```

3. Run backend tests

```powershell
.\gradlew.bat :server:test
```

4. Run CI-equivalent local checks

```powershell
.\gradlew.bat :server:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

5. Connect app to backend

- Android emulator: `10.0.2.2:8765`
- Physical device over USB:

```powershell
adb reverse tcp:8765 tcp:8765
```

Use `127.0.0.1:8765` in app settings when using `adb reverse`.
