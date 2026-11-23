package com.example.feature.order;

import androidx.lifecycle.ViewModel;
import com.example.data.order.Order;
import com.example.util.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

@HiltViewModel
public class PlaceOrderViewModel extends ViewModel {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final MutableStateFlow<Resource<Order>> _order;

    @Inject
    public PlaceOrderViewModel(FirebaseFirestore firestore, FirebaseAuth auth) {
        this.firestore = firestore;
        this.auth = auth;
        this._order = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
    }

    public StateFlow<Resource<Order>> getOrder() {
        return _order;
    }

    public void placeOrder(Order order) {
        _order.setValue(new Resource.Loading<>());
        String uid = auth.getUid();
        if (uid == null) {
            _order.setValue(new Resource.Error<>("Chưa đăng nhập"));
            return;
        }

        firestore.runBatch(batch -> {
            firestore.collection("user")
                .document(uid)
                .collection("orders")
                .document()
                .set(order);

            firestore.collection("orders").document().set(order);

            firestore.collection("user").document(uid)
                .collection("cart").get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.getDocuments().forEach(doc -> doc.getReference().delete());
                });
        }).addOnSuccessListener(aVoid -> {
            _order.setValue(new Resource.Success<>(order));
        }).addOnFailureListener(e -> {
            _order.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi đặt hàng"));
        });
    }
}
