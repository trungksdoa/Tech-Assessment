# Giả định và Giới hạn (Assumptions and Limitations)

Tài liệu này mô tả phạm vi của nền tảng Concert Ticket Booking, trình bày chi tiết các giả định được đưa ra trong quá trình phát triển, các tính năng đã được triển khai, và các phần cố tình bị loại bỏ khỏi phạm vi dự án (Giới hạn).

## 1. Các Giả định 

* **Xác thực và Phân quyền (Authentication & Authorization):** Giả định rằng có một hệ thống Identity Provider độc lập (ví dụ: Keycloak, Auth0, hoặc API Gateway) xử lý việc đăng nhập của người dùng. Backend tin tưởng hoàn toàn vào `userId` được truyền qua API (trong thực tế, giá trị này sẽ được trích xuất từ JWT token đã được xác thực).
* **Xử lý Thanh toán (Payment Processing):** Việc thanh toán thực tế đòi hỏi các callback bất đồng bộ từ cổng thanh toán bên thứ 3 (Stripe, VNPay). Do đó, giả định rằng API cập nhật trạng thái (`PATCH /api/bookings/{id}/status`) là để giả lập phản hồi webhook từ cổng thanh toán, giúp chuyển đổi đơn hàng từ `PENDING` sang `PAID` hoặc `FAILED`.
* **Luồng Trạng thái Đơn hàng (Booking State Machine):** Một đơn hàng (Booking) có luồng trạng thái nghiêm ngặt. Khởi tạo luôn là `PENDING`, và chỉ có thể chuyển sang `PAID`, `CANCELLED`, hoặc `FAILED`.
* **Sử dụng Voucher:** Một voucher có thể được nhiều người sử dụng, nhưng mỗi người chỉ được dùng số lần nhất định do trường `maxUsagePerUser` quy định. Khi booking thành công, lượt dùng voucher sẽ lập tức được ghi nhận.
* **Cấu hình sẵn từ Admin:** Giả định rằng quản trị viên (Admin) đã tạo sẵn dữ liệu về các Concerts (Sự kiện), Ticket Categories (Loại vé), và Vouchers trực tiếp dưới cơ sở dữ liệu. Do đó, các API phục vụ vận hành nội bộ (Operation) sẽ chỉ tập trung vào việc theo dõi và thao tác trên các đơn hàng đang diễn ra, chứ không làm chức năng nhập liệu master data.

## 2. Những gì  ĐÃ LÀM (Tính năng cốt lõi)

Hệ thống ưu tiên xây dựng lõi đặt vé (Core Booking Engine) đảm bảo sự ổn định, chịu tải cao (High Concurrency) và đáng tin cậy trong các chiến dịch Flash Sale:

* **Chống bán vượt vé (Overselling):** Triển khai cập nhật Atomic trực tiếp dưới database (`reduceQuantity`) để ngăn chặn triệt để Overselling khi lưu lượng truy cập tăng vọt mà không cần phụ thuộc vào hệ thống Distributed Lock phức tạp.
* **Tính lũy đẳng (Idempotency):** Áp dụng cơ chế `Idempotency-Key` trong luồng tạo Booking để ngăn ngừa lỗi **Duplicate Bookings** (đặt trùng đơn) do mạng chập chờn hoặc người dùng bấm retry nhiều lần.
* **Bảo vệ Voucher (Voucher Abuse Prevention):** Xây dựng luồng giao dịch chuẩn ACID có sử dụng Database Lock để kiểm tra chính xác số lượt sử dụng voucher của từng User (`VoucherHistory`), chống gian lận/vượt quá số lần cho phép.
* **Kiến trúc Clean Architecture:** Áp dụng nguyên lý SOLID bằng cách tách biệt logic nghiệp vụ phức tạp thành các service chuyên dụng (`BookingItemProcessingService`, `VoucherProcessingService`) đằng sau một Facade (`BookingServiceImpl`).
* **Phân trang & Tìm kiếm động:** Toàn bộ các API lấy danh sách đều được phân trang (`Page<T>`) để tránh tràn bộ nhớ. Thêm chức năng tìm kiếm động (Criteria API) cho danh sách sự kiện.
* **API Tính Toán Voucher Độc Lập:** Tách biệt API `calculateDiscount` để Client có thể hiển thị trước số tiền được giảm và số tiền cuối cùng cho người dùng trước khi thực sự ấn nút đặt vé.
* **Lịch sử Vận hành (Audit Trail):** Triển khai hệ thống `OperationLog` bất đồng bộ (Asynchronous) để ghi vết mọi thay đổi trạng thái của Booking, phục vụ cho bộ phận vận hành nội bộ.

## 3. Những gì  KHÔNG LÀM (Giới hạn phạm vi)

Dựa trên việc ưu tiên các giá trị cốt lõi nhất của hệ thống, các tính năng sau đã chủ động được loại bỏ khỏi thiết kế:

* **Các API CRUD cho Master Data:** 
  * Các API dùng để Tạo/Sửa/Xóa Concerts, Ticket Categories, và Vouchers **không được triển khai**. 
  * *Lý do:* Đây là các thao tác CRUD cơ bản, không thể hiện được khả năng giải quyết các rào cản kỹ thuật khó. Hệ thống phụ thuộc vào dữ liệu được tạo sẵn (Database Seeding).
* **Quản lý User nâng cao:** Không có Entity User hay luồng Đăng ký/Đăng nhập. Hệ thống hoàn toàn dựa vào tham số định danh `userId`.
* **Chọn Chỗ Ngồi (Seat Selection):** Hệ thống hiện tại chỉ quản lý số lượng vé theo *loại vé* (Quantity per Category), chứ không giữ chỗ theo số ghế cụ thể.
* **Distributed Caching (Bộ nhớ đệm phân tán):** Mặc dù việc dùng Cache (như Redis) sẽ giúp tối ưu tốc độ đọc danh sách Concert, nhưng nó bị loại bỏ để giữ cho môi trường Local Setup đơn giản nhất. Thiết kế hiện tại tối ưu tốc độ đọc bằng Database Indexing.
* **Tự động hết hạn (Cron Jobs):** Trong thực tế, các đơn hàng `PENDING` chưa thanh toán sẽ tự động hết hạn sau 15 phút để nhả vé lại kho. Lịch trình chạy ngầm này (Background Jobs) không nằm trong phạm vi của bài test.
