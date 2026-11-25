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

    public ListenerRegistration listenAllOrders(Callback<List<Order>> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return null;
        }

        return firestore.collection("user").document(uid).collection("orders")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        if (callback != null) callback.onError(error.getMessage());
                        return;
                    }
                    if (querySnapshot != null) {
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
                    }
                });
    }

    public void getAllOrders(Callback<List<Order>> callback) {
        listenAllOrders(callback);
    }

    public void placeOrder(Order order, Callback<Order> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        String orderIdStr = order.getOrderCode();
        WriteBatch batch = firestore.batch();

        // 1. Lưu vào collection con: user/{uid}/orders/{orderCode}
        DocumentReference userOrderRef = firestore.collection("user").document(uid).collection("orders").document(orderIdStr);
        batch.set(userOrderRef, order);

        // 2. Lưu vào bảng độc lập: orders/{orderCode}
        DocumentReference globalOrderRef = firestore.collection("orders").document(orderIdStr);
        batch.set(globalOrderRef, order);

        // 3. Lưu vào bảng chi tiết: order_details/{orderCode_productId}
        if (order.getProducts() != null) {
            int dtlIdx = 1;
            for (CartProduct cp : order.getProducts()) {
                if (cp != null && cp.getProduct() != null) {
                    String detailId = orderIdStr + "_" + String.format(Locale.US, "DTL%03d", dtlIdx++);
                    Map<String, Object> d = new HashMap<>();
                    d.put("detailId", detailId); d.put("orderCode", order.getOrderCode()); d.put("orderId", order.getOrderId());
                    d.put("productId", cp.getProduct().getId()); d.put("productName", cp.getProduct().getName());
                    d.put("productPrice", cp.getProduct().getPrice()); d.put("quantity", cp.getQuantity());
                    d.put("selectedColor", cp.getSelectedColor()); d.put("selectedSize", cp.getSelectedSize());
                    d.put("userId", uid); d.put("date", order.getDate());
                    batch.set(firestore.collection("order_details").document(detailId), d);
                }
            }
        }

        // 4. Lưu vào bảng giao dịch thanh toán: payments/PAY{orderId}
        String paymentId = String.format(Locale.US, "PAY%03d", order.getOrderId());
        String email = auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null ? auth.getCurrentUser().getEmail() : "";
        String status = ("Sandbox".equalsIgnoreCase(order.getPaymentMethod()) || "Đã thanh toán".equalsIgnoreCase(order.getPaymentStatus())) ? "SUCCESS" : "PENDING";
        String bankCode = "Sandbox".equalsIgnoreCase(order.getPaymentMethod()) ? "SANDBOX_GATEWAY" : "COD";

        Payment payment = new Payment(paymentId, order.getOrderId(), uid, email, (double) order.getTotalPrice(),
                order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD", status,
                "TXN_" + System.currentTimeMillis(), bankCode, "Thanh toán đơn hàng " + orderIdStr);
        batch.set(firestore.collection("payments").document(paymentId), payment);

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

        String orderIdStr = Order.formatOrderCode(orderId);
        WriteBatch batch = firestore.batch();

        DocumentReference userOrderRef = firestore.collection("user").document(uid).collection("orders").document(orderIdStr);
        batch.update(userOrderRef, "orderStatus", "Đã hủy");

        DocumentReference globalOrderRef = firestore.collection("orders").document(orderIdStr);
        batch.update(globalOrderRef, "orderStatus", "Đã hủy");

        String paymentId = String.format(Locale.US, "PAY%03d", orderId);
        batch.update(firestore.collection("payments").document(paymentId), "status", "CANCELLED");

        batch.commit()
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(null); })
                .addOnFailureListener(e -> firestore.collection("user").document(uid).collection("orders")
                        .whereEqualTo("orderId", orderId).get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                querySnapshot.getDocuments().get(0).getReference().update("orderStatus", "Đã hủy")
                                        .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(null); });
                            } else if (callback != null) {
                                callback.onError("Không tìm thấy mã đơn hàng " + orderIdStr);
                            }
                        })
                        .addOnFailureListener(err -> { if (callback != null) callback.onError(err != null ? err.getMessage() : "Lỗi hủy đơn"); }));
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
                                if (addr.getId() == null || addr.getId().isEmpty()) addr.setId(doc.getId());
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

        if (address.getId() != null && !address.getId().trim().isEmpty()) {
            firestore.collection("user").document(uid).collection("address").document(address.getId()).set(address)
                    .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(address); })
                    .addOnFailureListener(err -> { if (callback != null) callback.onError(err != null ? err.getMessage() : "Lỗi lưu địa chỉ"); });
        } else {
            firestore.collection("user").document(uid).collection("address").get()
                    .addOnSuccessListener(snap -> {
                        String adrId = String.format(Locale.US, "ADR%03d", (snap != null ? snap.size() : 0) + 1);
                        address.setId(adrId);
                        firestore.collection("user").document(uid).collection("address").document(adrId).set(address)
                                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(address); })
                                .addOnFailureListener(err -> { if (callback != null) callback.onError(err != null ? err.getMessage() : "Lỗi lưu địa chỉ"); });
                    })
                    .addOnFailureListener(err -> {
                        address.setId("ADR001");
                        firestore.collection("user").document(uid).collection("address").document("ADR001").set(address)
                                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(address); })
                                .addOnFailureListener(e -> { if (callback != null) callback.onError(e != null ? e.getMessage() : "Lỗi lưu địa chỉ"); });
                    });
        }
    }

    public void deleteAddress(Address address, Callback<Void> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        if (address != null && address.getId() != null && !address.getId().trim().isEmpty()) {
            firestore.collection("user").document(uid).collection("address").document(address.getId()).delete()
                    .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(null); })
                    .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
        } else if (address != null) {
            firestore.collection("user").document(uid).collection("address")
                    .whereEqualTo("street", address.getStreet()).get()
                    .addOnSuccessListener(snap -> {
                        for (DocumentSnapshot doc : snap.getDocuments()) doc.getReference().delete();
                        if (callback != null) callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
        }
    }

    public void clearAllOrdersInSystem(Callback<Void> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        firestore.collection("user").document(uid).collection("orders").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) doc.getReference().delete();
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e.getMessage()); });
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
        if (order.isReviewed()) {
            if (callback != null) callback.onError("Đơn hàng này đã được đánh giá rồi!");
            return;
        }

        String orderCode = order.getOrderCode();
        firestore.collection("user").document(uid).collection("orders").document(orderCode).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getBoolean("reviewed"))) {
                        order.setReviewed(true);
                        if (callback != null) callback.onError("Đơn hàng này đã được đánh giá rồi!");
                        return;
                    }
                    executeSubmitReview(order, rating, comment, callback, uid, orderCode);
                })
                .addOnFailureListener(e -> executeSubmitReview(order, rating, comment, callback, uid, orderCode));
    }

    private void executeSubmitReview(Order order, float rating, String comment, Callback<Void> callback, String uid, String orderCode) {
        String userName = (order.getAddress() != null && order.getAddress().getFullName() != null && !order.getAddress().getFullName().isEmpty())
                ? order.getAddress().getFullName()
                : (auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null ? auth.getCurrentUser().getDisplayName() : "Khách hàng");
        String userEmail = auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null ? auth.getCurrentUser().getEmail() : "";
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        firestore.collection("reviews").get().addOnCompleteListener(task -> {
            int currentRevCount = (task.isSuccessful() && task.getResult() != null) ? task.getResult().size() : 30;
            WriteBatch batch = firestore.batch();
            int count = 0;
            if (order.getProducts() != null) {
                for (CartProduct cp : order.getProducts()) {
                    if (cp != null && cp.getProduct() != null && cp.getProduct().getId() != null) {
                        String pid = cp.getProduct().getId();
                        String revId = String.format(Locale.US, "REV%03d", ++currentRevCount);
                        batch.set(firestore.collection("reviews").document(revId), new Review(revId, pid, orderCode, userName, userEmail, rating, comment, date));
                        count++;
                    }
                }
            }
            if (count == 0) {
                String revId = String.format(Locale.US, "REV%03d", ++currentRevCount);
                batch.set(firestore.collection("reviews").document(revId), new Review(revId, "sofa_01", orderCode, userName, userEmail, rating, comment, date));
            }

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("reviewed", true);
            batch.set(firestore.collection("user").document(uid).collection("orders").document(orderCode), updateMap, com.google.firebase.firestore.SetOptions.merge());
            batch.set(firestore.collection("orders").document(orderCode), updateMap, com.google.firebase.firestore.SetOptions.merge());

            batch.commit().addOnSuccessListener(aVoid -> {
                order.setReviewed(true);
                if (callback != null) callback.onSuccess(null);
            }).addOnFailureListener(e -> {
                if (callback != null) callback.onError(e != null ? e.getMessage() : "Lỗi lưu đánh giá");
            });
        });
    }
}
