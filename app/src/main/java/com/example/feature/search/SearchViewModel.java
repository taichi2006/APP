package com.example.feature.search;

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
public class SearchViewModel extends ViewModel {
    public enum SortType {
        SOLD_DESC,
        PRICE_ASC,
        PRICE_DESC,
        RATING_DESC,
        OFFER_DESC
    }

    private final FirebaseFirestore firestore;
    private final MutableStateFlow<Resource<List<Product>>> _search;
    private List<Product> allProducts = new ArrayList<>();
    private SortType currentSort = SortType.SOLD_DESC;
    private String currentQuery = "";

    @Inject
    public SearchViewModel(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this._search = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        loadAllProducts();
    }

    public StateFlow<Resource<List<Product>>> getSearch() {
        return _search;
    }

    public SortType getCurrentSort() {
        return currentSort;
    }

    public void loadAllProducts() {
        _search.setValue(new Resource.Loading<>());
        firestore.collection("Products").get()
            .addOnSuccessListener(querySnapshot -> {
                List<Product> products = querySnapshot.toObjects(Product.class);
                allProducts = new ArrayList<>(products);
                applyFilterAndSort();
            })
            .addOnFailureListener(e -> {
                _search.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tải sản phẩm"));
            });
    }

    public void setSortType(SortType sortType) {
        this.currentSort = sortType;
        applyFilterAndSort();
    }

    public void searchProduct(String query) {
        this.currentQuery = query != null ? query.trim() : "";
        applyFilterAndSort();
    }

    private void applyFilterAndSort() {
        if (allProducts == null || allProducts.isEmpty()) {
            return;
        }

        List<Product> filtered = new ArrayList<>();
        String cleanQuery = currentQuery.toLowerCase();

        for (Product p : allProducts) {
            if (p != null) {
                if (cleanQuery.isEmpty() || (p.getName() != null && p.getName().toLowerCase().contains(cleanQuery))) {
                    filtered.add(p);
                }
            }
        }

        // Apply sorting
        Collections.sort(filtered, (a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;

            switch (currentSort) {
                case PRICE_ASC: {
                    float priceA = getEffectivePrice(a);
                    float priceB = getEffectivePrice(b);
                    return Float.compare(priceA, priceB);
                }
                case PRICE_DESC: {
                    float priceA = getEffectivePrice(a);
                    float priceB = getEffectivePrice(b);
                    return Float.compare(priceB, priceA);
                }
                case RATING_DESC: {
                    return Float.compare(b.getRating(), a.getRating());
                }
                case OFFER_DESC: {
                    float offerA = a.getOfferPercentage() != null ? a.getOfferPercentage() : 0f;
                    float offerB = b.getOfferPercentage() != null ? b.getOfferPercentage() : 0f;
                    return Float.compare(offerB, offerA);
                }
                case SOLD_DESC:
                default: {
                    return Integer.compare(b.getSoldCount(), a.getSoldCount());
                }
            }
        });

        _search.setValue(new Resource.Success<>(filtered));
    }

    private float getEffectivePrice(Product p) {
        if (p == null) return 0f;
        if (p.getOfferPercentage() != null && p.getOfferPercentage() > 0) {
            return p.getPrice() * (1f - p.getOfferPercentage());
        }
        return p.getPrice();
    }
}
