package com.example.feature.product.repository;

import com.example.core.firebase.FirebaseCommon;
import com.example.model.CartProduct;
import com.example.model.Product;
import com.example.model.Review;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository xử lý dữ liệu Sản phẩm trực tiếp với Firebase Firestore (100% Java)
 */
@Singleton
public class ProductRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final FirebaseFirestore firestore;
    private final FirebaseCommon firebaseCommon;

    @Inject
    public ProductRepository(FirebaseFirestore firestore, FirebaseCommon firebaseCommon) {
        this.firestore = firestore;
        this.firebaseCommon = firebaseCommon;
    }

    public void getProductById(String productId, Callback<Product> callback) {
        if (productId == null || productId.trim().isEmpty()) {
            if (callback != null) callback.onError("Mã sản phẩm không hợp lệ");
            return;
        }

        firestore.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Product product = documentSnapshot.toObject(Product.class);
                    if (product != null) {
                        if (callback != null) callback.onSuccess(product);
                    } else {
                        if (callback != null) callback.onError("Không tìm thấy sản phẩm");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi tải sản phẩm");
                });
    }

    public void getReviewsForProduct(String productId, Callback<List<Review>> callback) {
        if (productId == null || productId.trim().isEmpty()) {
            if (callback != null) callback.onError("Mã sản phẩm không hợp lệ");
            return;
        }

        firestore.collection("reviews")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Review r = doc.toObject(Review.class);
                        if (r != null) {
                            reviews.add(r);
                        }
                    }
                    if (callback != null) callback.onSuccess(reviews);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi tải đánh giá");
                });
    }

    public void addOrUpdateCartProduct(String userId, CartProduct cartProduct, Callback<CartProduct> callback) {
        if (userId == null || userId.trim().isEmpty()) {
            if (callback != null) callback.onError("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
            return;
        }

        String productId = cartProduct.getProduct() != null ? cartProduct.getProduct().getId() : "";
        firestore.collection("user").document(userId).collection("cart")
                .whereEqualTo("product.id", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    DocumentSnapshot matchingDoc = null;
                    CartProduct existingMatch = null;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        CartProduct existing = doc.toObject(CartProduct.class);
                        if (existing != null &&
                                existing.getProduct() != null &&
                                Objects.equals(existing.getProduct().getId(), productId) &&
                                Objects.equals(existing.getSelectedColor(), cartProduct.getSelectedColor()) &&
                                Objects.equals(existing.getSelectedSize(), cartProduct.getSelectedSize())) {
                            matchingDoc = doc;
                            existingMatch = existing;
                            break;
                        }
                    }

                    if (matchingDoc != null && existingMatch != null) {
                        int newQuantity = existingMatch.getQuantity() + cartProduct.getQuantity();
                        final CartProduct updated = existingMatch.copy(newQuantity);
                        firestore.collection("user").document(userId).collection("cart")
                                .document(matchingDoc.getId())
                                .update("quantity", newQuantity)
                                .addOnSuccessListener(aVoid -> {
                                    if (callback != null) callback.onSuccess(updated);
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi cập nhật giỏ hàng");
                                });
                    } else {
                        firebaseCommon.addProductToCart(cartProduct, (addedProduct, e) -> {
                            if (e == null && addedProduct != null) {
                                if (callback != null) callback.onSuccess(addedProduct);
                            } else {
                                if (callback != null) callback.onError(e != null ? e.getMessage() : "Lỗi thêm giỏ hàng");
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi kết nối");
                });
    }
}
