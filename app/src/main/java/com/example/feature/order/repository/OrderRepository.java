package com.example.feature.order.repository;

import com.example.model.Address;
import com.example.model.CartProduct;
import com.example.model.Order;
import com.example.model.Payment;
import com.example.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OrderRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public interface OnAddressesListener {
        void onLoaded(List<Address> addresses);
        void onError(String message);
    }

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    @Inject
    public OrderRepository(FirebaseFirestore firestore, FirebaseAuth auth) {
        this.firestore = firestore;
        this.auth = auth;
    }

    public String getCurrentUserId() {
        return auth.getUid();
    }

    public void getAllOrders(Callback<List<Order>> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        firestore.collection("user").document(uid).collection("orders").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Order> orders = querySnapshot.toObjects(Order.class);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    orders.sort((o1, o2) -> {
                        long t1 = 0, t2 = 0;
                        try { if (o1.getDate() != null) t1 = sdf.parse(o1.getDate()).getTime(); } catch (Exception ignored) {}
                        try { if (o2.getDate() != null) t2 = sdf.parse(o2.getDate()).getTime(); } catch (Exception ignored) {}
                        if (t1 != t2) return Long.compare(t2, t1);
                        return Long.compare(o2.getOrderId(), o1.getOrderId());
                    });
                    if (callback != null) callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void placeOrder(Order order, Callback<Order> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        String orderIdStr = String.valueOf(order.getOrderId());
        WriteBatch batch = firestore.batch();

        // 1. Lưu vào collection con: user/{uid}/orders/{orderId}
        DocumentReference userOrderRef = firestore.collection("user").document(uid).collection("orders").document(orderIdStr);
        batch.set(userOrderRef, order);

        // 2. Lưu vào bảng độc lập: orders/{orderId}
        DocumentReference globalOrderRef = firestore.collection("orders").document(orderIdStr);
        batch.set(globalOrderRef, order);

        // 3. Lưu vào bảng chi tiết: order_details/{orderId_productId}
        if (order.getProducts() != null) {
            for (CartProduct cp : order.getProducts()) {
                if (cp != null && cp.getProduct() != null) {
                    String detailId = orderIdStr + "_" + cp.getProduct().getId();
                    DocumentReference detailRef = firestore.collection("order_details").document(detailId);
                    Map<String, Object> detailData = new HashMap<>();
                    detailData.put("orderId", order.getOrderId());
                    detailData.put("productId", cp.getProduct().getId());
                    detailData.put("productName", cp.getProduct().getName());
                    detailData.put("productPrice", cp.getProduct().getPrice());
                    detailData.put("quantity", cp.getQuantity());
                    detailData.put("selectedColor", cp.getSelectedColor());
                    detailData.put("selectedSize", cp.getSelectedSize());
                    detailData.put("userId", uid);
                    detailData.put("date", order.getDate());
                    batch.set(detailRef, detailData);
                }
            }
        }

        // 4. Lưu vào bảng giao dịch thanh toán: payments/{paymentId}
        String paymentId = "PAY_" + orderIdStr;
        DocumentReference paymentRef = firestore.collection("payments").document(paymentId);
        String email = auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null
                ? auth.getCurrentUser().getEmail() : "";
        String status = ("Sandbox".equalsIgnoreCase(order.getPaymentMethod()) || "Đã thanh toán".equalsIgnoreCase(order.getPaymentStatus()))
                ? "SUCCESS" : "PENDING";
        String bankCode = "Sandbox".equalsIgnoreCase(order.getPaymentMethod()) ? "SANDBOX_GATEWAY" : "COD";

        Payment payment = new Payment(
                paymentId,
                order.getOrderId(),
                uid,
                email,
                (double) order.getTotalPrice(),
                order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD",
                status,
                "TXN_" + System.currentTimeMillis(),
                bankCode,
                "Thanh toán đơn hàng #" + orderIdStr
        );
        batch.set(paymentRef, payment);

        // Thực hiện ghi đồng thời 4 bảng Firestore
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Dọn dẹp giỏ hàng
                    firestore.collection("user").document(uid).collection("cart").get()
                            .addOnSuccessListener(snapshot -> {
                                for (DocumentSnapshot doc : snapshot.getDocuments()) doc.getReference().delete();
                            });
                    if (callback != null) callback.onSuccess(order);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void cancelOrder(long orderId, Callback<Void> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        String orderIdStr = String.valueOf(orderId);
        WriteBatch batch = firestore.batch();

        DocumentReference userOrderRef = firestore.collection("user").document(uid).collection("orders").document(orderIdStr);
        batch.update(userOrderRef, "orderStatus", "Đã hủy");

        DocumentReference globalOrderRef = firestore.collection("orders").document(orderIdStr);
        batch.update(globalOrderRef, "orderStatus", "Đã hủy");

        DocumentReference paymentRef = firestore.collection("payments").document("PAY_" + orderIdStr);
        batch.update(paymentRef, "status", "CANCELLED");

        batch.commit()
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(null); })
                .addOnFailureListener(e -> {
                    // Fallback query if document id was auto-generated
                    firestore.collection("user").document(uid).collection("orders")
                            .whereEqualTo("orderId", orderId).get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    querySnapshot.getDocuments().get(0).getReference().update("orderStatus", "Đã hủy")
                                            .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(null); });
                                } else if (callback != null) {
                                    callback.onError("Không tìm thấy mã đơn hàng #" + orderId);
                                }
                            })
                            .addOnFailureListener(err -> { if (callback != null) callback.onError(err.getMessage()); });
                });
    }

    public ListenerRegistration listenAddresses(OnAddressesListener listener) {
        String uid = auth.getUid();
        if (uid == null) {
            if (listener != null) listener.onError("Chưa đăng nhập");
            return null;
        }

        return firestore.collection("user").document(uid).collection("address")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (listener != null) listener.onError(error.getMessage() != null ? error.getMessage() : "Lỗi tải địa chỉ");
                        return;
                    }
                    if (value != null && listener != null) {
                        List<Address> addresses = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Address addr = doc.toObject(Address.class);
                            if (addr != null) {
                                if (addr.getId() == null || addr.getId().isEmpty()) {
                                    addr.setId(doc.getId());
                                }
                                addresses.add(addr);
                            }
                        }
                        listener.onLoaded(addresses);
                    }
                });
    }

    public void addAddress(Address address, Callback<Address> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        DocumentReference docRef;
        if (address.getId() != null && !address.getId().trim().isEmpty()) {
            docRef = firestore.collection("user").document(uid).collection("address").document(address.getId());
        } else {
            docRef = firestore.collection("user").document(uid).collection("address").document();
            address.setId(docRef.getId());
        }

        docRef.set(address)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(address);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi lưu địa chỉ");
                });
    }

    public void deleteAddress(Address address, Callback<Void> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        if (address != null && address.getId() != null && !address.getId().trim().isEmpty()) {
            firestore.collection("user").document(uid).collection("address").document(address.getId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onError(e.getMessage());
                    });
        } else if (address != null) {
            firestore.collection("user").document(uid).collection("address")
                    .whereEqualTo("street", address.getStreet())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            doc.getReference().delete();
                        }
                        if (callback != null) callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onError(e.getMessage());
                    });
        }
    }

    public void clearAllOrdersInSystem(Callback<Void> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        firestore.collection("user").document(uid).collection("orders")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void submitOrderReview(Order order, float rating, String comment, Callback<Void> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Vui lòng đăng nhập để gửi đánh giá");
            return;
        }
        if (order == null) {
            if (callback != null) callback.onError("Đơn hàng không hợp lệ");
            return;
        }

        String orderIdStr = String.valueOf(order.getOrderId());
        String userName = (order.getAddress() != null && order.getAddress().getFullName() != null && !order.getAddress().getFullName().isEmpty())
                ? order.getAddress().getFullName()
                : (auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null ? auth.getCurrentUser().getDisplayName() : "Khách hàng");
        String userEmail = auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null ? auth.getCurrentUser().getEmail() : "";
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        WriteBatch batch = firestore.batch();
        if (order.getProducts() != null && !order.getProducts().isEmpty()) {
            for (CartProduct cp : order.getProducts()) {
                if (cp != null && cp.getProduct() != null && cp.getProduct().getId() != null) {
                    String pid = cp.getProduct().getId();
                    String reviewId = "rev_" + orderIdStr + "_" + pid;
                    Review r = new Review(reviewId, pid, orderIdStr, userName, userEmail, rating, comment, date);
                    batch.set(firestore.collection("reviews").document(reviewId), r);
                }
            }
        }

        DocumentReference userOrderRef = firestore.collection("user").document(uid).collection("orders").document(orderIdStr);
        batch.update(userOrderRef, "reviewed", true);
        DocumentReference globalOrderRef = firestore.collection("orders").document(orderIdStr);
        batch.update(globalOrderRef, "reviewed", true);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    order.setReviewed(true);
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi lưu đánh giá");
                });
    }
}
