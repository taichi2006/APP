package com.example.feature.cart;

import androidx.lifecycle.ViewModel;
import com.example.data.CartProduct;
import com.example.firebase.FirebaseCommon;
import com.example.util.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.channels.BufferOverflow;
import kotlinx.coroutines.flow.MutableSharedFlow;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.SharedFlow;
import kotlinx.coroutines.flow.SharedFlowKt;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

@HiltViewModel
public class CartViewModel extends ViewModel {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final FirebaseCommon firebaseCommon;

    private final MutableStateFlow<Resource<List<CartProduct>>> _cartProducts;
    private final MutableStateFlow<Float> _productPrice;
    private final MutableSharedFlow<CartProduct> _deleteDialog;

    private List<DocumentSnapshot> cartProductDocuments = new ArrayList<>();

    @Inject
    public CartViewModel(FirebaseFirestore firestore, FirebaseAuth auth, FirebaseCommon firebaseCommon) {
        this.firestore = firestore;
        this.auth = auth;
        this.firebaseCommon = firebaseCommon;
        this._cartProducts = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        this._productPrice = StateFlowKt.MutableStateFlow(0f);
        this._deleteDialog = SharedFlowKt.MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST);

        fetchCartProducts();
    }

    public StateFlow<Resource<List<CartProduct>>> getCartProducts() {
        return _cartProducts;
    }

    public StateFlow<Float> getProductPrice() {
        return _productPrice;
    }

    public SharedFlow<CartProduct> getDeleteDialog() {
        return _deleteDialog;
    }

    public void deleteCartProduct(CartProduct cartProduct) {
        Resource<List<CartProduct>> current = _cartProducts.getValue();
        if (current != null && current.getData() != null) {
            int index = current.getData().indexOf(cartProduct);
            if (index != -1 && index < cartProductDocuments.size()) {
                String documentId = cartProductDocuments.get(index).getId();
                String uid = auth.getUid();
                if (uid != null) {
                    firestore.collection("user").document(uid).collection("cart")
                        .document(documentId).delete();
                }
            }
        }
    }

    private float calculatePrice(List<CartProduct> data) {
        float sum = 0f;
        if (data != null) {
            for (CartProduct cp : data) {
                if (cp != null && cp.getProduct() != null && cp.getQuantity() > 0) {
                    float price = cp.getProduct().getPrice();
                    Float offer = cp.getProduct().getOfferPercentage();
                    if (offer != null && offer > 0) {
                        price = (1f - offer) * price;
                    }
                    sum += price * cp.getQuantity();
                }
            }
        }
        return sum;
    }

    private void fetchCartProducts() {
        _cartProducts.setValue(new Resource.Loading<>());
        String uid = auth.getUid();
        if (uid == null) {
            _cartProducts.setValue(new Resource.Error<>("Chưa đăng nhập"));
            return;
        }

        firestore.collection("user").document(uid).collection("cart")
            .addSnapshotListener((value, error) -> {
                if (error != null || value == null) {
                    _cartProducts.setValue(new Resource.Error<>(error != null ? error.getMessage() : "Lỗi tải giỏ hàng"));
                } else {
                    cartProductDocuments = value.getDocuments();
                    List<CartProduct> products = value.toObjects(CartProduct.class);
                    List<CartProduct> validProducts = new ArrayList<>();
                    for (int i = 0; i < products.size(); i++) {
                        CartProduct cp = products.get(i);
                        if (cp != null) {
                            if (cp.getQuantity() <= 0) {
                                if (i < cartProductDocuments.size()) {
                                    cartProductDocuments.get(i).getReference().delete();
                                }
                            } else {
                                validProducts.add(cp);
                            }
                        }
                    }
                    _productPrice.setValue(calculatePrice(validProducts));
                    _cartProducts.setValue(new Resource.Success<>(validProducts));
                }
            });
    }

    public void changeQuantity(CartProduct cartProduct, FirebaseCommon.QuantityChanging quantityChanging) {
        Resource<List<CartProduct>> current = _cartProducts.getValue();
        if (current != null && current.getData() != null) {
            int index = current.getData().indexOf(cartProduct);
            if (index != -1 && index < cartProductDocuments.size()) {
                String documentId = cartProductDocuments.get(index).getId();
                if (quantityChanging == FirebaseCommon.QuantityChanging.INCREASE) {
                    _cartProducts.setValue(new Resource.Loading<>());
                    increaseQuantity(documentId);
                } else if (quantityChanging == FirebaseCommon.QuantityChanging.DECREASE) {
                    if (cartProduct.getQuantity() <= 1) {
                        _deleteDialog.tryEmit(cartProduct);
                        return;
                    }
                    _cartProducts.setValue(new Resource.Loading<>());
                    decreaseQuantity(documentId);
                }
            }
        }
    }

    private void decreaseQuantity(String documentId) {
        firebaseCommon.decreaseQuantity(documentId, (result, e) -> {
            if (e != null) {
                _cartProducts.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi giảm số lượng"));
            }
        });
    }

    private void increaseQuantity(String documentId) {
        firebaseCommon.increaseQuantity(documentId, (result, e) -> {
            if (e != null) {
                _cartProducts.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tăng số lượng"));
            }
        });
    }
}
