package com.example.feature.order.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.feature.order.repository.OrderRepository;
import com.example.model.Address;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BillingViewModel extends ViewModel {

    // ==========================================
    // 1. Khởi tạo & State
    // ==========================================
    private final OrderRepository orderRepository;
    private final MutableLiveData<Resource<List<Address>>> addressesLiveData = new MutableLiveData<>();

    @Inject
    public BillingViewModel(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        getUserAddresses();
    }

    // ==========================================
    // 2. Getters LiveData
    // ==========================================
    public LiveData<Resource<List<Address>>> getAddressesLiveData() {
        return addressesLiveData;
    }

    // ==========================================
    // 3. Nghiệp vụ Order / Address
    // ==========================================
    public void getUserAddresses() {
        addressesLiveData.postValue(new Resource.Loading<>());

        orderRepository.listenAddresses(new OrderRepository.OnAddressesListener() {
            @Override
            public void onLoaded(List<Address> addresses) {
                addressesLiveData.postValue(new Resource.Success<>(addresses));
            }

            @Override
            public void onError(String message) {
                addressesLiveData.postValue(new Resource.Error<>(message != null ? message : "Lỗi tải địa chỉ"));
            }
        });
    }
}
