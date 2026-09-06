# BuyNow — Backend

RESTful e-commerce API built with Spring Boot 4, Spring Security, and MySQL.

## Tech Stack

- **Java 21** + **Spring Boot 4.0.5**
- **Spring Security** with stateless JWT authentication (jjwt 0.12.3)
- **Spring Data JPA** + **Hibernate** + **MySQL**
- **ModelMapper** for entity-to-DTO conversion
- **Lombok**

## Getting Started

### Prerequisites

- Java 21
- MySQL (via Homebrew: `brew install mysql && brew services start mysql`)
- Maven (or the included `./mvnw` wrapper)

### Configuration

Create `src/main/resources/application.properties` — this file is git-ignored:

```properties
# Server
server.port=9090

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/buynowdotcom?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=<your_password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# API prefix
api.prefix=/api/v1

# Cookie (set true in production: adds Secure + SameSite=None)
app.useSecureCookie=false

# JWT
auth.token.jwtSecret=<hex_secret_at_least_32_bytes>
auth.token.accessExpirationInMils=120000
auth.token.refreshExpirationInMils=300000

# File upload
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Run

```bash
mysql -u root -p -e "CREATE DATABASE buynowdotcom;"
./mvnw spring-boot:run
```

Requires Java 21. If your default JDK is older:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw spring-boot:run
```

Verify the port actually in use:

```bash
lsof -iTCP -sTCP:LISTEN | grep java
```

## API

Base URL: `http://localhost:9090/api/v1`

All responses (except `/auth/**` and image downloads) use one envelope:

```json
{ "message": "...", "data": ... }
```

| Status | When |
|---|---|
| 200 | Success |
| 401 | Missing/invalid token on a protected path |
| 404 | `EntityNotFoundException` |
| 409 | `EntityExistsException` (duplicate email or product) |
| 500 | Anything else — `"Something went wrong: ..."` |

### Auth

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/login` | Returns `{ accessToken }` and sets an `HttpOnly` `refreshToken` cookie |
| POST | `/auth/refresh-token` | Exchanges the refresh cookie for a new access token |

### Products

| Method | Endpoint |
|---|---|
| GET | `/products/all` |
| GET | `/products/product/{productId}/product` |
| POST | `/products/add` |
| PUT | `/products/product/{productId}/update` |
| DELETE | `/products/product/{productId}/delete` |
| GET | `/products/products/{name}/products` |
| GET | `/products/product/by-brand?brand=` |
| GET | `/products/{category}/products` |
| GET | `/products/category/{categoryId}/products` |
| GET | `/products/products/by/brand-and-name?brandName=&productName=` |
| GET | `/products/products/by/category-and-brand?category=&brand=` |
| GET | `/products/distinct/products` |
| GET | `/products/distinct/brands` |

### Categories

| Method | Endpoint |
|---|---|
| GET | `/categories` · `/categories/all` |
| GET | `/categories/{categoryId}` |
| GET | `/categories/by/name?name=` |
| POST | `/categories/add` |
| PUT | `/categories/{categoryId}/update` |
| DELETE | `/categories/{categoryId}/delete` |

### Images

| Method | Endpoint |
|---|---|
| POST | `/images/upload?productId=` (multipart) |
| GET | `/images/{imageId}/download` |
| PUT | `/images/{imageId}/update` |
| DELETE | `/images/{imageId}/delete` |

### Users

| Method | Endpoint |
|---|---|
| GET | `/users/{userId}` |
| POST | `/users/add` |
| PUT | `/users/{userId}/update` |
| DELETE | `/users/{userId}/delete` |

### Cart & Orders 🔒

Require `Authorization: Bearer <accessToken>`, and the cart/order must be yours.

| Method | Endpoint |
|---|---|
| GET | `/carts/{cartId}` |
| GET | `/carts/{cartId}/total-price` |
| DELETE | `/carts/{cartId}/clear` |
| POST | `/cartItems/item/add?cartId=&productId=&quantity=` |
| PUT | `/cartItems/cart/{cartId}/item/{productId}/update?quantity=` |
| DELETE | `/cartItems/cart/{cartId}/item/{productId}/remove` |
| POST | `/orders/order?userId=` |
| GET | `/orders/{orderId}/order` |
| GET | `/orders/user/{userId}/order` |

> 🔒 = authenticated **and** the resource must belong to you (checked in the service layer;
> a mismatch returns 403). 🛡 = requires `ROLE_ADMIN`. Catalog reads are public; every catalog
> write is admin-only. Anything not listed defaults to requiring authentication.
> Full matrix: [DESIGN.md §8.4](../DESIGN.md).

## Roles

`RoleSeeder` creates `ROLE_USER` and `ROLE_ADMIN` at startup, and registration grants
`ROLE_USER`. There is deliberately **no endpoint for granting admin** — an endpoint that lets
a caller escalate its own privileges would defeat the authorization rules. Promote an account
directly in the database:

```sql
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM user u, role r
WHERE u.email = 'you@example.com' AND r.name = 'ROLE_ADMIN';
```

Log in again afterwards so the new access token carries the authority.

## Authentication Flow

1. Register — `POST /api/v1/users/add` (a `Cart` is created automatically)
2. Login — `POST /api/v1/auth/login` → `{ accessToken }` + `Set-Cookie: refreshToken (HttpOnly)`
3. Send `Authorization: Bearer <accessToken>` on protected requests
4. When the access token expires (2 min), `POST /api/v1/auth/refresh-token` — the browser
   sends the refresh cookie automatically

## Data Model

```
User 1──0..1 Cart 1──* CartItem *──1 Product *──1 Category
  │ 1──* Order 1──* OrderItem *──1 Product 1──* Image
  └ *──* Role  (user_roles join table)
```

- Registering a user creates an empty `Cart`
- `CartItem.unitPrice` snapshots the product price at add-to-cart time
- `placeOrder` runs in one transaction: decrement inventory → create order → clear cart
- `OrderItem.price` snapshots again, so order totals stay reproducible after price changes

Full entity/ER/sequence diagrams: [DESIGN.md](../DESIGN.md)

## Other Docs

- [TESTING_NOTES.md](TESTING_NOTES.md) — MySQL setup and debugging notes
- [POSTMAN_TESTING.md](POSTMAN_TESTING.md) — manual API testing walkthrough
