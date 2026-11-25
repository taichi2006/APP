package com.example.feature.product.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.feature.product.repository.ProductRepository;
import com.example.model.CartProduct;
import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DetailsViewModel extends ViewModel {

    // ==========================================
    // 1. Khởi tạo & Dependencies
    // ==========================================
    private final ProductRepository productRepository;
    private final FirebaseAuth auth;

    private final MutableLiveData<Resource<CartProduct>> addToCartLiveData = new MutableLiveData<>();

    @Inject
    public DetailsViewModel(ProductRepository productRepository, FirebaseAuth auth) {
        this.productRepository = productRepository;
        this.auth = auth;
    }

    // ==========================================
    // 2. Getters LiveData cho UI
    // ==========================================
    public LiveData<Resource<CartProduct>> getAddToCartLiveData() {
        return addToCartLiveData;
    }

    // ==========================================
    // 3. Nghiệp vụ Thêm sản phẩm vào giỏ
    // ==========================================
    public void addUpdateProductInCart(CartProduct cartProduct) {
        String uid = auth.getUid();
        if (uid == null || uid.trim().isEmpty()) {
            addToCartLiveData.setValue(new Resource.Error<>("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng"));
            return;
        }

        addToCartLiveData.setValue(new Resource.Loading<>());

        productRepository.addOrUpdateCartProduct(uid, cartProduct, new ProductRepository.Callback<CartProduct>() {
            @Override
            public void onSuccess(CartProduct result) {
                addToCartLiveData.setValue(new Resource.Success<>(result));
            }

            @Override
            public void onError(String message) {
                addToCartLiveData.setValue(new Resource.Error<>(message != null ? message : "Lỗi thêm giỏ hàng"));
            }
        });
    }
}
