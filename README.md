# BuyNow

A full-stack e-commerce platform — React SPA front end, Spring Boot REST API back end, MySQL.

| | Stack |
|---|---|
| **Frontend** | React 19 · Redux Toolkit · React Router 7 · Vite 8 · Axios · React-Bootstrap |
| **Backend** | Java 21 · Spring Boot 4 · Spring Security (stateless JWT) · Spring Data JPA · ModelMapper |
| **Database** | MySQL |

## Repository Layout

```
buynowdotcom/
├── backend/     Spring Boot REST API  (Maven)
├── frontend/    React SPA             (Vite)
├── DESIGN.md    Architecture & design document (UML diagrams)
└── CLAUDE.md    Working notes for AI-assisted development
```

The two sides are decoupled by an explicit contract: versioned REST resources under `/api/v1`,
a single response envelope `{ message, data }`, and DTOs as the only data shape the client sees.
Entities never cross the API boundary.

## Quick Start

**Prerequisites:** Java 21, MySQL, Node 18+

### 1. Database

```bash
mysql -u root -p -e "CREATE DATABASE buynowdotcom;"
```

### 2. Backend → http://localhost:9090

Create `backend/src/main/resources/application.properties` (git-ignored — see
[backend/README.md](backend/README.md) for the full template):

```properties
server.port=9090
spring.datasource.url=jdbc:mysql://localhost:3306/buynowdotcom?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=<your_password>
spring.jpa.hibernate.ddl-auto=update
api.prefix=/api/v1
auth.token.jwtSecret=<hex_secret_at_least_32_bytes>
auth.token.accessExpirationInMils=120000
auth.token.refreshExpirationInMils=300000
app.useSecureCookie=false
```

```bash
cd backend
./mvnw spring-boot:run
```

> Requires Java 21. If your default JDK is older:
> `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw spring-boot:run`

### 3. Frontend → http://localhost:5174

```bash
cd frontend
npm install
npm run dev
```

The API base URL is set in [frontend/src/component/services/api.js](frontend/src/component/services/api.js).
The backend's CORS configuration allows origins `5173`–`5175`.

## Architecture at a Glance

**Backend** — strict layering, each domain exposed as an interface + implementation:

```
Controller → IXxxService / XxxService → Repository → Entity
                                             ↓
                                       DTO (ModelMapper)
```

- One response envelope: `ApiResponse(message, data)`
- `GlobalExceptionHandler` maps `EntityNotFoundException → 404`, `EntityExistsException → 409`
- Protected paths (`/carts/**`, `/cartItems/**`, `/orders/**`) declared once in the security filter chain
- Auth: short-lived access token in the response body + refresh token in an `HttpOnly` cookie

**Frontend** — components split by role, state split by domain:

```
component/layout/     app shell
component/product/    page-level "smart" components
component/common/     reusable presentational components
component/services/   the only place that knows about the API
store/features/       searchSlice · productSlice · paginationSlice · categorySlice
```

Every async call follows one `createAsyncThunk` pattern (`pending / fulfilled / rejected`),
and all network traffic goes through a single Axios instance.

## Documentation

| Document | Contents |
|---|---|
| [DESIGN.md](DESIGN.md) | Full design document: 14 UML diagrams (domain model class diagram, ER, sequence, state), API contract, design decisions with trade-offs, known issues and roadmap |
| [backend/README.md](backend/README.md) | Backend setup, configuration template, endpoint reference |
| [backend/TESTING_NOTES.md](backend/TESTING_NOTES.md) | MySQL setup and debugging notes |
| [backend/POSTMAN_TESTING.md](backend/POSTMAN_TESTING.md) | Manual API testing walkthrough |

## Status

Working: product catalog, categories, brand/name/category filtering, search, pagination,
image upload & download, user registration, JWT login with refresh, cart and order APIs.

Not yet wired: "add to cart" is complete on the back end but the front-end buttons do not
dispatch yet.

**This is a learning/portfolio project and is not production-hardened.** Known gaps —
including missing resource-ownership checks on protected endpoints, unguarded write
operations, and absent inventory validation — are documented with severity ratings and a
remediation roadmap in [DESIGN.md §9–§10](DESIGN.md).
