# Debug & Setup Notes

## 一、MySQL 安装与配置（Mac / Homebrew）

### 1. 安装并启动 MySQL

```bash
brew install mysql
brew services start mysql
```

---

### 2. 设置 root 密码失败（一路回车跳过了 mysql_secure_installation）

**现象：**
```
ERROR 1045 (28000): Access denied for user 'root'@'localhost' (using password: NO)
```

**原因：** 安装后运行 `mysql_secure_installation` 时没有输入密码，但 MySQL 实际上设了一个随机初始密码。

**解决：** 用 skip-grant-tables 模式重置密码：

```bash
brew services stop mysql
mysqld_safe --skip-grant-tables &   # 注意：是 mysqld_safe，不是 mysql_safe
```

等几秒后：
```bash
mysql -u root
```

进入 MySQL 后执行：
```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY '你的新密码';
exit
```

最后重启：
```bash
brew services restart mysql
```

---

### 3. `mysql_safe: command not found`

**原因：** 命令拼写错误，多打了一个 `s`。

**正确命令：** `mysqld_safe`（有 `d`）

---

### 4. "A mysqld process already exists" 导致无法启动

**现象：**
```
mysqld_safe A mysqld process already exists
```

**原因：** 之前用 `mysqld_safe --skip-grant-tables &` 启动的进程还在后台运行，再次启动时冲突。

**解决：**
```bash
sudo pkill mysqld
brew services start mysql
```

---

### 5. `brew services start mysql` 启动失败（Bootstrap error 5）

**现象：**
```
Bootstrap failed: 5: Input/output error
```

**解决：** 改用 sudo 启动：
```bash
sudo brew services start mysql
```

注意：这会更改一些文件的 owner 为 root，升级时可能需要手动处理。日常开发建议再改回非 root 启动。

---

### 6. MySQL Workbench 版本警告

**现象：**
```
Incompatible/nonstandard server version or connection protocol detected (9.6.0).
MySQL Workbench is developed and tested for MySQL Server versions 5.6, 5.7 and 8.0
```

**原因：** Homebrew 安装的 MySQL 是 9.6，比 Workbench 支持的版本新。

**解决：** 点 **Continue Anyway**，功能正常使用，不影响项目。

---

### 7. MySQL Workbench 连接配置

- Connection Method: **Standard (TCP/IP)**
- Hostname: `127.0.0.1`
- Port: `3306`
- Username: `root`
- Default Schema: 留空（连接后再建库）

建库 SQL：
```sql
CREATE DATABASE buynowdotcom;
```

---

## 二、Spring Boot 编译错误

### 1. `com.fasterxml.jackson.databind does not exist`

**原因：** `spring-boot-starter-webmvc` 没有自动引入 Jackson。

**解决：** 在 `pom.xml` 中添加：
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

---

### 2. `package jakarta.validation does not exist` / `cannot find symbol: class Valid`

**位置：** `AuthController.java`

**原因：** `@Valid` 需要 `spring-boot-starter-validation` 依赖，但 `LoginRequest` 本身没有任何校验注解，用不上。

**解决：** 直接删掉 `AuthController` 里的 `@Valid` 注解和对应 import。

---

### 3. `DaoAuthenticationProvider cannot be applied to given types`

**原因：** 新版 Spring Security 移除了无参构造函数和 `setUserDetailsService()` 方法，改为构造函数直接传入。

**错误写法：**
```java
DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
provider.setUserDetailsService(userDetailsService);
```

**正确写法：**
```java
DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
```

---

### 4. `CONCACT` 拼写错误（ProductRepository）

**原因：** JPQL 查询里 `CONCAT` 被拼成了 `CONCACT`，导致查询报错。

**解决：** 全局替换为 `CONCAT`，共 5 处。

---

### 5. `mappedBy` 拼写错误导致 EntityManagerFactory 启动失败

**位置：** `Product.java`

**错误：**
```java
@OneToMany(mappedBy = "prooduct", ...)
```

**正确：**
```java
@OneToMany(mappedBy = "product", ...)
```

同类问题：`Cart.java` 里 `mappedBy = "Cart"` 应为小写 `"cart"`。

---

## 三、API 测试（Postman）

### 1. 找到应用运行端口

应用没有设置 `server.port`，但实际运行在 909 而非默认的 8080（由 IntelliJ Run Configuration 决定）。

**方法一：看启动日志**
```
Tomcat started on port 909 (http) with context path '/'
```

**方法二：命令行查询**
```bash
lsof -iTCP -sTCP:LISTEN | grep java
```

---

### 2. 401 Unauthorized on `POST /api/v1/users/add`

**现象：**
```json
{
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "status": 401
}
```

**原因：** `WebSecurityConfig` 中 `/api/v1/users/**` 整段都被保护了，包括注册接口，导致无法在没有 token 的情况下注册。

**解决：** 在 `WebSecurityConfig.java` 的 `authorizeHttpRequests` 中，将 `/api/v1/users/add` 单独放在 `authenticated()` 规则之前：

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/users/add").permitAll()
    .requestMatchers(SECURED_URLS.toArray(String[]::new)).authenticated()
    .anyRequest().permitAll()
)
```

Spring Security 按顺序匹配，更具体的规则必须放在前面。

---

### 3. "Required request body is missing"

**现象：**
```json
{
    "message": "Something went wrong: Required request body is missing: ..."
}
```

**原因：** 在 Postman 中把参数放到了 **Params** 标签页（URL 查询参数），而不是 **Body**。

**规则：**
- `@RequestBody` → Postman Body → raw → JSON
- `@RequestParam` → Postman Params

**正确配置：**
1. Body → raw → JSON
2. 确保 Header 中有 `Content-Type: application/json`

---

## 四、业务逻辑 Bug（API 测试阶段发现）

### 1. 注册用户后购物车不存在，添加商品报 "Cart not found"

**现象：** 注册完用户，调用 `POST /cartItems/item/add` 报错：
```json
{ "message": "Cart not found" }
```

**根本原因：** `UserService.createUser` 只创建了 `User`，没有同步创建 `Cart`。用户注册完没有关联的购物车。

**面试考点：** 用户和购物车是 `OneToOne` 关系，购物车的生命周期应该和用户一致，应在创建用户时一并初始化。这属于业务完整性设计缺失。

**修复（`UserService.java`）：**
```java
User savedUser = userRepository.save(user);
Cart cart = new Cart();
cart.setUser(savedUser);
cartRepository.save(cart);
return savedUser;
```

---

### 2. 删除购物车商品时连带删除了 Product

**现象：** 调用 `DELETE /cartItems/cart/{cartId}/item/{productId}/remove` 后，产品从数据库消失，后续所有产品查询返回空。

**根本原因：** `CartItem.java` 中 `product` 字段配置了 `cascade = CascadeType.ALL`：
```java
@ManyToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "product_id")
private Product product;
```
`CascadeType.ALL` 包含 `REMOVE`，当 CartItem 被删除时，Hibernate 会级联删除关联的 Product。

**面试考点：** `@ManyToOne` 关系上不应该加 `CascadeType.ALL` 或 `CascadeType.REMOVE`。级联删除只应该从"拥有方"到"被拥有方"（如 `Order → OrderItem`），不应该反向传播到共享实体（Product 可以属于多个 CartItem，不能被其中一个删掉）。

**修复（`CartItem.java`）：**
```java
// 修复前
@ManyToOne(cascade = CascadeType.ALL)

// 修复后
@ManyToOne
```
`cart` 字段上的 `cascade = CascadeType.ALL` 同理也要移除。

---

### 3. 下订单返回 `orderItems: []`，且响应体循环引用导致 JSON 超长

**现象：** `POST /orders/order` 返回的订单中 `orderItems` 为空数组，响应 JSON 长达数万字符（User → Cart → User → Cart → ...无限循环）。

**根本原因：** `OrderController.placeOrder` 直接返回了原始 `Order` 实体：
```java
Order order = orderService.placeOrder(userId);
return ResponseEntity.ok(new ApiResponse("...", order));
```
问题一：`Order.orderItems` 是懒加载（`@OneToMany` 默认 `FetchType.LAZY`），Jackson 序列化时 Hibernate Session 已关闭，无法触发懒加载，所以序列化为空集合。

问题二：`Order` 包含 `User`，`User` 包含 `Cart`，`Cart` 包含 `User`，形成循环引用，Jackson 会无限递归序列化。

**面试考点：** Controller 层不应直接返回 JPA 实体，原因有三：
1. 懒加载在 Session 关闭后失效
2. 实体间双向关联会导致 Jackson 循环引用
3. 暴露内部字段（如 `password` 哈希值）

应始终返回 DTO。

**修复（`OrderController.java`）：**
```java
// 修复前
Order order = orderService.placeOrder(userId);
return ResponseEntity.ok(new ApiResponse("...", order));

// 修复后
var order = orderService.placeOrder(userId);
return ResponseEntity.ok(new ApiResponse("...", orderService.getOrder(order.getId())));
```