# CustomOrderQuery Documentation

Đây là tài liệu hướng dẫn sử dụng `CustomOrderQuery` cho việc lọc và tìm kiếm đơn hàng trong hệ thống quản lý.

## Cấu trúc

### CustomOrderQuery.OrderFilterParam

Class này chứa các tham số filter cho việc tìm kiếm đơn hàng:

```java
public static class OrderFilterParam {
    private String keywords;     // Tìm kiếm theo mã đơn hàng
    private String sortField;    // Trường để sắp xếp
    private String sortType;     // Kiểu sắp xếp (ASC/DESC)
    private Long userId;         // Lọc theo ID người dùng
    private String status;       // Lọc theo trạng thái đơn hàng
    private String startDate;    // Lọc từ ngày (format: YYYY-MM-DD)
    private String endDate;      // Lọc đến ngày (format: YYYY-MM-DD)
}
```

## Cách sử dụng

### 1. Trong Controller

```java
@GetMapping("/admin/orders")
public ResponseEntity<?> getAllOrdersForAdmin(@Valid @ModelAttribute GetOrderRequest request) {
    Page<OrderDto> page = orderService.getAllOrders(request, 
        PageRequest.of(request.getStart(), request.getLimit()));
    return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
}
```

### 2. Request Parameters

Khi gọi API, bạn có thể truyền các tham số sau:

- `keywords`: Tìm kiếm theo mã đơn hàng (tìm kiếm partial match)
- `userId`: Lọc đơn hàng của user cụ thể
- `status`: Lọc theo trạng thái (`pending`, `confirmed`, `shipped`, `delivered`, `cancelled`)
- `startDate`: Ngày bắt đầu (định dạng YYYY-MM-DD)
- `endDate`: Ngày kết thúc (định dạng YYYY-MM-DD)
- `sortField`: Trường cần sắp xếp (`id`, `orderCode`, `totalAmount`, `createdAt`, `status`)
- `sortType`: Kiểu sắp xếp (`ASC` hoặc `DESC`)
- `start`: Trang bắt đầu (default: 0)
- `limit`: Số lượng item trên trang (default: 10, range: 5-50)

### 3. Ví dụ API Call

```bash
# Lấy tất cả đơn hàng, sắp xếp theo ngày tạo mới nhất
GET /api/admin/orders?sortField=createdAt&sortType=DESC

# Tìm đơn hàng theo mã đơn hàng
GET /api/admin/orders?keywords=12345

# Lọc theo trạng thái và user
GET /api/admin/orders?status=pending&userId=1

# Lọc theo khoảng thời gian
GET /api/admin/orders?startDate=2024-01-01&endDate=2024-01-31

# Kết hợp nhiều filter
GET /api/admin/orders?status=confirmed&startDate=2024-01-01&sortField=totalAmount&sortType=DESC&start=0&limit=20
```

## Chi tiết Filter Logic

### 1. Keywords (Mã đơn hàng)
- Tìm kiếm partial match trong orderCode
- Không phân biệt hoa thường
- Sử dụng LIKE với wildcard

### 2. UserId
- Lọc chính xác theo ID người dùng
- Join với bảng User

### 3. Status
- Lọc chính xác theo trạng thái đơn hàng
- So sánh string exact match

### 4. Date Range
- Lọc theo trường `createdAt`
- `startDate`: >= ngày chỉ định
- `endDate`: <= ngày chỉ định
- Sử dụng DATE function để so sánh chỉ phần ngày

### 5. Sorting
- Default sort: `id DESC`
- Có thể sort theo bất kỳ trường nào của Order entity
- Hỗ trợ cả ASC và DESC

## Lưu ý

1. **Validation**: `GetOrderRequest` có validation cho `start` và `limit`
2. **Performance**: Query đã được tối ưu với proper indexing
3. **Security**: Chỉ admin mới có thể truy cập endpoint này
4. **Pagination**: Luôn sử dụng pagination để tránh load quá nhiều data
5. **Date Format**: Ngày phải theo format YYYY-MM-DD (ISO format)

## Troubleshooting

### Lỗi thường gặp:

1. **Invalid date format**: Đảm bảo `startDate` và `endDate` theo format YYYY-MM-DD
2. **Invalid sort field**: Chỉ sử dụng các field có trong Orders entity
3. **Limit out of range**: `limit` phải trong khoảng 5-50

### Performance Tips:

1. Luôn sử dụng pagination
2. Kết hợp filter để giảm số lượng kết quả
3. Sử dụng index trên các trường thường xuyên filter (status, userId, createdAt)