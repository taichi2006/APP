package com.example.feature.home;

import androidx.lifecycle.ViewModel;
import com.example.data.Category;
import com.example.data.Product;
import com.example.util.Resource;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

public class CategoryViewModel extends ViewModel {
    private final FirebaseFirestore firestore;
    private final Category category;

    private final MutableStateFlow<Resource<List<Product>>> _offerProducts;
    private final MutableStateFlow<Resource<List<Product>>> _bestProducts;

    public CategoryViewModel(FirebaseFirestore firestore, Category category) {
        this.firestore = firestore;
        this.category = category;
        this._offerProducts = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        this._bestProducts = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());

        fetchBestProducts();
        fetchOfferProducts();
    }

    public StateFlow<Resource<List<Product>>> getOfferProducts() {
        return _offerProducts;
    }

    public StateFlow<Resource<List<Product>>> getBestProducts() {
        return _bestProducts;
    }

    public void fetchOfferProducts() {
        _offerProducts.setValue(new Resource.Loading<>());
        String catName = category != null ? category.getCategory() : "";

        firestore.collection("Products").whereEqualTo("category", catName).whereNotEqualTo("offerPercentage", null)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Product> products = querySnapshot.toObjects(Product.class);
                _offerProducts.setValue(new Resource.Success<>(products));
            })
            .addOnFailureListener(e -> {
                _offerProducts.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tải sản phẩm"));
            });
    }

    public void fetchBestProducts() {
        _bestProducts.setValue(new Resource.Loading<>());
        String catName = category != null ? category.getCategory() : "";

        firestore.collection("Products").whereEqualTo("category", catName).whereEqualTo("offerPercentage", null)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Product> products = querySnapshot.toObjects(Product.class);
                _bestProducts.setValue(new Resource.Success<>(products));
            })
            .addOnFailureListener(e -> {
                _bestProducts.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tải sản phẩm"));
            });
    }
}
