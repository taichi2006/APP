package com.example.core.util;

import android.util.Log;

import com.example.model.Address;
import com.example.model.CartProduct;
import com.example.model.Order;
import com.example.model.Payment;
import com.example.model.Product;
import com.example.model.Review;
import com.example.model.Voucher;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";

    public interface OnSeedCompleteListener {
        void onComplete(boolean success, String message);
    }

    public static void seedAll() {
        seedAll(null);
    }

    public static void seedAll(OnSeedCompleteListener onComplete) {
        cleanAndSeed(onComplete);
    }

    public static void seedAllData(OnSeedCompleteListener onComplete) {
        cleanAndSeed(onComplete);
    }

    public static void cleanAndSeed(OnSeedCompleteListener onComplete) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String currentUid = FirebaseAuth.getInstance().getUid();

        // 1. Lấy danh sách tài liệu cần xóa từ các collection rác (reviews, orders, order_details, payments)
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        tasks.add(firestore.collection("reviews").get());
        tasks.add(firestore.collection("orders").get());
        tasks.add(firestore.collection("order_details").get());
        tasks.add(firestore.collection("payments").get());
        if (currentUid != null && !currentUid.isEmpty()) {
            tasks.add(firestore.collection("user").document(currentUid).collection("orders").get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            WriteBatch deleteBatch = firestore.batch();
            for (Object obj : results) {
                if (obj instanceof QuerySnapshot) {
                    for (DocumentSnapshot doc : ((QuerySnapshot) obj).getDocuments()) {
                        deleteBatch.delete(doc.getReference());
                    }
                }
            }
            deleteBatch.commit().addOnCompleteListener(deleteTask -> {
                Log.d(TAG, "Đã xóa sạch các collection cũ! Tiến hành seed dữ liệu chuẩn mới...");
                executeSeedCleanData(firestore, currentUid, onComplete);
            });
        }).addOnFailureListener(e -> {
            // Nếu xóa thất bại (ví dụ collection chưa tồn tại), vẫn seed bình thường
            executeSeedCleanData(firestore, currentUid, onComplete);
        });
    }

    private static void executeSeedCleanData(FirebaseFirestore firestore, String currentUid, OnSeedCompleteListener onComplete) {
        WriteBatch batch = firestore.batch();

        // 1. Products (10 món nội thất)
        List<Product> products = DatabaseSampleData.getSampleProducts();
        for (Product product : products) {
            batch.set(firestore.collection("Products").document(product.getId()), product);
        }

        // 2. Vouchers (4 mã giảm giá)
        List<Voucher> vouchers = DatabaseSampleData.getSampleVouchers();
        for (Voucher voucher : vouchers) {
            batch.set(firestore.collection("vouchers").document(voucher.getCode()), voucher);
        }

        // 3. 3 Đơn hàng mẫu cho tài khoản demo (ORD001 - Đã nhận, ORD002 - Đang giao, ORD003 - Đã đặt)
        List<Order> orders = DatabaseSampleData.getSampleOrders();
        int dtlIdx = 1;
        for (Order order : orders) {
            String oCode = order.getOrderCode();
            batch.set(firestore.collection("orders").document(oCode), order);

            if (currentUid != null && !currentUid.isEmpty()) {
                batch.set(firestore.collection("user").document(currentUid).collection("orders").document(oCode), order);
            }

            if (order.getProducts() != null) {
                for (CartProduct cp : order.getProducts()) {
                    if (cp != null && cp.getProduct() != null) {
                        String detailId = String.format(Locale.US, "DTL%03d", dtlIdx++);
                        DocumentReference detailRef = firestore.collection("order_details").document(detailId);
                        Map<String, Object> detailData = new HashMap<>();
                        detailData.put("detailId", detailId);
                        detailData.put("orderCode", order.getOrderCode());
                        detailData.put("orderId", order.getOrderId());
                        detailData.put("productId", cp.getProduct().getId());
                        detailData.put("productName", cp.getProduct().getName());
                        detailData.put("productPrice", cp.getProduct().getPrice());
                        detailData.put("quantity", cp.getQuantity());
                        detailData.put("selectedColor", cp.getSelectedColor());
                        detailData.put("selectedSize", cp.getSelectedSize());
                        detailData.put("userId", currentUid != null ? currentUid : "USR001");
                        detailData.put("date", order.getDate());
                        batch.set(detailRef, detailData);
                    }
                }
            }
        }

        // Địa chỉ mẫu cho user
        if (currentUid != null && !currentUid.isEmpty()) {
            Address defaultAddr = new Address("Nhà riêng", "Chí Tài", "12 Nguyễn Huệ, P. Bến Nghé", "0901234567", "Quận 1", "TP. Hồ Chí Minh");
            defaultAddr.setId("ADR001");
            batch.set(firestore.collection("user").document(currentUid).collection("address").document("ADR001"), defaultAddr);
        }

        // Thanh toán mẫu (PAY001, PAY002, PAY003)
        List<Payment> payments = DatabaseSampleData.getSamplePayments();
        for (Payment payment : payments) {
            batch.set(firestore.collection("payments").document(payment.getPaymentId()), payment);
        }

        // Commit batch 1
        batch.commit().addOnSuccessListener(aVoid -> {
            // 4. Seed Reviews chuẩn (30 đánh giá: REV001 -> REV030)
            WriteBatch reviewBatch = firestore.batch();
            List<Review> reviews = DatabaseSampleData.getSampleReviews();
            for (Review review : reviews) {
                reviewBatch.set(firestore.collection("reviews").document(review.getId()), review);
            }
            reviewBatch.commit().addOnSuccessListener(v -> {
                Log.d(TAG, "Đã dọn sạch database và seed lại data chuẩn thành công!");
                if (onComplete != null) onComplete.onComplete(true, "Dọn dẹp và nạp dữ liệu sạch thành công!");
            }).addOnFailureListener(e -> {
                if (onComplete != null) onComplete.onComplete(true, "Đã nạp phần lớn dữ liệu");
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi nạp dữ liệu: " + e.getMessage());
            if (onComplete != null) onComplete.onComplete(false, e.getMessage());
        });
    }
}
