# 📘 Các Cách Truyền Tham Số Trong Spring Boot

Spring Boot hỗ trợ nhiều cách để truyền dữ liệu từ client đến server thông qua HTTP request. Dưới đây là các cách phổ biến:

---

## 1. ✅ Request Path (Path Variable)

### ✔️ Dùng để truyền dữ liệu qua URL path

**Ví dụ:**
```java
@GetMapping("/users/{id}")
public ResponseEntity<?> getUserById(@PathVariable Long id) {
    // id sẽ nhận giá trị từ URL, ví dụ: /users/5 -> id = 5
}
```

**URL gọi:** `/users/5`

---

## 2. ✅ Request Query Parameters

### ✔️ Dữ liệu truyền dưới dạng `?key=value`

**Ví dụ:**
```java
@GetMapping("/search")
public ResponseEntity<?> search(@RequestParam String keyword) {
    // keyword lấy từ URL, ví dụ: /search?keyword=spring
}
```

**URL gọi:** `/search?keyword=spring`

---

## 3. ✅ Request Body

### ✔️ Dữ liệu gửi trong phần **body** của request (thường dùng với POST/PUT)

**Ví dụ:**
```java
@PostMapping("/users")
public ResponseEntity<?> createUser(@RequestBody UserDTO user) {
    // user chứa dữ liệu JSON được gửi trong body
}
```

**Request Body JSON:**
```json
{
  "name": "Alice",
  "email": "alice@example.com"
}
```

---

## 4. ✅ Request Header

### ✔️ Dữ liệu truyền qua HTTP headers

**Ví dụ:**
```java
@GetMapping("/info")
public ResponseEntity<?> getInfo(@RequestHeader("X-User-Token") String token) {
    // token sẽ lấy từ header X-User-Token
}
```

**Header gọi:**
```
X-User-Token: abc123
```

---

## 5. ✅ Request Part (dùng với Multipart - thường để upload file)

**Ví dụ:**
```java
@PostMapping("/upload")
public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile file) {
    // file chứa file người dùng gửi lên
}
```

**Content-Type:** `multipart/form-data`

---

## 6. ✅ Matrix Variables (ít dùng)

**Ví dụ:**
```java
@GetMapping("/products/{filters}")
public ResponseEntity<?> filterProducts(@MatrixVariable int min, @MatrixVariable int max) {
    // Truy cập qua URL: /products/filter;min=10;max=100
}
```

> Cần bật cấu hình hỗ trợ matrix variable trong Spring.

---

## 7. ✅ Cookie Value

**Ví dụ:**
```java
@GetMapping("/welcome")
public ResponseEntity<?> welcome(@CookieValue("user-id") String userId) {
    // userId được lấy từ cookie
}
```

---

## 📌 Tổng Kết

| Loại Truyền | Annotation        | Dữ liệu nằm ở đâu            |
|-------------|-------------------|------------------------------|
| Path        | `@PathVariable`   | Trong URL path               |
| Query       | `@RequestParam`   | Sau dấu `?` trên URL         |
| Body        | `@RequestBody`    | Trong phần body              |
| Header      | `@RequestHeader`  | Trong HTTP header            |
| File Upload | `@RequestPart`    | Trong multipart/form-data    |
| Matrix Var  | `@MatrixVariable` | Trong path kiểu `;key=value` |
| Cookie      | `@CookieValue`    | Trong HTTP Cookie            |
