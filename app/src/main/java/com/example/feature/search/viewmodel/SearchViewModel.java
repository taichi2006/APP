package com.example.feature.search.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.feature.search.repository.SearchRepository;
import com.example.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {

    public enum SortType {
        SOLD_DESC,
        PRICE_ASC,
        PRICE_DESC,
        RATING_DESC,
        OFFER_DESC
    }

    // ==========================================
    // 1. Khởi tạo & State
    // ==========================================
    private final SearchRepository searchRepository;
    private final MutableLiveData<Resource<List<Product>>> searchLiveData = new MutableLiveData<>();

    private List<Product> allProducts = new ArrayList<>();
    private SortType currentSort = SortType.SOLD_DESC;
    private String currentQuery = "";

    @Inject
    public SearchViewModel(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
        loadAllProducts();
    }

    // ==========================================
    // 2. Getters LiveData & State
    // ==========================================
    public LiveData<Resource<List<Product>>> getSearchLiveData() {
        return searchLiveData;
    }

    public SortType getCurrentSort() {
        return currentSort;
    }

    // ==========================================
    // 3. Nghiệp vụ Search & Filter
    // ==========================================
    public void loadAllProducts() {
        searchLiveData.postValue(new Resource.Loading<>());

        searchRepository.getAllProducts(new SearchRepository.Callback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                allProducts = (products != null) ? new ArrayList<>(products) : new ArrayList<>();
                applyFilterAndSort();
            }

            @Override
            public void onError(String message) {
                searchLiveData.postValue(new Resource.Error<>(message != null ? message : "Lỗi tải sản phẩm"));
            }
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

        searchLiveData.postValue(new Resource.Success<>(filtered));
    }

    private float getEffectivePrice(Product p) {
        if (p == null) return 0f;
        if (p.getOfferPercentage() != null && p.getOfferPercentage() > 0) {
            return p.getPrice() * (1f - p.getOfferPercentage());
        }
        return p.getPrice();
    }
}
