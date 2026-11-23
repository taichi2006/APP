package com.example.feature.product;

import androidx.lifecycle.ViewModel;
import com.example.data.CartProduct;
import com.example.firebase.FirebaseCommon;
import com.example.util.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

@HiltViewModel
public class DetailsViewModel extends ViewModel {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final FirebaseCommon firebaseCommon;
    private final MutableStateFlow<Resource<CartProduct>> _addToCart;

    @Inject
    public DetailsViewModel(FirebaseFirestore firestore, FirebaseAuth auth, FirebaseCommon firebaseCommon) {
        this.firestore = firestore;
        this.auth = auth;
        this.firebaseCommon = firebaseCommon;
        this._addToCart = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
    }

    public StateFlow<Resource<CartProduct>> getAddToCart() {
        return _addToCart;
    }

    public void addUpdateProductInCart(CartProduct cartProduct) {
        String uid = auth.getUid();
        if (uid == null || uid.trim().isEmpty()) {
            _addToCart.setValue(new Resource.Error<>("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng"));
            return;
        }

        _addToCart.setValue(new Resource.Loading<>());

        String productId = cartProduct.getProduct() != null ? cartProduct.getProduct().getId() : "";
        firestore.collection("user").document(uid).collection("cart")
            .whereEqualTo("product.id", productId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                DocumentSnapshot matchingDoc = null;
                CartProduct existingMatch = null;

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    CartProduct existing = doc.toObject(CartProduct.class);
                    if (existing != null &&
                        existing.getProduct() != null &&
                        Objects.equals(existing.getProduct().getId(), productId) &&
                        Objects.equals(existing.getSelectedColor(), cartProduct.getSelectedColor()) &&
                        Objects.equals(existing.getSelectedSize(), cartProduct.getSelectedSize())) {
                        matchingDoc = doc;
                        existingMatch = existing;
                        break;
                    }
                }

                if (matchingDoc != null && existingMatch != null) {
                    int newQuantity = existingMatch.getQuantity() + cartProduct.getQuantity();
                    final CartProduct updated = existingMatch.copy(newQuantity);
                    firestore.collection("user").document(uid).collection("cart")
                        .document(matchingDoc.getId())
                        .update("quantity", newQuantity)
                        .addOnSuccessListener(aVoid -> {
                            _addToCart.setValue(new Resource.Success<>(updated));
                        })
                        .addOnFailureListener(e -> {
                            _addToCart.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi cập nhật giỏ hàng"));
                        });
                } else {
                    addNewProduct(cartProduct);
                }
            })
            .addOnFailureListener(e -> {
                _addToCart.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi kết nối"));
            });
    }

    private void addNewProduct(CartProduct cartProduct) {
        firebaseCommon.addProductToCart(cartProduct, (addedProduct, e) -> {
            if (e == null && addedProduct != null) {
                _addToCart.setValue(new Resource.Success<>(addedProduct));
            } else {
                _addToCart.setValue(new Resource.Error<>(e != null ? e.getMessage() : "Lỗi thêm giỏ hàng"));
            }
        });
    }
}
