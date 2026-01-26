# 🚀 MICROservices E-Commerce System

**Hệ thống e-commerce xây dựng theo kiến trúc *microservices***,
gồm nhiều service backend, gateway, UI frontend và các server AI/Node hỗ trợ.

---

# 📌 GIỚI THIỆU

Dự án **Microservices E-Commerce System** là một hệ thống bán hàng trực tuyến được thiết kế theo mô hình **microservices**, giúp:

* 🔹 Dễ mở rộng từng service độc lập
* 🔹 Dễ bảo trì và nâng cấp
* 🔹 Tách biệt rõ frontend – gateway – backend – AI service
* 🔹 Phù hợp cho học tập và triển khai thực tế

---

# 🧩 CÁC MODULE CHÍNH

Hệ thống hiện bao gồm các module sau:

### 1️⃣ `backend/`

👉 Chứa các **microservice backend cốt lõi**
Ví dụ: product-service, order-service, user-service, inventory-service, …

### 2️⃣ `gateway/`

👉 API Gateway dùng để:

* Định tuyến request từ client tới các microservice
* Áp dụng bảo mật, logging, rate limit, auth, …

### 3️⃣ `ecommerce-ui/`

👉 Frontend giao diện người dùng cho hệ thống e-commerce
Người dùng có thể:

* Xem sản phẩm
* Đặt hàng
* Quản lý tài khoản

### 4️⃣ `node-server/`

👉 Node.js server phụ trợ dùng cho:

* Realtime (WebSocket)
* Upload file / ảnh
* Các tác vụ background

### 5️⃣ `python-embedding-server/`

👉 Python server dùng cho AI/ML
Ví dụ:

* Text embedding
* Semantic search
* Recommendation system

---

# 📂 CẤU TRÚC THƯ MỤC

```
.
├── backend/                    # Các microservice backend
├── gateway/                    # API Gateway
├── ecommerce-ui/               # Frontend UI
├── node-server/                # Node.js server phụ trợ
├── python-embedding-server/    # Python AI / Embedding server
└── README.md
```

---

# 🔧 BACKEND SERVICES (CORE MICROSERVICES)

Backend được thiết kế theo mô hình **microservices**, trong đó mỗi service đảm nhiệm một vai trò riêng biệt, dễ scale và phát triển độc lập.

---

## ✍️ 1. Write Service (Command Service)

👉 Service chuyên xử lý **ghi dữ liệu (writing)** vào database.

Chức năng chính:

* Nhận request tạo / cập nhật / xoá dữ liệu
* Ghi dữ liệu vào DB theo mô hình chuẩn hoá
* Phát sự kiện (event) cho các service khác (order, inventory, …)

Ví dụ nghiệp vụ:

* Tạo đơn hàng mới
* Cập nhật trạng thái đơn hàng
* Cập nhật tồn kho

---

## 🔐 2. Auth Service – OAuth2 Google

👉 Service xác thực & phân quyền người dùng.

Chức năng chính:

* Đăng nhập bằng **Google OAuth2**
* Phát hành access token / refresh token
* Xác thực request giữa các service
* Gắn role cho user

---

## 📦 3. Inventory Service (Quản lý kho)

👉 Service quản lý toàn bộ tồn kho sản phẩm.

Chức năng chính:

* Theo dõi số lượng tồn kho
* Giữ chỗ (reserve) hàng khi tạo đơn
* Trừ kho khi đơn hàng thành công
* Hoàn kho khi huỷ đơn

---

## 🧾 4. Order Service (Xử lý đơn hàng)

👉 Service xử lý toàn bộ vòng đời đơn hàng.

Chức năng chính:

* Tạo đơn hàng
* Kiểm tra tồn kho
* Cập nhật trạng thái: PENDING → PAID → SHIPPED → COMPLETED
* Huỷ đơn / hoàn tiền

---

## 👥 5. User & Role Service

👉 Service quản lý người dùng và phân quyền.

Hệ thống hỗ trợ nhiều role:

### 🔑 OWNER

* Toàn quyền hệ thống
* Quản lý cấu hình
* Quản lý admin & manager

### 🧑‍💼 MANAGER

* Quản lý sản phẩm
* Quản lý tồn kho
* Xem báo cáo

### 🛠 ADMIN

* Hỗ trợ vận hành hệ thống
* Xử lý đơn hàng lỗi
* Quản lý user

### 👤 USER

* Xem sản phẩm
* Đặt hàng
* Theo dõi đơn hàng

---

## ⚙️ Hạ tầng xử lý bất đồng bộ & tối ưu hiệu năng

Backend tích hợp các thành phần hạ tầng để đảm bảo hệ thống **chịu tải tốt, ổn định và mở rộng dễ dàng**:

### 📨 Apache Kafka

* Dùng để giao tiếp **bất đồng bộ** giữa các microservice
* Phát sự kiện khi:

  * Tạo đơn hàng
  * Cập nhật trạng thái đơn
  * Trừ / hoàn kho
* Giảm coupling giữa Order Service ↔ Inventory Service
* Tăng độ tin cậy khi traffic lớn

### ⚡ Redis

* Dùng làm **cache** và **store tạm thời** cho dữ liệu nóng
* Lưu trạng thái đơn hàng tạm thời
* Hỗ trợ:

  * Giữ chỗ hàng (reserve stock)
  * Chống double-order
  * Tăng tốc độ phản hồi API

### 🛑 Circuit Breaker

* Tự động ngắt request khi service downstream bị lỗi
* Tránh cascade failure giữa các microservice
* Tăng độ ổn định toàn hệ thống

---

## ⭐ Điểm nổi bật của Backend

* Kiến trúc **microservices chuẩn**

* Tách riêng **write service** để đảm bảo tính nhất quán dữ liệu

* Xác thực bằng **OAuth2 Google**

* Phân quyền chi tiết theo role

* Tích hợp **Kafka** cho xử lý bất đồng bộ

* Tích hợp **Redis** để cache & giữ chỗ tồn kho

* Áp dụng **Circuit Breaker** để tăng khả năng chịu lỗi

* Kiến trúc **microservices chuẩn**

* Tách riêng **write service** để đảm bảo tính nhất quán dữ liệu

* Xác thực bằng **OAuth2 Google**

* Phân quyền chi tiết theo role

* Dễ mở rộng thêm service mới

---

> ✨ Phần tiếp theo sẽ mô tả chi tiết công nghệ sử dụng cho từng service.
# 🌐 GATEWAY SERVICE

Gateway đóng vai trò là **cửa ngõ duy nhất** cho toàn bộ request từ client.

Chức năng chính:

* Định tuyến request tới đúng microservice
* Xác thực token OAuth2
* Áp dụng rate limit
* Logging & tracing
* Load balancing

---

# 🖥 ECOMMERCE UI (FRONTEND)

Frontend là giao diện web cho người dùng cuối.

Chức năng chính:

* Đăng nhập bằng Google
* Xem danh sách sản phẩm
* Đặt hàng
* Theo dõi trạng thái đơn
* Quản lý tài khoản cá nhân

---

# 🔁 NODE SERVER (REALTIME & FILE)

Node.js server dùng cho các tác vụ realtime và phụ trợ.

Chức năng chính:

* WebSocket realtime
* Upload file / ảnh
* Notification
* Background jobs

---

# 🤖 PYTHON EMBEDDING SERVER (AI)

Python server phục vụ các tính năng AI.

Chức năng chính:

* Text embedding
* Semantic search
* Recommendation system

---

# 🛠 CÔNG NGHỆ SỬ DỤNG

* Backend: Spring Boot / NestJS
* Gateway: Spring Cloud Gateway
* Frontend: React / Next.js
* Message Broker: Apache Kafka
* Cache: Redis
* Database: MySQL / MongoDB
* Auth: OAuth2 Google
* AI: Python, Embedding models

---

# 🚀 HƯỚNG PHÁT TRIỂN TIẾP THEO

* Thêm payment service
* Thêm recommendation nâng cao
* Observability (Prometheus, Grafana)
* CI/CD pipeline

---

# 👤 TÁC GIẢ

**Châu** – Học sinh, yêu thích backend & hệ thống phân tán
GitHub: [https://github.com/Chaudeptrai123456](https://github.com/Chaudeptrai123456)

---

# 📄 LICENSE

MIT License

