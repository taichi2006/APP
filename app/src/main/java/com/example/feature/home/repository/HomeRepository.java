package com.example.feature.home.repository;

import com.example.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HomeRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final FirebaseFirestore firestore;

    @Inject
    public HomeRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void fetchSpecialProducts(Callback<List<Product>> callback) {
        firestore.collection("Products").get()
                .addOnSuccessListener(result -> {
                    List<Product> allProducts = result.toObjects(Product.class);
                    List<Product> discounted = new ArrayList<>();
                    for (Product p : allProducts) {
                        if (p.getOfferPercentage() != null && p.getOfferPercentage() > 0) {
                            discounted.add(p);
                        }
                    }
                    Collections.sort(discounted, (a, b) -> Float.compare(
                            b.getOfferPercentage() != null ? b.getOfferPercentage() : 0f,
                            a.getOfferPercentage() != null ? a.getOfferPercentage() : 0f
                    ));

                    if (discounted.isEmpty()) {
                        discounted = allProducts.subList(0, Math.min(allProducts.size(), 6));
                    }
                    if (callback != null) callback.onSuccess(discounted);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi tải khuyến mãi");
                });
    }

    public void fetchBestDealsProducts(Callback<List<Product>> callback) {
        firestore.collection("Products").whereEqualTo("category", "Sofa").get()
                .addOnSuccessListener(result -> {
                    List<Product> bestDealsList = result.toObjects(Product.class);
                    if (bestDealsList.isEmpty()) {
                        firestore.collection("Products").limit(4).get()
                                .addOnSuccessListener(fallback -> {
                                    if (callback != null) callback.onSuccess(fallback.toObjects(Product.class));
                                });
                    } else if (callback != null) {
                        callback.onSuccess(bestDealsList);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi tải ưu đãi");
                });
    }

    public void fetchBestProducts(long limit, Callback<List<Product>> callback) {
        firestore.collection("Products").limit(limit).get()
                .addOnSuccessListener(result -> {
                    List<Product> bestProductList = result.toObjects(Product.class);
                    if (callback != null) callback.onSuccess(bestProductList);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi tải sản phẩm");
                });
    }

    public void fetchCategoryOfferProducts(String category, Callback<List<Product>> callback) {
        firestore.collection("Products")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> all = querySnapshot.toObjects(Product.class);
                    List<Product> filtered = new ArrayList<>();
                    for (Product p : all) {
                        if (p.getOfferPercentage() != null && p.getOfferPercentage() > 0) {
                            filtered.add(p);
                        }
                    }
                    if (callback != null) callback.onSuccess(filtered);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi tải sản phẩm");
                });
    }

    public void fetchCategoryBestProducts(String category, Callback<List<Product>> callback) {
        firestore.collection("Products")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> all = querySnapshot.toObjects(Product.class);
                    List<Product> filtered = new ArrayList<>();
                    for (Product p : all) {
                        if (p.getOfferPercentage() == null || p.getOfferPercentage() == 0) {
                            filtered.add(p);
                        }
                    }
                    if (filtered.isEmpty()) filtered = all;
                    if (callback != null) callback.onSuccess(filtered);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage() != null ? e.getMessage() : "Lỗi tải sản phẩm");
                });
    }
}
