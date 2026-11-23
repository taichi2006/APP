package com.example.feature.order;

import androidx.lifecycle.ViewModel;
import com.example.data.Address;
import com.example.util.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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
public class AddressViewModel extends ViewModel {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final MutableStateFlow<Resource<Address>> _addNewAddress;
    private final MutableSharedFlow<String> _error;

    @Inject
    public AddressViewModel(FirebaseFirestore firestore, FirebaseAuth auth) {
        this.firestore = firestore;
        this.auth = auth;
        this._addNewAddress = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        this._error = SharedFlowKt.MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST);
    }

    public StateFlow<Resource<Address>> getDdNewAddress() {
        return _addNewAddress;
    }

    public SharedFlow<String> getError() {
        return _error;
    }

    public void addAddress(Address address) {
        if (!validateInput(address)) {
            _error.tryEmit("Vui lòng điền đầy đủ các thông tin địa chỉ!");
            return;
        }

        _addNewAddress.setValue(new Resource.Loading<>());
        String uid = auth.getUid();
        if (uid == null) {
            _addNewAddress.setValue(new Resource.Error<>("Chưa đăng nhập"));
            return;
        }

        firestore.collection("user").document(uid).collection("address").document().set(address)
            .addOnSuccessListener(aVoid -> {
                _addNewAddress.setValue(new Resource.Success<>(address));
            })
            .addOnFailureListener(e -> {
                _addNewAddress.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Lỗi lưu địa chỉ"));
            });
    }

    private boolean validateInput(Address address) {
        if (address == null) return false;
        return address.getAddressTitle() != null && !address.getAddressTitle().trim().isEmpty() &&
               address.getCity() != null && !address.getCity().trim().isEmpty() &&
               address.getStreet() != null && !address.getStreet().trim().isEmpty() &&
               address.getState() != null && !address.getState().trim().isEmpty() &&
               address.getFullName() != null && !address.getFullName().trim().isEmpty() &&
               address.getPhoneNumb() != null && !address.getPhoneNumb().trim().isEmpty();
    }
}
