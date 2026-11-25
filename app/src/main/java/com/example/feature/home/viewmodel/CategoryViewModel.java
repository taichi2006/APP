package com.example.feature.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.feature.home.repository.HomeRepository;
import com.example.model.Category;
import com.example.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CategoryViewModel extends ViewModel {

    // ==========================================
    // 1. Khởi tạo & Dependencies
    // ==========================================
    private final HomeRepository homeRepository;
    private final Category category;

    private final MutableLiveData<Resource<List<Product>>> offerProductsLiveData = new MutableLiveData<>(new Resource.Unspecified<>());
    private final MutableLiveData<Resource<List<Product>>> bestProductsLiveData = new MutableLiveData<>(new Resource.Unspecified<>());

    public CategoryViewModel(HomeRepository homeRepository, Category category) {
        this.homeRepository = homeRepository;
        this.category = category;

        fetchBestProducts();
        fetchOfferProducts();
    }

    public CategoryViewModel(FirebaseFirestore firestore, Category category) {
        this(new HomeRepository(firestore), category);
    }

    // ==========================================
    // 2. Getters LiveData
    // ==========================================
    public LiveData<Resource<List<Product>>> getOfferProductsLiveData() {
        return offerProductsLiveData;
    }

    public LiveData<Resource<List<Product>>> getBestProductsLiveData() {
        return bestProductsLiveData;
    }

    // ==========================================
    // 3. Nghiệp vụ Tải Sản phẩm theo Danh mục
    // ==========================================
    public void fetchOfferProducts() {
        offerProductsLiveData.setValue(new Resource.Loading<>());
        String catName = category != null ? category.getCategory() : "";

        homeRepository.fetchCategoryOfferProducts(catName, new HomeRepository.Callback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                offerProductsLiveData.setValue(new Resource.Success<>(products));
            }

            @Override
            public void onError(String message) {
                offerProductsLiveData.setValue(new Resource.Error<>(message));
            }
        });
    }

    public void fetchBestProducts() {
        bestProductsLiveData.setValue(new Resource.Loading<>());
        String catName = category != null ? category.getCategory() : "";

        homeRepository.fetchCategoryBestProducts(catName, new HomeRepository.Callback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                bestProductsLiveData.setValue(new Resource.Success<>(products));
            }

            @Override
            public void onError(String message) {
                bestProductsLiveData.setValue(new Resource.Error<>(message));
            }
        });
    }
}
