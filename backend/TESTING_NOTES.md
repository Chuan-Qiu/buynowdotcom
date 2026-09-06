# Debug & Setup Notes

A running log of problems hit while getting this project working, kept because
the diagnosis is usually more reusable than the fix.

---

## 1. MySQL Setup (macOS / Homebrew)

### 1.1 Install and start

```bash
brew install mysql
brew services start mysql
```

### 1.2 Root password not set (pressed Enter through `mysql_secure_installation`)

**Symptom**
```
ERROR 1045 (28000): Access denied for user 'root'@'localhost' (using password: NO)
```

**Cause** `mysql_secure_installation` was skipped, but MySQL had already generated a random initial password.

**Fix** Reset it in skip-grant-tables mode:

```bash
brew services stop mysql
mysqld_safe --skip-grant-tables &   # note: mysqld_safe, not mysql_safe
```

Then, after a few seconds:

```bash
mysql -u root
```

```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'your_new_password';
exit
```

```bash
brew services restart mysql
```

### 1.3 `mysql_safe: command not found`

Typo. The command is `mysqld_safe` — with the `d`.

### 1.4 "A mysqld process already exists"

**Symptom**
```
mysqld_safe A mysqld process already exists
```

**Cause** The process started earlier with `mysqld_safe --skip-grant-tables &` is still running in the background and conflicts with the new one.

**Fix**
```bash
sudo pkill mysqld
brew services start mysql
```

### 1.5 `brew services start mysql` fails with Bootstrap error 5

**Symptom**
```
Bootstrap failed: 5: Input/output error
```

**Fix** Start it with sudo:
```bash
sudo brew services start mysql
```

Note that this changes the owner of some files to root, which may need manual cleanup on upgrade. For day-to-day development, switching back to a non-root start is preferable.

### 1.6 MySQL Workbench version warning

**Symptom**
```
Incompatible/nonstandard server version or connection protocol detected (9.6.0).
MySQL Workbench is developed and tested for MySQL Server versions 5.6, 5.7 and 8.0
```

**Cause** Homebrew installs MySQL 9.6, newer than the versions Workbench targets.

**Fix** Click **Continue Anyway**. Everything needed for this project works.

### 1.7 Workbench connection settings

- Connection Method: **Standard (TCP/IP)**
- Hostname: `127.0.0.1`
- Port: `3306`
- Username: `root`
- Default Schema: leave empty and create the database after connecting

```sql
CREATE DATABASE buynowdotcom;
```

---

## 2. Spring Boot Compilation Errors

### 2.1 `com.fasterxml.jackson.databind does not exist`

**Cause** `spring-boot-starter-webmvc` does not pull in Jackson transitively.

**Fix** Add it to `pom.xml`:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

### 2.2 `package jakarta.validation does not exist` / `cannot find symbol: class Valid`

**Where** `AuthController.java`

**Cause** `@Valid` requires `spring-boot-starter-validation`, but `LoginRequest` carries no validation annotations, so the annotation had nothing to do.

**Fix** Remove the `@Valid` annotation and its import from `AuthController`.

### 2.3 `DaoAuthenticationProvider cannot be applied to given types`

**Cause** Recent Spring Security removed the no-arg constructor and `setUserDetailsService()`; the dependency is now passed to the constructor.

```java
// no longer compiles
DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
provider.setUserDetailsService(userDetailsService);

// correct
DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
```

### 2.4 `CONCACT` typo in `ProductRepository`

`CONCAT` was misspelled `CONCACT` in the JPQL query, so it failed at runtime. Replaced in all 5 occurrences.

### 2.5 `mappedBy` typo breaking EntityManagerFactory startup

**Where** `Product.java`

```java
// wrong
@OneToMany(mappedBy = "prooduct", ...)

// right
@OneToMany(mappedBy = "product", ...)
```

Same class of problem in `Cart.java`, where `mappedBy = "Cart"` should have been lowercase `"cart"`.

---

## 3. API Testing (Postman)

### 3.1 Finding the port the app is actually on

At the time of these notes the app had no `server.port` setting and ran on 909, chosen by the IntelliJ run configuration rather than by any file. (It is now declared explicitly as `9090` in `application.properties`, which is the actual lesson here.)

**From the startup log**
```
Tomcat started on port 909 (http) with context path '/'
```

**From the command line**
```bash
lsof -iTCP -sTCP:LISTEN | grep java
```

### 3.2 401 Unauthorized on `POST /api/v1/users/add`

**Symptom**
```json
{
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "status": 401
}
```

**Cause** The whole `/api/v1/users/**` range was protected, registration included, so there was no way to create the first account without already having a token.

**Fix** Put the more specific rule ahead of the general one — Spring Security matches in order:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.POST, "/api/v1/users/add").permitAll()
    .requestMatchers(OWNER_SCOPED).authenticated()
    ...
)
```

> The security configuration has since been reworked (see `DESIGN.md` §8.4). Rule ordering still matters exactly as described.

### 3.3 "Required request body is missing"

**Symptom**
```json
{
    "message": "Something went wrong: Required request body is missing: ..."
}
```

**Cause** The parameters were entered on Postman's **Params** tab (URL query string) instead of **Body**.

**Rule of thumb**
- `@RequestBody` → Postman Body → raw → JSON
- `@RequestParam` → Postman Params

Also make sure the request carries `Content-Type: application/json`.

---

## 4. Business-Logic Bugs Found During API Testing

### 4.1 "Cart not found" when adding an item after registering

**Symptom** After registering, `POST /cartItems/item/add` returned:
```json
{ "message": "Cart not found" }
```

**Root cause** `UserService.createUser` created the `User` but no `Cart`, so a freshly registered user had nothing to add items to.

**Takeaway** `User` and `Cart` are `OneToOne` and share a lifetime, so the cart should be initialised alongside the user. This is a gap in business-integrity design, not a coding error.

**Fix** (`UserService.java`)
```java
User savedUser = userRepository.save(user);
Cart cart = new Cart();
cart.setUser(savedUser);
cartRepository.save(cart);
return savedUser;
```

### 4.2 Removing a cart item deleted the Product

**Symptom** After `DELETE /cartItems/cart/{cartId}/item/{productId}/remove`, the product vanished from the database and every subsequent product query came back empty.

**Root cause** `CartItem.product` was annotated with `cascade = CascadeType.ALL`:
```java
@ManyToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "product_id")
private Product product;
```
`CascadeType.ALL` includes `REMOVE`, so deleting a `CartItem` cascaded into deleting the `Product` it pointed at.

**Takeaway** `@ManyToOne` should not carry `CascadeType.ALL` or `CascadeType.REMOVE`. Cascading deletes belong on the owning side of a composition (`Order → OrderItem`), never propagating back into a shared entity — a `Product` can belong to many `CartItem`s and must not be destroyed by one of them.

**Fix** (`CartItem.java`)
```java
// before
@ManyToOne(cascade = CascadeType.ALL)

// after
@ManyToOne
```
The same removal applies to the `cart` field.

> `Product.category` still carries `cascade = ALL` for the same reason and has the same defect — it is tracked in `DESIGN.md` §11.2.

### 4.3 Checkout returned `orderItems: []` with a huge, self-referential response

**Symptom** `POST /orders/order` returned an order whose `orderItems` was an empty array, in a JSON body tens of thousands of characters long (User → Cart → User → Cart → …).

**Root cause** `OrderController.placeOrder` returned the raw `Order` entity:
```java
Order order = orderService.placeOrder(userId);
return ResponseEntity.ok(new ApiResponse("...", order));
```

Two separate problems:

1. `Order.orderItems` is lazy (`@OneToMany` defaults to `FetchType.LAZY`). By the time Jackson serialized it, the Hibernate session was closed, so the collection could not be initialised and came out empty.
2. `Order` holds a `User`, which holds a `Cart`, which holds the `User` — a cycle Jackson recurses through indefinitely.

**Takeaway** Controllers should never return JPA entities, for three reasons: lazy associations break once the session closes, bidirectional associations create serialization cycles, and internal fields (such as a password hash) leak. Always return a DTO.

**Fix** (`OrderController.java`)
```java
// before
Order order = orderService.placeOrder(userId);
return ResponseEntity.ok(new ApiResponse("...", order));

// after
var order = orderService.placeOrder(userId);
return ResponseEntity.ok(new ApiResponse("...", orderService.getOrder(order.getId())));
```
