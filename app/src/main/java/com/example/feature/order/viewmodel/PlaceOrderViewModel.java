package com.example.feature.order.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.feature.order.repository.OrderRepository;
import com.example.model.Order;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PlaceOrderViewModel extends ViewModel {

    // ==========================================
    // 1. Khởi tạo & State
    // ==========================================
    private final OrderRepository orderRepository;
    private final MutableLiveData<Resource<Order>> orderLiveData = new MutableLiveData<>();

    @Inject
    public PlaceOrderViewModel(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ==========================================
    // 2. Getters LiveData
    // ==========================================
    public LiveData<Resource<Order>> getOrderLiveData() {
        return orderLiveData;
    }

    // ==========================================
    // 3. Nghiệp vụ đặt hàng
    // ==========================================
    public void placeOrder(Order order) {
        orderLiveData.postValue(new Resource.Loading<>());

        orderRepository.placeOrder(order, new OrderRepository.Callback<Order>() {
            @Override
            public void onSuccess(Order result) {
                orderLiveData.postValue(new Resource.Success<>(result));
            }

            @Override
            public void onError(String message) {
                orderLiveData.postValue(new Resource.Error<>(message != null ? message : "Lỗi đặt hàng"));
            }
        });
    }
}
