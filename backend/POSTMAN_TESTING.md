# Postman Walkthrough

An end-to-end manual test of the API, in the order that produces working data.

**Base URL:** `http://localhost:9090/api/v1`

- Every POST/PUT with a JSON payload: Body → raw → JSON (Postman sets `Content-Type: application/json`).
- Endpoints marked 🔒 need `Authorization: Bearer <accessToken>` in Headers.
- Endpoints marked 🛡 additionally require the account to hold `ROLE_ADMIN` — see step 0.

---

## 0. Granting Yourself Admin

Catalog mutations (products, categories, images) require `ROLE_ADMIN`. There is deliberately
no API for granting it — a self-service privilege-escalation endpoint would defeat the
authorization rules entirely. Promote an account directly in the database after registering
it in step 1:

```sql
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM user u, role r
WHERE u.email = 'test@test.com' AND r.name = 'ROLE_ADMIN';
```

Roles themselves are seeded at startup by `RoleSeeder`. Log in again after the insert so the
new access token carries the authority.

---

## 1. Users and Authentication

### 1. Register
- `POST /users/add`
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "test@test.com",
  "password": "password"
}
```
Returns the created user. Note the `id` — it is the `userId` used later. A cart is created
automatically at the same time.

### 2. Log in
- `POST /auth/login`
```json
{
  "email": "test@test.com",
  "password": "password"
}
```
Returns `{ "accessToken": "..." }` and sets an `HttpOnly` `refreshToken` cookie. **Copy the
access token.** It expires after 2 minutes — when a request starts returning 401, run step 3.

### 3. Refresh the access token
- `POST /auth/refresh-token`

Postman sends the refresh cookie automatically. Returns a fresh `accessToken`.

### 4. Get user 🔒
- `GET /users/1`

Only your own account, or any account if you are an admin. Requesting someone else's id
returns **403**.

### 5. Update user 🔒
- `PUT /users/1/update`
```json
{
  "firstName": "John",
  "lastName": "Does",
  "email": "test@test.com"
}
```

---

## 2. Categories

### 6. Add a category 🛡
- `POST /categories/add`
```json
{ "name": "Electronics" }
```

### 7. List all categories
- `GET /categories` (or `GET /categories/all`)

### 8. Get a category by id
- `GET /categories/1`

### 9. Get a category by name
- `GET /categories/by/name?name=Electronics`

### 10. Update a category 🛡
- `PUT /categories/1/update`
```json
{ "name": "Consumer Electronics" }
```

---

## 3. Products

### 11. Add a product 🛡
- `POST /products/add`
```json
{
  "name": "iPhone 15",
  "brand": "Apple",
  "description": "Latest iPhone",
  "price": 999.99,
  "inventory": 10,
  "category": {
    "name": "Electronics"
  }
}
```
Note the returned `id` — it is the `productId` used later. An existing category name is
reused; an unknown one is created.

### 12. List all products
- `GET /products/all`

### 13. Get a product by id
- `GET /products/product/1/product`

### 14. Search by name
- `GET /products/products/iPhone/products`

### 15. Search by brand
- `GET /products/product/by-brand?brand=Apple`

### 16. Search by category name
- `GET /products/Electronics/products`

### 17. Search by category id
- `GET /products/category/1/products`

### 18. Search by brand and name
- `GET /products/products/by/brand-and-name?brandName=Apple&productName=iPhone`

### 19. Search by category and brand
- `GET /products/products/by/category-and-brand?category=Electronics&brand=Apple`

### 20. Distinct products (one per name)
- `GET /products/distinct/products`

### 21. Distinct brands
- `GET /products/distinct/brands`

### 22. Update a product 🛡
- `PUT /products/product/1/update`
```json
{
  "name": "iPhone 15 Pro",
  "brand": "Apple",
  "description": "Pro version",
  "price": 1199.99,
  "inventory": 5,
  "category": {
    "name": "Electronics"
  }
}
```

---

## 4. Images

### 23. Upload images 🛡
- `POST /images/upload?productId=1`
- Body → **form-data** (not JSON)
  - Key `files`, type **File**, choose one or more images

Returns a list of `ImageDto`. Note an `id`.

### 24. Download an image
- `GET /images/1/download`

Public — the storefront needs it without a login. Returns the bytes.

### 25. Update an image 🛡
- `PUT /images/1/update`
- Body → form-data, key `file`, type File

### 26. Delete an image 🛡
- `DELETE /images/1/delete`

---

## 5. Cart and Cart Items

> The cart is created during registration, so there is nothing to create by hand.
> Every cart endpoint verifies that the cart belongs to the authenticated user — passing
> someone else's `cartId` returns **403**, not their data.

### 27. Add an item to the cart 🔒
- `POST /cartItems/item/add?cartId=1&productId=1&quantity=2`

The `cartId` is your own cart's id. If you do not know it, read it from step 4's response.

### 28. View the cart 🔒
- `GET /carts/1`

### 29. Cart total 🔒
- `GET /carts/1/total-price`

### 30. Change an item's quantity 🔒
- `PUT /cartItems/cart/1/item/1/update?quantity=5`

### 31. Remove an item 🔒
- `DELETE /cartItems/cart/1/item/1/remove`

### 32. Empty the cart 🔒
- `DELETE /carts/1/clear`

---

## 6. Orders

> Make sure the cart has items first — re-run step 27 if you emptied it.
> As with carts, the `userId` and `orderId` are checked against the authenticated principal.

### 33. Place an order 🔒
- `POST /orders/order?userId=1`

Returns the order. Note its `id`. The cart is emptied and product inventory is decremented
in the same transaction.

### 34. Get an order 🔒
- `GET /orders/1/order`

### 35. List a user's orders 🔒
- `GET /orders/user/1/order`

---

## 7. Cleanup (optional)

### 36. Delete a product 🛡
- `DELETE /products/product/1/delete`

### 37. Delete a category 🛡
- `DELETE /categories/1/delete`

### 38. Delete a user 🔒
- `DELETE /users/1/delete`

---

## Expected Failures Worth Confirming

These are the cases the authorization design exists for. Each should fail:

| Attempt | Expected |
|---|---|
| Any catalog write without a token | 401 |
| Any catalog write as a non-admin user | 403 |
| `GET /carts/{id}` with another user's cart id | 403 |
| `GET /orders/user/{id}/order` with another user's id | 403 |
| `POST /orders/order?userId={someone else}` | 403 |
| `GET /users/{id}` for an account that is not yours | 403 |
| Any protected endpoint more than 2 minutes after login without refreshing | 401 |
