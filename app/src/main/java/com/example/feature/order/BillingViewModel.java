package com.example.feature.order;

import androidx.lifecycle.ViewModel;
import com.example.data.Address;
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
public class BillingViewModel extends ViewModel {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final MutableStateFlow<Resource<List<Address>>> _addresses;

    @Inject
    public BillingViewModel(FirebaseFirestore firestore, FirebaseAuth auth) {
        this.firestore = firestore;
        this.auth = auth;
        this._addresses = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        getUserAddresses();
    }

    public StateFlow<Resource<List<Address>>> getAddresses() {
        return _addresses;
    }

    public void getUserAddresses() {
        _addresses.setValue(new Resource.Loading<>());
        String uid = auth.getUid();
        if (uid == null) {
            _addresses.setValue(new Resource.Error<>("Chưa đăng nhập"));
            return;
        }

        firestore.collection("user").document(uid).collection("address")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    _addresses.setValue(new Resource.Error<>(error.getMessage() != null ? error.getMessage() : "Lỗi tải địa chỉ"));
                    return;
                }
                if (value != null) {
                    List<Address> addresses = value.toObjects(Address.class);
                    _addresses.setValue(new Resource.Success<>(addresses));
                }
            });
    }
}
