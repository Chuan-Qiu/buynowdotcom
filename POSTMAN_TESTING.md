# Postman 完整测试流程

base URL: `http://localhost:909/api/v1`

所有 POST/PUT 请求：Body → raw → JSON，Header 自动加 `Content-Type: application/json`。

需要认证的接口在 Headers 加：`Authorization: Bearer <token>`

---

## 一、用户注册与登录

### 1. 注册用户
- 方法：`POST`
- URL：`/users/add`
- Body：
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "test@test.com",
  "password": "password"
}
```
- 预期：`201` 返回用户信息，记下返回的 `id`（后续用作 `userId`）

---

### 2. 登录
- 方法：`POST`
- URL：`/auth/login`
- Body：
```json
{
  "email": "test@test.com",
  "password": "password"
}
```
- 预期：返回 `token` 和 `id`，**复制 token**，后续所有需要认证的请求都要带上

---

### 3. 查询用户（需要认证）
- 方法：`GET`
- URL：`/users/1`（换成实际 userId）
- Headers：`Authorization: Bearer <token>`
- 预期：返回用户信息

---

### 4. 更新用户（需要认证）
- 方法：`PUT`
- URL：`/users/1/update`
- Headers：`Authorization: Bearer <token>`
- Body：
```json
{
  "firstName": "John",
  "lastName": "Does",
  "email": "test@test.com"
}
```

---

## 二、分类（Category）

### 5. 添加分类
- 方法：`POST`
- URL：`/categories/add`
- Body：
```json
{
  "name": "Electronics"
}
```
- 预期：返回保存的 category，记下 `id`

---

### 6. 获取所有分类
- 方法：`GET`
- URL：`/categories`

---

### 7. 按 ID 查分类
- 方法：`GET`
- URL：`/categories/1`

---

### 8. 按名称查分类
- 方法：`GET`
- URL：`/categories/by/name?name=Electronics`

---

### 9. 更新分类
- 方法：`PUT`
- URL：`/categories/1/update`
- Body：
```json
{
  "name": "Consumer Electronics"
}
```

---

## 三、产品（Product）

### 10. 添加产品
- 方法：`POST`
- URL：`/products/add`
- Body：
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
- 预期：返回产品信息，记下 `id`（后续用作 `productId`）
- 注意：如果 category 名称已存在会复用，不存在会新建

---

### 11. 获取所有产品
- 方法：`GET`
- URL：`/products`

---

### 12. 按 ID 查产品
- 方法：`GET`
- URL：`/products/1`

---

### 13. 按名称搜索
- 方法：`GET`
- URL：`/products/by/name?name=iPhone`

---

### 14. 按品牌搜索
- 方法：`GET`
- URL：`/products/by/brand?brand=Apple`

---

### 15. 按分类搜索
- 方法：`GET`
- URL：`/products/by/category?category=Electronics`

---

### 16. 按品牌 + 名称搜索
- 方法：`GET`
- URL：`/products/by/brand-and-name?brand=Apple&name=iPhone`

---

### 17. 按分类 + 品牌搜索
- 方法：`GET`
- URL：`/products/by/category-and-brand?category=Electronics&brand=Apple`

---

### 18. 更新产品
- 方法：`PUT`
- URL：`/products/1/update`
- Body：
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

## 四、图片（Image）

### 19. 上传图片
- 方法：`POST`
- URL：`/images/upload?productId=1`
- Body：选 **form-data**（不是 JSON）
  - Key: `files`，类型选 **File**，选一张图片
- 预期：返回 imageId，记下备用

---

### 20. 下载图片
- 方法：`GET`
- URL：`/images/1/download`
- 预期：直接返回图片文件

---

### 21. 更新图片
- 方法：`PUT`
- URL：`/images/1/update`
- Body：form-data，Key: `file`，类型 File，选新图片

---

### 22. 删除图片
- 方法：`DELETE`
- URL：`/images/1/delete`

---

## 五、购物车（Cart & CartItem）

> 购物车在用户下单时由系统自动创建，不需要手动创建。先通过添加商品的方式触发购物车创建。

### 23. 添加商品到购物车（需要认证）
- 方法：`POST`
- URL：`/cartItems/item/add?cartId=1&productId=1&quantity=2`
- Headers：`Authorization: Bearer <token>`
- 注意：`cartId` 是用户的购物车 ID，首次可以先试 `1`，如果报错从查询用户接口的返回里找 cart 信息

---

### 24. 查看购物车（需要认证）
- 方法：`GET`
- URL：`/carts/1`
- Headers：`Authorization: Bearer <token>`

---

### 25. 查看购物车总价（需要认证）
- 方法：`GET`
- URL：`/carts/1/total-price`
- Headers：`Authorization: Bearer <token>`

---

### 26. 更新商品数量（需要认证）
- 方法：`PUT`
- URL：`/cartItems/cart/1/item/1/update?quantity=5`
- Headers：`Authorization: Bearer <token>`

---

### 27. 删除购物车中的商品（需要认证）
- 方法：`DELETE`
- URL：`/cartItems/cart/1/item/1/remove`
- Headers：`Authorization: Bearer <token>`

---

### 28. 清空购物车（需要认证）
- 方法：`DELETE`
- URL：`/carts/1/clear`
- Headers：`Authorization: Bearer <token>`

---

## 六、订单（Order）

> 下订单前确保购物车里有商品（先重新执行步骤 23）

### 29. 下订单（需要认证）
- 方法：`POST`
- URL：`/orders/order?userId=1`
- Headers：`Authorization: Bearer <token>`
- 预期：返回订单信息，记下 `id`（orderId）
- 注意：下单后购物车会自动清空

---

### 30. 查询订单详情（需要认证）
- 方法：`GET`
- URL：`/orders/1/order`
- Headers：`Authorization: Bearer <token>`

---

### 31. 查询用户所有订单（需要认证）
- 方法：`GET`
- URL：`/orders/user/1/order`
- Headers：`Authorization: Bearer <token>`

---

## 七、清理测试（可选）

### 32. 删除产品
- 方法：`DELETE`
- URL：`/products/1/delete`

### 33. 删除分类
- 方法：`DELETE`
- URL：`/categories/1/delete`

### 34. 删除用户（需要认证）
- 方法：`DELETE`
- URL：`/users/1/delete`
- Headers：`Authorization: Bearer <token>`