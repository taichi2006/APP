package com.example.feature.search.repository;

import com.example.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository tải danh sách sản phẩm phục vụ tìm kiếm và sắp xếp (100% Java)
 */
@Singleton
public class SearchRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final FirebaseFirestore firestore;

    @Inject
    public SearchRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void getAllProducts(Callback<List<Product>> callback) {
        firestore.collection("Products").get()
            .addOnSuccessListener(querySnapshot -> {
                List<Product> products = querySnapshot.toObjects(Product.class);
                if (callback != null) callback.onSuccess(products);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi tải sản phẩm");
            });
    }
}
