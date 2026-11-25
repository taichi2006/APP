package com.example.feature.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.feature.auth.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    // ==========================================
    // 1. Khởi tạo & Dependency
    // ==========================================
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<FirebaseUser>> loginLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> resetPasswordLiveData = new MutableLiveData<>();

    @Inject
    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // ==========================================
    // 2. Getters LiveData cho UI quan sát
    // ==========================================
    public LiveData<Resource<FirebaseUser>> getLoginLiveData() {
        return loginLiveData;
    }

    public LiveData<Resource<String>> getResetPasswordLiveData() {
        return resetPasswordLiveData;
    }

    // ==========================================
    // 3. Nghiệp vụ Đăng nhập & Đặt lại mật khẩu
    // ==========================================
    public void login(String email, String password) {
        loginLiveData.setValue(new Resource.Loading<>());

        authRepository.login(email, password, new AuthRepository.Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (user != null) {
                    loginLiveData.setValue(new Resource.Success<>(user));
                }
            }

            @Override
            public void onError(String message) {
                String msg = message != null ? message : "Đăng nhập thất bại";
                if (msg.toLowerCase().contains("user-not-found") || msg.toLowerCase().contains("no user record")) {
                    msg = "Không tìm thấy tài khoản với email này.";
                } else if (msg.toLowerCase().contains("wrong-password") || msg.toLowerCase().contains("password is invalid")) {
                    msg = "Mật khẩu không chính xác.";
                } else if (msg.toLowerCase().contains("disabled")) {
                    msg = "Tài khoản này đã bị vô hiệu hóa.";
                }
                loginLiveData.setValue(new Resource.Error<>(msg));
            }
        });
    }

    public void signInWithGoogle(String idToken) {
        loginLiveData.setValue(new Resource.Loading<>());

        authRepository.signInWithGoogle(idToken, new AuthRepository.Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                loginLiveData.setValue(new Resource.Success<>(user));
            }

            @Override
            public void onError(String message) {
                String err = message != null ? message : "Đăng nhập Google thất bại";
                loginLiveData.setValue(new Resource.Error<>(err));
            }
        });
    }

    public void resetPassword(String email) {
        resetPasswordLiveData.setValue(new Resource.Loading<>());

        authRepository.resetPassword(email, new AuthRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                resetPasswordLiveData.setValue(new Resource.Success<>(email));
            }

            @Override
            public void onError(String message) {
                String err = message != null ? message : "Không thể gửi email đặt lại mật khẩu";
                resetPasswordLiveData.setValue(new Resource.Error<>(err));
            }
        });
    }
}
