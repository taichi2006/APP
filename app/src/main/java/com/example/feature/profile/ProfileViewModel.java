package com.example.feature.profile;

import androidx.lifecycle.ViewModel;
import com.example.data.User;
import com.example.util.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final MutableStateFlow<Resource<User>> _user;

    @Inject
    public ProfileViewModel(FirebaseFirestore firestore, FirebaseAuth auth) {
        this.firestore = firestore;
        this.auth = auth;
        this._user = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        loadUser();
    }

    public StateFlow<Resource<User>> getUser() {
        return _user;
    }

    public void loadUser() {
        _user.setValue(new Resource.Loading<>());
        String uid = auth.getUid();
        if (uid == null) {
            _user.setValue(new Resource.Error<>("Chưa đăng nhập"));
            return;
        }

        firestore.collection("user").document(uid)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    _user.setValue(new Resource.Error<>(error.getMessage() != null ? error.getMessage() : "Lỗi tải người dùng"));
                } else if (value != null) {
                    User user = value.toObject(User.class);
                    if (user != null) {
                        _user.setValue(new Resource.Success<>(user));
                    }
                }
            });
    }

    public void signOut() {
        auth.signOut();
    }
}
