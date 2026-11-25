package com.example.feature.order.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.core.util.ValidationCheck;
import com.example.feature.order.repository.OrderRepository;
import com.example.model.Address;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddressViewModel extends ViewModel {

    private final OrderRepository orderRepository;

    private final MutableLiveData<Resource<Address>> addNewAddressLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> deleteAddressLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    @Inject
    public AddressViewModel(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public LiveData<Resource<Address>> getAddNewAddressLiveData() {
        return addNewAddressLiveData;
    }

    public LiveData<Resource<Void>> getDeleteAddressLiveData() {
        return deleteAddressLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void addAddress(Address address) {
        if (!validateInput(address)) {
            errorLiveData.setValue("Vui lòng điền số điện thoại và địa chỉ nhận hàng!");
            return;
        }
        if (!ValidationCheck.isValidVietnamesePhone(address.getPhoneNumb())) {
            errorLiveData.setValue("Số điện thoại không hợp lệ (cần 10 chữ số, ví dụ 0901234567)!");
            return;
        }

        if (address.getFullName() == null || address.getFullName().trim().isEmpty()) {
            address.setFullName("Khách hàng");
        }
        if (address.getAddressTitle() == null || address.getAddressTitle().trim().isEmpty()) {
            address.setAddressTitle("Địa chỉ nhận hàng");
        }
        if (address.getCity() == null) address.setCity("");
        if (address.getState() == null) address.setState("");

        addNewAddressLiveData.setValue(new Resource.Loading<>());

        orderRepository.addAddress(address, new OrderRepository.Callback<Address>() {
            @Override
            public void onSuccess(Address result) {
                addNewAddressLiveData.setValue(new Resource.Success<>(result));
            }

            @Override
            public void onError(String message) {
                addNewAddressLiveData.setValue(new Resource.Error<>(message != null ? message : "Lỗi lưu địa chỉ"));
            }
        });
    }

    public void deleteAddress(Address address) {
        if (address == null) return;
        deleteAddressLiveData.setValue(new Resource.Loading<>());
        orderRepository.deleteAddress(address, new OrderRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                deleteAddressLiveData.setValue(new Resource.Success<>(null));
            }

            @Override
            public void onError(String message) {
                deleteAddressLiveData.setValue(new Resource.Error<>(message != null ? message : "Lỗi xóa địa chỉ"));
            }
        });
    }

    private boolean validateInput(Address address) {
        if (address == null) return false;
        String phone = address.getPhoneNumb();
        String street = address.getStreet();
        String city = address.getCity();
        return phone != null && !phone.trim().isEmpty() &&
                ((street != null && !street.trim().isEmpty()) || (city != null && !city.trim().isEmpty()));
    }
}
