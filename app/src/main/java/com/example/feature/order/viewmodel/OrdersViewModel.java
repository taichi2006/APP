package com.example.feature.order.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.feature.order.repository.OrderRepository;
import com.example.model.Order;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OrdersViewModel extends ViewModel {

    // ==========================================
    // 1. Khởi tạo & Dependencies
    // ==========================================
    private final OrderRepository orderRepository;
    private final MutableLiveData<Resource<List<Order>>> ordersLiveData = new MutableLiveData<>();

    @Inject
    public OrdersViewModel(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        getAllOrders();
    }

    // ==========================================
    // 2. Getters LiveData cho UI
    // ==========================================
    public LiveData<Resource<List<Order>>> getOrdersLiveData() {
        return ordersLiveData;
    }

    // ==========================================
    // 3. Nghiệp vụ Quản lý Đơn hàng
    // ==========================================
    public void getAllOrders() {
        ordersLiveData.setValue(new Resource.Loading<>());

        orderRepository.getAllOrders(new OrderRepository.Callback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> result) {
                ordersLiveData.setValue(new Resource.Success<>(result));
            }

            @Override
            public void onError(String message) {
                ordersLiveData.setValue(new Resource.Error<>(message != null ? message : "Lỗi tải đơn hàng"));
            }
        });
    }

    public interface DeleteAllOrdersCallback {
        void onComplete(boolean success, String message);
    }

    public void clearAllOrdersInSystem(DeleteAllOrdersCallback callback) {
        orderRepository.clearAllOrdersInSystem(new OrderRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                getAllOrders();
                if (callback != null) callback.onComplete(true, "Đã xóa toàn bộ đơn hàng cũ");
            }

            @Override
            public void onError(String message) {
                getAllOrders();
                if (callback != null) callback.onComplete(true, "Đã xóa toàn bộ đơn hàng cũ");
            }
        });
    }

    public interface CancelOrderCallback {
        void onResult(boolean success, String message);
    }

    public void cancelOrder(long orderId, CancelOrderCallback callback) {
        orderRepository.cancelOrder(orderId, new OrderRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                getAllOrders();
                if (callback != null) callback.onResult(true, null);
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onResult(false, message);
            }
        });
    }

    public interface SubmitReviewCallback {
        void onResult(boolean success, String message);
    }

    public void submitOrderReview(Order order, float rating, String comment, SubmitReviewCallback callback) {
        orderRepository.submitOrderReview(order, rating, comment, new OrderRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                getAllOrders();
                if (callback != null) callback.onResult(true, null);
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onResult(false, message);
            }
        });
    }
}
