# CLAUDE.md

Guidance for Claude Code (claude.ai/code) when working in this repository.

## Repository Layout

This is a monorepo. Run commands from the matching subdirectory — there is no build tool at the root.

```
backend/     Spring Boot 4 REST API (Maven)
frontend/    React 19 SPA (Vite)
DESIGN.md    Architecture document — 23 UML diagrams (both sides), design decisions, known issues
```

## Commands

**Backend** (from `backend/`):

```bash
./mvnw spring-boot:run                                    # run on port 9090
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw spring-boot:run   # if default JDK < 21
./mvnw clean package -DskipTests                          # build
lsof -iTCP -sTCP:LISTEN | grep java                       # check the port actually in use
```

**Frontend** (from `frontend/`):

```bash
npm run dev      # http://localhost:5174
npm run build
npm run lint
```

**MySQL** (Homebrew): `brew services start mysql` / `brew services stop mysql`

## Backend Architecture

Spring Boot 4.0.5 / Java 21 REST API backed by MySQL. Endpoints are prefixed with `/api/v1`
(via `api.prefix` in `application.properties`). Responses are wrapped in `ApiResponse(message, data)` —
except `/auth/**`, which returns a bare `{ accessToken }` map, and image downloads, which return bytes.

### Layer structure

```
controller → service (interface + impl) → repository → model
```

Each domain has a paired interface (`IProductService`) and implementation (`ProductService`).
Controllers inject the interface. Services depend on other services' interfaces too.

### Domain model

- `User` owns a `Cart` (OneToOne) and has many `Order`s (OneToMany)
- `Cart` has a set of `CartItem`s; each `CartItem` references a `Product`
- `Order` has a list of `OrderItem`s; each `OrderItem` references a `Product`
- `Product` belongs to a `Category` and has a list of `Image`s
- `User` has many `Role`s via a `user_roles` join table (ManyToMany, EAGER)

`Category.products` carries `@JsonIgnore` to break the bidirectional serialization cycle.

### Security

Stateless JWT (`jjwt 0.12.x`). Configured in `security/config/ShopConfig.java`:

- Catalog (`/products/**`, `/categories/**`, `/images/**`): `GET` is public, every write requires `ROLE_ADMIN`.
- Per-user resources (`/carts/**`, `/cartItems/**`, `/orders/**`, `/users/**`): authenticated.
- `/auth/**` and `POST /users/add` are public. Everything else defaults to `authenticated()`.
- **Authentication is not authorization here.** An id in a URL locates a resource but never proves
  entitlement, so ownership is verified in the service layer — `CartService.getCart`,
  `CartService.getCartByUserId`, the three `OrderService` entry points, and
  `UserService.assertSelfOrAdmin`. They throw `AccessDeniedException` → 403.
  When adding an endpoint under those paths, add the matching ownership check.
- `RoleSeeder` seeds `ROLE_USER` / `ROLE_ADMIN`; registration grants `ROLE_USER`. There is no API
  for granting admin — it is a manual DB insert (see `backend/README.md`).
- Access token: 2 min, returned in the response body. Refresh token: 5 min, `HttpOnly` cookie.
- To get the current user inside a service, call `userService.getAuthenticatedUser()`.

CORS is registered **inside the security filter chain** (`http.cors()` + a `CorsConfigurationSource`
bean in `ShopConfig`), allowing `localhost:5173`–`5175`. It must stay there, not at the MVC layer:
otherwise preflight `OPTIONS` is rejected by the authorization rules before reaching a controller.

### DTO conversion

The `ModelMapper` bean lives in `ShopConfig`. **Never return a raw entity from a controller** —
always map to a DTO first.

### Error handling

`GlobalExceptionHandler` maps `EntityNotFoundException → 404`, `EntityExistsException → 409`,
everything else → 500 `"Something went wrong: ..."`. Throw these JPA exceptions directly from
services; the handler catches them globally.

### Database

`spring.jpa.hibernate.ddl-auto=update` — Hibernate maintains the schema; data survives restarts.

Credentials and the JWT secret live in `backend/src/main/resources/application.properties`,
which is **git-ignored**. See `backend/README.md` for the template.

## Frontend Architecture

React 19 + Redux Toolkit + Vite. Three layers:

```
component/{layout,home,hero}/   app shell and landing page
component/product/              page-level "smart" components (Products, ProductDetails)
component/common/               reusable presentational components (Paginator, SideBar, ...)
component/services/api.js       the ONLY place that knows about the API (single Axios instance)
store/features/                 searchSlice · productSlice · paginationSlice · categorySlice
```

Conventions to preserve when editing:

- Presentational components take props only — no `useSelector` inside `ProductCard` and friends.
- One slice per domain. `Products.jsx` is the only place that combines search + brand + pagination.
- Every API call goes through `createAsyncThunk`. `productSlice` declares the shared
  `pending`/`rejected` behaviour once via `addMatcher` rather than per case — keep it that way.
- No component imports `axios` directly — always go through `component/services/api.js`.
  Image URLs are built only in `component/services/imageService.js`.
- A rejected thunk raises a toast via the listener middleware in `store.js`. Do not add
  per-component error handling for that.

## Documentation Language

Tracked docs are **English only** — this repository is read by English-speaking reviewers.
Chinese working copies live beside them as `*.zh-CN.md` and are git-ignored. If you update a
tracked doc that has a `.zh-CN.md` sibling, update both.

## Before Changing Behavior

Read [DESIGN.md](DESIGN.md) §11 first — it lists known defects on both sides (IDOR on protected endpoints,
unguarded writes, no inventory validation, `cascade = ALL` on `Product.category`) with severity
ratings. Do not "fix" something already documented there without checking the roadmap in §12.
