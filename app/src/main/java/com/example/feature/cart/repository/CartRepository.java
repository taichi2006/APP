package com.example.feature.cart.repository;

import com.example.model.CartProduct;
import com.example.core.firebase.FirebaseCommon;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository xử lý toàn bộ tác vụ dữ liệu giỏ hàng trên Firestore bằng 100% Java:
 * - Lắng nghe thay đổi giỏ hàng theo thời gian thực (SnapshotListener)
 * - Tăng / giảm số lượng sản phẩm trong giỏ
 * - Xóa sản phẩm khỏi giỏ hàng
 */
@Singleton
public class CartRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public interface OnCartUpdatedListener {
        void onUpdated(List<CartProduct> products, List<DocumentSnapshot> documents);
        void onError(String message);
    }

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final FirebaseCommon firebaseCommon;

    @Inject
    public CartRepository(FirebaseFirestore firestore, FirebaseAuth auth, FirebaseCommon firebaseCommon) {
        this.firestore = firestore;
        this.auth = auth;
        this.firebaseCommon = firebaseCommon;
    }

    public String getCurrentUserId() {
        return auth.getUid();
    }

    public ListenerRegistration listenCartChanges(OnCartUpdatedListener listener) {
        String uid = auth.getUid();
        if (uid == null) {
            if (listener != null) listener.onError("Chưa đăng nhập");
            return null;
        }

        return firestore.collection("user").document(uid).collection("cart")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (listener != null) listener.onError(error.getMessage() != null ? error.getMessage() : "Lỗi tải giỏ hàng");
                        return;
                    }

                    List<CartProduct> products = new ArrayList<>();
                    List<DocumentSnapshot> documents = new ArrayList<>();
                    if (value != null) {
                        documents.addAll(value.getDocuments());
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CartProduct p = doc.toObject(CartProduct.class);
                            if (p != null) {
                                products.add(p);
                            }
                        }
                    }
                    if (listener != null) {
                        listener.onUpdated(products, documents);
                    }
                });
    }

    public void deleteCartProduct(String documentId, Callback<Void> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        firestore.collection("user").document(uid).collection("cart")
                .document(documentId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi xóa sản phẩm");
                });
    }

    public void updateQuantity(String documentId, int newQuantity, Callback<Void> callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        firestore.collection("user").document(uid).collection("cart")
                .document(documentId)
                .update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi cập nhật số lượng");
                });
    }
}
