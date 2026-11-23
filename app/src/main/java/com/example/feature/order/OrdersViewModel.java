package com.example.feature.order;

import androidx.lifecycle.ViewModel;
import com.example.data.order.Order;
import com.example.util.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

@HiltViewModel
public class OrdersViewModel extends ViewModel {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final MutableStateFlow<Resource<List<Order>>> _orders;

    @Inject
    public OrdersViewModel(FirebaseFirestore firestore, FirebaseAuth auth) {
        this.firestore = firestore;
        this.auth = auth;
        this._orders = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        getAllOrders();
    }

    public StateFlow<Resource<List<Order>>> getOrders() {
        return _orders;
    }

    public void getAllOrders() {
        _orders.setValue(new Resource.Loading<>());
        String uid = auth.getUid();
        if (uid == null) {
            _orders.setValue(new Resource.Error<>("Chưa đăng nhập"));
            return;
        }

        firestore.collection("user").document(uid).collection("orders")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Order> orders = querySnapshot.toObjects(Order.class);
                _orders.setValue(new Resource.Success<>(orders));
            })
            .addOnFailureListener(e -> {
                _orders.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi tải đơn hàng"));
            });
    }

    public interface CancelOrderCallback {
        void onResult(boolean success, String message);
    }

    public void cancelOrder(long orderId, CancelOrderCallback callback) {
        String uid = auth.getUid();
        if (uid == null) {
            if (callback != null) callback.onResult(false, "Chưa đăng nhập");
            return;
        }

        firestore.collection("user").document(uid).collection("orders")
            .whereEqualTo("orderId", orderId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    querySnapshot.getDocuments().get(0).getReference()
                        .update("orderStatus", "Đã hủy")
                        .addOnSuccessListener(aVoid -> {
                            getAllOrders();
                            if (callback != null) callback.onResult(true, null);
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onResult(false, e.getMessage());
                        });
                } else {
                    if (callback != null) callback.onResult(false, "Không tìm thấy mã đơn hàng #" + orderId);
                }
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onResult(false, e.getMessage());
            });
    }
}
