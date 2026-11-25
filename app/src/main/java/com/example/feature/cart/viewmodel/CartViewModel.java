package com.example.feature.cart.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.firebase.FirebaseCommon;
import com.example.core.util.PriceCalculator;
import com.example.core.util.Resource;
import com.example.feature.cart.repository.CartRepository;
import com.example.model.CartProduct;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CartViewModel extends ViewModel {

    // ==========================================
    // 1. Khởi tạo & Dependencies
    // ==========================================
    private final CartRepository cartRepository;
    private final FirebaseCommon firebaseCommon;

    private final MutableLiveData<Resource<List<CartProduct>>> cartProductsLiveData = new MutableLiveData<>(new Resource.Unspecified<>());
    private final MutableLiveData<Float> productPriceLiveData = new MutableLiveData<>(0f);
    private final MutableLiveData<CartProduct> deleteDialogLiveData = new MutableLiveData<>();

    private List<DocumentSnapshot> cartProductDocuments = new ArrayList<>();

    @Inject
    public CartViewModel(CartRepository cartRepository, FirebaseCommon firebaseCommon) {
        this.cartRepository = cartRepository;
        this.firebaseCommon = firebaseCommon;

        fetchCartProducts();
    }

    // ==========================================
    // 2. Getters LiveData cho UI quan sát
    // ==========================================
    public LiveData<Resource<List<CartProduct>>> getCartProductsLiveData() {
        return cartProductsLiveData;
    }

    public LiveData<Float> getProductPriceLiveData() {
        return productPriceLiveData;
    }

    public LiveData<CartProduct> getDeleteDialogLiveData() {
        return deleteDialogLiveData;
    }

    // ==========================================
    // 3. Nghiệp vụ Giỏ hàng (Thêm / Bớt / Xóa)
    // ==========================================
    public void deleteCartProduct(CartProduct cartProduct) {
        Resource<List<CartProduct>> current = cartProductsLiveData.getValue();
        if (current != null && current.getData() != null) {
            int index = current.getData().indexOf(cartProduct);
            if (index != -1 && index < cartProductDocuments.size()) {
                String documentId = cartProductDocuments.get(index).getId();
                cartRepository.deleteCartProduct(documentId, new CartRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {}

                    @Override
                    public void onError(String message) {}
                });
            }
        }
    }

    public void changeQuantity(CartProduct cartProduct, FirebaseCommon.QuantityChanging quantityChanging) {
        Resource<List<CartProduct>> current = cartProductsLiveData.getValue();
        if (current != null && current.getData() != null) {
            int index = current.getData().indexOf(cartProduct);
            if (index != -1 && index < cartProductDocuments.size()) {
                String documentId = cartProductDocuments.get(index).getId();
                switch (quantityChanging) {
                    case INCREASE:
                        cartProductsLiveData.setValue(new Resource.Loading<>());
                        increaseQuantity(documentId);
                        break;
                    case DECREASE:
                        if (cartProduct.getQuantity() == 1) {
                            deleteDialogLiveData.setValue(cartProduct);
                            return;
                        }
                        cartProductsLiveData.setValue(new Resource.Loading<>());
                        decreaseQuantity(documentId);
                        break;
                }
            }
        }
    }

    private void decreaseQuantity(String documentId) {
        firebaseCommon.decreaseQuantity(documentId, (result, exception) -> {
            if (exception != null) {
                cartProductsLiveData.setValue(new Resource.Error<>(exception.getMessage()));
            }
        });
    }

    private void increaseQuantity(String documentId) {
        firebaseCommon.increaseQuantity(documentId, (result, exception) -> {
            if (exception != null) {
                cartProductsLiveData.setValue(new Resource.Error<>(exception.getMessage()));
            }
        });
    }

    private void fetchCartProducts() {
        cartProductsLiveData.setValue(new Resource.Loading<>());

        cartRepository.listenCartChanges(new CartRepository.OnCartUpdatedListener() {
            @Override
            public void onUpdated(List<CartProduct> products, List<DocumentSnapshot> documents) {
                cartProductDocuments = documents;
                calculatePrice(products);
                cartProductsLiveData.setValue(new Resource.Success<>(products));
            }

            @Override
            public void onError(String message) {
                cartProductsLiveData.setValue(new Resource.Error<>(message != null ? message : "Lỗi giỏ hàng"));
            }
        });
    }

    private void calculatePrice(List<CartProduct> data) {
        float total = 0f;
        if (data != null) {
            for (CartProduct cp : data) {
                if (cp != null && cp.getProduct() != null) {
                    float price = PriceCalculator.getProductPrice(cp.getProduct().getOfferPercentage(), cp.getProduct().getPrice());
                    total += price * cp.getQuantity();
                }
            }
        }
        productPriceLiveData.setValue(total);
    }
}
