# Báo cáo chuyển đổi Model sang E-commerce Bán Quần Áo

## 1. Các Model mới được tạo:

### 1.1 Product.java (thay thế Product.java)
- Đổi tên từ `Product` thành `Product`
- Cập nhật table name: `product` → `product`
- Thêm relationship với:
  - ProductInventory (OneToMany)
  - Giữ nguyên relationships với User, Criteria, Comment, Action, Image

### 1.2 Size.java (thay thế Size.java)
- Đổi tên từ `Size` thành `Size`
- Cập nhật table name: `size` → `size`
- Thêm relationship với ProductInventory

### 1.3 ProductInventory.java (Mới)
- Quản lý tồn kho sản phẩm theo từng size
- Bảng: `product_inventory`
- Các trường:
  - id (Primary Key)
  - product_id (Foreign Key → Product)
  - size_id (Foreign Key → Size)
  - quantity (số lượng tồn kho)
  - created_at, updated_at
- Unique constraint: (product_id, size_id)

### 1.4 Orders.java (Mới)
- Quản lý đơn hàng
- Bảng: `orders`
- Các trường:
  - id, order_code (unique)
  - user_id (Foreign Key → User)
  - total_amount, status
  - customer_name, customer_phone
  - shipping_address, notes
  - created_at, updated_at
- Relationships:
  - OrderItems (OneToMany)
  - PaymentHistory (OneToMany)

### 1.5 OrderItems.java (Mới)
- Chi tiết sản phẩm trong đơn hàng
- Bảng: `order_items`
- Các trường:
  - id, order_id, product_id, size_id
  - quantity, unit_price, total_price
  - created_at

## 2. Các Model được cập nhật:

### 2.1 Criteria.java
- Loại bỏ tất cả các trường cũ liên quan đến bài đăng/nhà trọ
- Chỉ giữ lại:
  - id, address, price, original_price
- Thêm các trường mới cho quần áo:
  - class (productClass) - loại sản phẩm
  - second_class (secondClass) - phân loại phụ
  - color - màu sắc
  - material - chất liệu
- Cập nhật relationship: product_id thay vì product_id

### 2.2 Action.java
- Cập nhật Foreign Key: `product_id` → `product_id`
- Cập nhật field: `product` → `product`
- Cập nhật constructor

### 2.3 Comment.java
- Cập nhật Foreign Key: `product_id` → `product_id`
- Cập nhật field: `product` → `product`

### 2.4 Image.java
- Cập nhật Foreign Key: `product_id` → `product_id`
- Cập nhật field: `product` → `product`
- Cập nhật constructor

### 2.5 PaymentHistory.java
- Thêm trường `order_id` (Foreign Key → Orders)
- Relationship: ManyToOne với Orders

## 3. Các file cũ cần xóa:
- Product.java
- Size.java

## 4. Bước tiếp theo cần thực hiện:

### 4.1 Cập nhật Repository layer:
- Tạo ProductRepository thay thế ProductRepository
- Tạo SizeRepository thay thế SizeRepository
- Tạo OrdersRepository, OrderItemsRepository, ProductInventoryRepository
- Cập nhật các repository khác nếu cần

### 4.2 Cập nhật Service layer:
- Cập nhật các service để sử dụng Product thay vì Product
- Tạo OrderService, ProductInventoryService
- Cập nhật logic business phù hợp với e-commerce

### 4.3 Cập nhật Controller layer:
- ProductController thay thế ProductController
- Tạo OrderController
- Cập nhật các endpoint

### 4.4 Cập nhật DTO layer:
- Cập nhật các DTO để phản ánh thay đổi model
- Tạo DTO cho Order, OrderItems, ProductInventory

### 4.5 Cập nhật Mapper layer:
- Cập nhật các mapper để map giữa entity và DTO mới

### 4.6 Cập nhật database:
- Chạy migration để đổi tên bảng và cột
- Tạo các bảng mới: orders, order_items, product_inventory
- Thêm cột order_id vào payment_history

## 5. Lưu ý:
- Đã giữ nguyên cấu trúc User, Role không thay đổi
- Hệ thống authentication và authorization vẫn hoạt động bình thường
- Cần cập nhật application.yml nếu có cấu hình database cụ thể
- Cần test kỹ các relationship và constraint sau khi migration
