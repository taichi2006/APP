package com.example.feature.auth;

import androidx.lifecycle.ViewModel;
import com.example.data.User;
import com.example.util.Constants;
import com.example.util.RegisterFieldsState;
import com.example.util.RegisterValidation;
import com.example.util.Resource;
import com.example.util.ValidationCheck;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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
public class RegisterViewModel extends ViewModel {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;

    private final MutableStateFlow<Resource<User>> _register;
    private final MutableSharedFlow<RegisterFieldsState> _validation;

    @Inject
    public RegisterViewModel(FirebaseAuth firebaseAuth, FirebaseFirestore db) {
        this.firebaseAuth = firebaseAuth;
        this.db = db;
        this._register = StateFlowKt.MutableStateFlow(new Resource.Unspecified<>());
        this._validation = SharedFlowKt.MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST);
    }

    public StateFlow<Resource<User>> getRegister() {
        return _register;
    }

    public SharedFlow<RegisterFieldsState> getValidation() {
        return _validation;
    }

    public void createAccountWithEmailAndPassword(User user, String password) {
        if (checkValidation(user, password)) {
            _register.setValue(new Resource.Loading<>());
            firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = authResult.getUser();
                    if (fbUser != null) {
                        saveUserInfo(fbUser.getUid(), user);
                    }
                })
                .addOnFailureListener(e -> {
                    String msg = formatAuthErrorMessage(e.getMessage());
                    _register.setValue(new Resource.Error<>(msg));
                });
        } else {
            RegisterFieldsState state = new RegisterFieldsState(
                ValidationCheck.validateEmail(user.getEmail()),
                ValidationCheck.validatePassword(password)
            );
            _validation.tryEmit(state);
        }
    }

    public void signInWithGoogle(String idToken) {
        _register.setValue(new Resource.Loading<>());
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener(authResult -> {
                FirebaseUser fbUser = authResult.getUser();
                if (fbUser != null) {
                    String displayName = fbUser.getDisplayName() != null ? fbUser.getDisplayName().trim() : "";
                    String[] parts = displayName.split(" ");
                    String firstName = parts.length > 0 ? parts[0] : "";
                    StringBuilder lastNameSb = new StringBuilder();
                    for (int i = 1; i < parts.length; i++) {
                        lastNameSb.append(parts[i]).append(i < parts.length - 1 ? " " : "");
                    }
                    User user = new User(
                        firstName,
                        lastNameSb.toString(),
                        fbUser.getEmail() != null ? fbUser.getEmail() : "",
                        fbUser.getPhotoUrl() != null ? fbUser.getPhotoUrl().toString() : ""
                    );
                    saveUserInfo(fbUser.getUid(), user);
                }
            })
            .addOnFailureListener(e -> {
                _register.setValue(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Đăng nhập Google thất bại"));
            });
    }

    private void saveUserInfo(String userUid, User user) {
        db.collection(Constants.USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                _register.setValue(new Resource.Success<>(user));
            })
            .addOnFailureListener(e -> {
                String errorStr = e.getMessage() != null ? e.getMessage() : "";
                if (errorStr.toLowerCase().contains("permission_denied") || errorStr.toLowerCase().contains("permissions")) {
                    _register.setValue(new Resource.Error<>("Lỗi quyền Firestore: Vui lòng vào Firebase Console -> Firestore Database -> Rules và bật 'allow read, write: if true;' để cho phép lưu tài khoản."));
                } else {
                    _register.setValue(new Resource.Error<>("Không thể lưu thông tin: " + errorStr));
                }
            });
    }

    private String formatAuthErrorMessage(String error) {
        String err = error != null ? error : "Đăng ký thất bại";
        if (err.toLowerCase().contains("already in use")) {
            return "Email này đã được sử dụng bởi một tài khoản khác.";
        } else if (err.toLowerCase().contains("disabled")) {
            return "Tính năng Email/Password chưa được bật trong Firebase Console (Authentication -> Sign-in method).";
        } else if (err.toLowerCase().contains("badly formatted")) {
            return "Định dạng email không hợp lệ.";
        } else if (err.toLowerCase().contains("weak")) {
            return "Mật khẩu quá yếu, vui lòng nhập mật khẩu phức tạp hơn.";
        }
        return err;
    }

    private boolean checkValidation(User user, String password) {
        RegisterValidation emailValidation = ValidationCheck.validateEmail(user.getEmail());
        RegisterValidation passwordValidation = ValidationCheck.validatePassword(password);

        return emailValidation instanceof RegisterValidation.Success &&
               passwordValidation instanceof RegisterValidation.Success;
    }
}
