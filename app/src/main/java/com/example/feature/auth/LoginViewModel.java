package com.example.feature.auth;

import androidx.lifecycle.ViewModel;
import com.example.data.User;
import com.example.util.Constants;
import com.example.util.Resource;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.channels.BufferOverflow;
import kotlinx.coroutines.flow.MutableSharedFlow;
import kotlinx.coroutines.flow.SharedFlow;
import kotlinx.coroutines.flow.SharedFlowKt;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    private final MutableSharedFlow<Resource<FirebaseUser>> _login;
    private final MutableSharedFlow<Resource<String>> _resetPassword;

    @Inject
    public LoginViewModel(FirebaseAuth firebaseAuth, FirebaseFirestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
        this._login = SharedFlowKt.MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST);
        this._resetPassword = SharedFlowKt.MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST);
    }

    public SharedFlow<Resource<FirebaseUser>> getLogin() {
        return _login;
    }

    public SharedFlow<Resource<String>> getResetPassword() {
        return _resetPassword;
    }

    public void login(String email, String password) {
        _login.tryEmit(new Resource.Loading<>());

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
                if (user != null) {
                    _login.tryEmit(new Resource.Success<>(user));
                }
            })
            .addOnFailureListener(e -> {
                String err = e.getMessage() != null ? e.getMessage() : "Đăng nhập thất bại";
                String msg = err;
                if (err.toLowerCase().contains("user-not-found") || err.toLowerCase().contains("no user record")) {
                    msg = "Không tìm thấy tài khoản với email này.";
                } else if (err.toLowerCase().contains("wrong-password") || err.toLowerCase().contains("password is invalid")) {
                    msg = "Mật khẩu không chính xác.";
                } else if (err.toLowerCase().contains("disabled")) {
                    msg = "Tài khoản này đã bị vô hiệu hóa hoặc chưa bật tính năng trên Firebase.";
                }
                _login.tryEmit(new Resource.Error<>(msg));
            });
    }

    public void signInWithGoogle(String idToken) {
        _login.tryEmit(new Resource.Loading<>());
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener(authResult -> {
                FirebaseUser fbUser = authResult.getUser();
                if (fbUser != null) {
                    firestore.collection(Constants.USER_COLLECTION).document(fbUser.getUid()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) {
                                String displayName = fbUser.getDisplayName() != null ? fbUser.getDisplayName().trim() : "";
                                String[] parts = displayName.split(" ");
                                String firstName = parts.length > 0 ? parts[0] : "";
                                StringBuilder lastNameSb = new StringBuilder();
                                for (int i = 1; i < parts.length; i++) {
                                    lastNameSb.append(parts[i]).append(i < parts.length - 1 ? " " : "");
                                }
                                User newUser = new User(
                                    firstName,
                                    lastNameSb.toString(),
                                    fbUser.getEmail() != null ? fbUser.getEmail() : "",
                                    fbUser.getPhotoUrl() != null ? fbUser.getPhotoUrl().toString() : ""
                                );
                                firestore.collection(Constants.USER_COLLECTION).document(fbUser.getUid()).set(newUser);
                            }
                            _login.tryEmit(new Resource.Success<>(fbUser));
                        })
                        .addOnFailureListener(e -> {
                            _login.tryEmit(new Resource.Success<>(fbUser));
                        });
                }
            })
            .addOnFailureListener(e -> {
                _login.tryEmit(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Đăng nhập Google thất bại"));
            });
    }

    public void resetPassword(String email) {
        _resetPassword.tryEmit(new Resource.Loading<>());
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener(aVoid -> {
                _resetPassword.tryEmit(new Resource.Success<>(email));
            })
            .addOnFailureListener(e -> {
                _resetPassword.tryEmit(new Resource.Error<>(e.getMessage() != null ? e.getMessage() : "Không thể gửi email đặt lại mật khẩu"));
            });
    }
}
