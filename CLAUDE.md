w# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Build:**
```bash
./mvnw clean package -DskipTests
```

**Run (from IntelliJ):** Use the green Run button. The app starts on port **909** (set in the IntelliJ Run Configuration, not in `application.properties`).

**Run from terminal:**
```bash
./mvnw spring-boot:run
```

**Check which port the app is actually using:**
```bash
lsof -iTCP -sTCP:LISTEN | grep java
```

**MySQL (Homebrew):**
```bash
brew services start mysql    # start
brew services stop mysql     # stop
```

## Architecture

Spring Boot 4.0.5 / Java 21 REST API backed by MySQL. All responses are wrapped in `ApiResponse(message, data)`. All endpoints are prefixed with `/api/v1` (configured via `api.prefix` in `application.properties`).

### Layer structure

```
controller → service (interface + impl) → repository → model
```

Each domain has a paired interface (`IProductService`) and implementation (`ProductService`). Controllers inject the interface.

### Domain model relationships

- `User` owns a `Cart` (OneToOne) and has many `Order`s (OneToMany)
- `Cart` has a set of `CartItem`s; each `CartItem` references a `Product`
- `Order` has a list of `OrderItem`s; each `OrderItem` references a `Product`
- `Product` belongs to a `Category` and has a list of `Image`s
- `User` has many `Role`s via a `user_roles` join table (ManyToMany)

### Security

JWT-based stateless auth (`jjwt 0.12.x`). The filter chain in `WebSecurityConfig` secures:
- `/api/v1/carts/**`, `/api/v1/orders/**`, `/api/v1/users/**` → require JWT

Exception: `/api/v1/users/add` is explicitly `permitAll()` (must stay above the `authenticated()` rule in the chain).

To get the currently authenticated user in a service, call `userService.getAuthenticatedUser()`, which reads from `SecurityContextHolder`.

### DTO conversion

`ModelMapper` bean (in `AppConfig`) converts entities to DTOs before returning from controllers. Never return raw entities from controllers — always map to a DTO first.

### Error handling

`GlobalExceptionHandler` maps:
- `EntityNotFoundException` → 404
- `EntityExistsException` → 409
- All other exceptions → 500 with message `"Something went wrong: ..."`

Throw these JPA exceptions directly from services; the handler catches them globally.

### Database

`ddl-auto=create` — **tables are dropped and recreated on every restart**. This is intentional for development. Change to `update` when data persistence between restarts is needed.

Credentials are in `src/main/resources/application.properties` (not committed to version control in production).