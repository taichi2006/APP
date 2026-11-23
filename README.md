# TCFurnish - Noble Interior & 3D AR Experience

**TCFurnish** là ứng dụng thương mại điện tử nội thất cao cấp tích hợp trải nghiệm thực tế ảo tăng cường (**3D AR**). Với TCFurnish, người dùng có thể trực tiếp ướm thử các sản phẩm nội thất sang trọng (sofa, bàn ghế, giường ngủ, tủ kệ gỗ tự nhiên) ngay trong không gian thực tế của ngôi nhà với kích thước tỉ lệ 1:1 trước khi ra quyết định mua sắm.

---

## 🌟 Tính Năng Nổi Bật

- **Trải Nghiệm 3D AR Thực Tế**: Tương tác xoay, di chuyển, thay đổi màu sắc và đặt mô hình 3D AR chuẩn xác vào phòng với Google Sceneform & ARCore.
- **Bộ Sưu Tập Nội Thất Thượng Lưu**: Danh mục đa dạng phong cách Scandinavian, Mid-Century và Modern Luxury từ gỗ sồi Bắc Mỹ, gỗ óc chó FAS và đệm mút cao cấp.
- **Chi Tiết Vật Liệu & Chế Tác Chuyên Sâu**: Cung cấp thông số cấu trúc khung gỗ, bề mặt bọc vải/da, đệm mút lò xo, sơn hoàn thiện và chính sách bảo hành 5 năm.
- **Thanh Toán Linh Hoạt**: Hỗ trợ thanh toán Sandbox tự động xác nhận và thanh toán khi nhận hàng (COD).
- **Hệ Thống Voucher & Giảm Giá**: Áp dụng mã ưu đãi thông minh, tự động tính toán tổng thanh toán và hiển thị huy hiệu giảm giá.
- **Quản Lý Đơn Hàng & Tài Khoản**: Theo dõi trạng thái đơn hàng, hủy đơn hàng, gửi thông báo Notification Bar và quản lý hồ sơ cá nhân.

---

## 🛠️ Kiến Trúc & Công Nghệ

- **Kiến trúc phân tầng rõ ràng**:
  - **Back-End / Business Logic (100% Java)**: Toàn bộ Models, ViewModels, Firebase Services, Dependency Injection, Helpers và Validators được viết bằng Java chuẩn mực.
  - **Front-End / UI (Kotlin)**: Fragments, ViewBinding, RecyclerView Adapters, Navigation Component và Sceneform AR Interaction.
- **Dagger-Hilt**: Quản lý Dependency Injection tập trung, module hóa linh hoạt.
- **Firebase Platform**:
  - Firebase Authentication: Đăng nhập/đăng ký qua Email, Mật khẩu và Google Sign-In.
  - Cloud Firestore: Đồng bộ cơ sở dữ liệu sản phẩm, giỏ hàng, đánh giá và đơn hàng thời gian thực.
  - Firebase Storage: Lưu trữ hình ảnh và tệp mô hình 3D (.glb).
- **Google Sceneform & ARCore**: Động cơ kết xuất đồ họa 3D Filament hiệu năng cao.
- **Glide**: Tải và tối ưu hóa bộ nhớ đệm hình ảnh mượt mà.

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

1. Clone dự án về máy:
   ```bash
   git clone https://github.com/taichi2006/APP.git
   ```
2. Mở dự án trong **Android Studio** (Hedgehog hoặc mới hơn).
3. Kết nối thiết bị Android hoặc máy ảo hỗ trợ **Google Play Services for AR (ARCore)**.
4. Chờ Gradle đồng bộ và nhấn **Run** (`Shift + F10`) để khởi chạy.
