package com.example.core.util;

import android.util.Log;

import com.example.model.CartProduct;
import com.example.model.Order;
import com.example.model.Payment;
import com.example.model.Product;
import com.example.model.Review;
import com.example.model.Voucher;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
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
        seedAllData(onComplete);
    }

    public static void seedProducts() {
        seedAllData(null);
    }

    public static void seedAllData(OnSeedCompleteListener onComplete) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        WriteBatch batch = firestore.batch();

        // 1. Seed Products (10 items)
        List<Product> products = DatabaseSampleData.getSampleProducts();
        for (Product product : products) {
            batch.set(firestore.collection("Products").document(product.getId()), product);
        }

        // 2. Seed Vouchers (4 items)
        List<Voucher> vouchers = DatabaseSampleData.getSampleVouchers();
        for (Voucher voucher : vouchers) {
            batch.set(firestore.collection("vouchers").document(voucher.getCode()), voucher);
        }

        // 3. Seed Sample Orders, Order Details, Payments
        List<Order> orders = DatabaseSampleData.getSampleOrders();
        for (Order order : orders) {
            String oIdStr = String.valueOf(order.getOrderId());
            batch.set(firestore.collection("orders").document(oIdStr), order);

            if (order.getProducts() != null) {
                for (CartProduct cp : order.getProducts()) {
                    if (cp != null && cp.getProduct() != null) {
                        String detailId = oIdStr + "_" + cp.getProduct().getId();
                        DocumentReference detailRef = firestore.collection("order_details").document(detailId);
                        Map<String, Object> detailData = new HashMap<>();
                        detailData.put("orderId", order.getOrderId());
                        detailData.put("productId", cp.getProduct().getId());
                        detailData.put("productName", cp.getProduct().getName());
                        detailData.put("productPrice", cp.getProduct().getPrice());
                        detailData.put("quantity", cp.getQuantity());
                        detailData.put("selectedColor", cp.getSelectedColor());
                        detailData.put("selectedSize", cp.getSelectedSize());
                        detailData.put("userId", "USR_DEMO");
                        detailData.put("date", order.getDate());
                        batch.set(detailRef, detailData);
                    }
                }
            }
        }

        List<Payment> payments = DatabaseSampleData.getSamplePayments();
        for (Payment payment : payments) {
            batch.set(firestore.collection("payments").document(payment.getPaymentId()), payment);
        }

        // Commit batch 1
        batch.commit().addOnSuccessListener(aVoid -> {
            // 4. Seed Reviews in batch 2 (30 items)
            WriteBatch reviewBatch = firestore.batch();
            List<Review> reviews = DatabaseSampleData.getSampleReviews();
            for (Review review : reviews) {
                reviewBatch.set(firestore.collection("reviews").document(review.getId()), review);
            }
            reviewBatch.commit().addOnSuccessListener(v -> {
                Log.d(TAG, "Seeded Products, Vouchers, Reviews, Orders, Order_details, Payments successfully to Firestore!");
                if (onComplete != null) onComplete.onComplete(true, "Seeded all collections successfully");
            }).addOnFailureListener(e -> {
                if (onComplete != null) onComplete.onComplete(true, "Seeded base collections");
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error seeding Firestore data: " + e.getMessage());
            if (onComplete != null) onComplete.onComplete(false, e.getMessage());
        });
    }
}
