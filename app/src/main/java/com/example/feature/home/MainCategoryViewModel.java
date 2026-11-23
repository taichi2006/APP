package com.example.feature.home;

import androidx.lifecycle.ViewModel;
import com.example.data.Product;
import com.example.util.Resource;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

@HiltViewModel
public class MainCategoryViewModel extends ViewModel {
    private final FirebaseFirestore firestore;

    private final MutableStateFlow<Resource<List<Product>>> _specialProducts;
    private final MutableStateFlow<Resource<List<Product>>> _bestDealsProducts;
    private final MutableStateFlow<Resource<List<Product>>> _bestProducts;

    private final PagingInfo pagingInfo = new PagingInfo();

    @Inject
    public MainCategoryViewModel(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this._specialProducts = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        this._bestDealsProducts = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        this._bestProducts = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());

        fetchSpecialProducts();
        fetchBestDealsProducts();
        fetchBestProducts();
    }

    public StateFlow<Resource<List<Product>>> getSpecialProducts() {
        return _specialProducts;
    }

    public StateFlow<Resource<List<Product>>> getBestDealsProducts() {
        return _bestDealsProducts;
    }

    public StateFlow<Resource<List<Product>>> getBestProducts() {
        return _bestProducts;
    }

    public void fetchSpecialProducts() {
        _specialProducts.setValue(new Resource.Loading<>());

        // Deep discount items: where offerPercentage > 0, sorted by discount percentage
        firestore.collection("Products").get()
            .addOnSuccessListener(result -> {
                List<Product> allProducts = result.toObjects(Product.class);
                List<Product> discounted = new ArrayList<>();
                for (Product p : allProducts) {
                    if (p.getOfferPercentage() != null && p.getOfferPercentage() > 0) {
                        discounted.add(p);
                    }
                }
                // Sort descending by discount percentage
                Collections.sort(discounted, (a, b) -> Float.compare(
                    b.getOfferPercentage() != null ? b.getOfferPercentage() : 0f,
                    a.getOfferPercentage() != null ? a.getOfferPercentage() : 0f
                ));

                // If not enough discounted items, fallback to top items
                if (discounted.isEmpty()) {
                    discounted = allProducts.subList(0, Math.min(allProducts.size(), 6));
                }
                _specialProducts.setValue(new Resource.Success<>(discounted));
            })
            .addOnFailureListener(e -> {
                _specialProducts.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tải khuyến mãi"));
            });
    }

    public void fetchBestDealsProducts() {
        _bestDealsProducts.setValue(new Resource.Loading<>());
        firestore.collection("Products").whereEqualTo("category", "Sofa").get()
            .addOnSuccessListener(result -> {
                List<Product> bestDealsList = result.toObjects(Product.class);
                _bestDealsProducts.setValue(new Resource.Success<>(bestDealsList));
            })
            .addOnFailureListener(e -> {
                _bestDealsProducts.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tải ưu đãi"));
            });
    }

    public void fetchBestProducts() {
        if (!pagingInfo.isPagingEnd) {
            _bestProducts.setValue(new Resource.Loading<>());
            firestore.collection("Products").limit(pagingInfo.bestProductsPage * 10).get()
                .addOnSuccessListener(result -> {
                    List<Product> bestProductList = result.toObjects(Product.class);
                    pagingInfo.isPagingEnd = bestProductList.equals(pagingInfo.oldBestProducts);
                    pagingInfo.oldBestProducts = bestProductList;
                    _bestProducts.setValue(new Resource.Success<>(bestProductList));
                    pagingInfo.bestProductsPage++;
                })
                .addOnFailureListener(e -> {
                    _bestProducts.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tải sản phẩm"));
                });
        }
    }

    static class PagingInfo {
        long bestProductsPage = 1;
        List<Product> oldBestProducts = new ArrayList<>();
        boolean isPagingEnd = false;
    }
}
