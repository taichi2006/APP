package com.example.feature.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.feature.home.repository.HomeRepository;
import com.example.model.Product;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainCategoryViewModel extends ViewModel {

    private final HomeRepository homeRepository;

    private final MutableLiveData<Resource<List<Product>>> specialProductsLiveData = new MutableLiveData<>(new Resource.Unspecified<>());
    private final MutableLiveData<Resource<List<Product>>> bestDealsProductsLiveData = new MutableLiveData<>(new Resource.Unspecified<>());
    private final MutableLiveData<Resource<List<Product>>> bestProductsLiveData = new MutableLiveData<>(new Resource.Unspecified<>());

    private final PagingInfo pagingInfo = new PagingInfo();

    @Inject
    public MainCategoryViewModel(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;

        fetchSpecialProducts();
        fetchBestDealsProducts();
        fetchBestProducts();
    }

    public LiveData<Resource<List<Product>>> getSpecialProductsLiveData() {
        return specialProductsLiveData;
    }

    public LiveData<Resource<List<Product>>> getBestDealsProductsLiveData() {
        return bestDealsProductsLiveData;
    }

    public LiveData<Resource<List<Product>>> getBestProductsLiveData() {
        return bestProductsLiveData;
    }

    public void fetchSpecialProducts() {
        specialProductsLiveData.setValue(new Resource.Loading<>());
        homeRepository.fetchSpecialProducts(new HomeRepository.Callback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                specialProductsLiveData.setValue(new Resource.Success<>(products));
            }

            @Override
            public void onError(String message) {
                specialProductsLiveData.setValue(new Resource.Error<>(message));
            }
        });
    }

    public void fetchBestDealsProducts() {
        bestDealsProductsLiveData.setValue(new Resource.Loading<>());
        homeRepository.fetchBestDealsProducts(new HomeRepository.Callback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                bestDealsProductsLiveData.setValue(new Resource.Success<>(products));
            }

            @Override
            public void onError(String message) {
                bestDealsProductsLiveData.setValue(new Resource.Error<>(message));
            }
        });
    }

    public void fetchBestProducts() {
        if (!pagingInfo.isPagingEnd) {
            bestProductsLiveData.setValue(new Resource.Loading<>());
            long limit = pagingInfo.bestProductPage * 10;
            homeRepository.fetchBestProducts(limit, new HomeRepository.Callback<List<Product>>() {
                @Override
                public void onSuccess(List<Product> products) {
                    if (products == null || products.isEmpty() || products.equals(pagingInfo.oldBestProducts)) {
                        pagingInfo.isPagingEnd = true;
                    }
                    pagingInfo.oldBestProducts = (products != null) ? products : new ArrayList<>();
                    bestProductsLiveData.setValue(new Resource.Success<>(new ArrayList<>(pagingInfo.oldBestProducts)));
                    pagingInfo.bestProductPage++;
                }

                @Override
                public void onError(String message) {
                    bestProductsLiveData.setValue(new Resource.Error<>(message));
                }
            });
        }
    }

    public void resetPaging() {
        pagingInfo.bestProductPage = 1;
        pagingInfo.oldBestProducts.clear();
        pagingInfo.isPagingEnd = false;
    }

    public static class PagingInfo {
        public long bestProductPage = 1;
        public List<Product> oldBestProducts = new ArrayList<>();
        public boolean isPagingEnd = false;
    }
}
