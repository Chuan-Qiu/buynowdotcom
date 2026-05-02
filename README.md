# BuyNow — E-Commerce Backend

A RESTful e-commerce backend built with Spring Boot 4, Spring Security, and MySQL.

## Tech Stack

- **Java 21** + **Spring Boot 4.0.5**
- **Spring Security 7** with stateless JWT authentication (jjwt 0.12.3)
- **Spring Data JPA** + **Hibernate 7** + **MySQL 9**
- **ModelMapper** for entity-to-DTO conversion
- **Lombok**

## Getting Started

### Prerequisites

- Java 21
- MySQL 9 (via Homebrew: `brew install mysql`)
- Maven (or use the included `./mvnw` wrapper)

### Configuration

Create `src/main/resources/application.yaml` (excluded from git):

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/buynowdotcom?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

server:
  port: 8080

api:
  prefix: /api/v1

auth:
  token:
    jwtSecret: your_jwt_secret_key_at_least_32_chars
    expirationInMils: 3600000
```

### Run

```bash
# Create database
mysql -u root -p -e "CREATE DATABASE buynowdotcom;"

# Start the app
./mvnw spring-boot:run
```

## API Overview

Base URL: `http://localhost:8080/api/v1`

All responses follow the format:
```json
{ "message": "...", "data": ... }
```

### Public Endpoints (no token required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | Login, returns JWT token |
| POST | `/users/add` | Register new user |
| GET | `/products` | Get all products |
| GET | `/products/{id}` | Get product by ID |
| GET | `/products/by/name?name=` | Search by name |
| GET | `/products/by/brand?brand=` | Search by brand |
| GET | `/products/by/category?category=` | Search by category |
| GET | `/products/by/brand-and-name?brand=&name=` | Search by brand and name |
| GET | `/products/by/category-and-brand?category=&brand=` | Search by category and brand |
| POST | `/products/add` | Add product |
| PUT | `/products/{id}/update` | Update product |
| DELETE | `/products/{id}/delete` | Delete product |
| GET | `/categories` | Get all categories |
| POST | `/categories/add` | Add category |
| GET | `/categories/{id}` | Get category by ID |
| GET | `/categories/by/name?name=` | Get category by name |
| PUT | `/categories/{id}/update` | Update category |
| DELETE | `/categories/{id}/delete` | Delete category |
| POST | `/images/upload?productId=` | Upload product images (multipart) |
| GET | `/images/{id}/download` | Download image |
| PUT | `/images/{id}/update` | Update image |
| DELETE | `/images/{id}/delete` | Delete image |

### Protected Endpoints (requires `Authorization: Bearer <token>`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users/{id}` | Get user by ID |
| PUT | `/users/{id}/update` | Update user |
| DELETE | `/users/{id}/delete` | Delete user |
| GET | `/carts/{cartId}` | Get cart |
| GET | `/carts/{cartId}/total-price` | Get cart total |
| DELETE | `/carts/{cartId}/clear` | Clear cart |
| POST | `/cartItems/item/add?cartId=&productId=&quantity=` | Add item to cart |
| PUT | `/cartItems/cart/{cartId}/item/{productId}/update?quantity=` | Update item quantity |
| DELETE | `/cartItems/cart/{cartId}/item/{productId}/remove` | Remove item from cart |
| POST | `/orders/order?userId=` | Place order |
| GET | `/orders/{orderId}/order` | Get order by ID |
| GET | `/orders/user/{userId}/order` | Get all orders for a user |

## Authentication Flow

1. Register: `POST /api/v1/users/add`
2. Login: `POST /api/v1/auth/login` → copy the `token` from the response
3. Add to every protected request header: `Authorization: Bearer <token>`

## Data Model

```
User ──── Cart ──── CartItem ──── Product ──── Category
  │                                  │
  └──── Order ──── OrderItem ────────┘
```

- A `User` is created with an associated `Cart` automatically
- Placing an order converts cart items into order items and clears the cart
- Products are not deleted when cart items are removed (no cascade on `CartItem.product`)
