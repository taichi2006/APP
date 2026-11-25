package com.example.feature.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.RegisterFieldsState;
import com.example.core.util.RegisterValidation;
import com.example.core.util.Resource;
import com.example.core.util.ValidationCheck;
import com.example.feature.auth.repository.AuthRepository;
import com.example.model.User;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    // ==========================================
    // 1. Khởi tạo & Dependency
    // ==========================================
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<User>> registerLiveData = new MutableLiveData<>();
    private final MutableLiveData<RegisterFieldsState> validationLiveData = new MutableLiveData<>();

    @Inject
    public RegisterViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // ==========================================
    // 2. Getters LiveData cho UI
    // ==========================================
    public LiveData<Resource<User>> getRegisterLiveData() {
        return registerLiveData;
    }

    public LiveData<RegisterFieldsState> getValidationLiveData() {
        return validationLiveData;
    }

    // ==========================================
    // 3. Nghiệp vụ Đăng ký tài khoản
    // ==========================================
    public void createAccountWithEmailAndPassword(User user, String password) {
        if (checkValidation(user, password)) {
            registerLiveData.setValue(new Resource.Loading<>());

            authRepository.register(user, password, new AuthRepository.Callback<User>() {
                @Override
                public void onSuccess(User result) {
                    registerLiveData.setValue(new Resource.Success<>(result));
                }

                @Override
                public void onError(String message) {
                    registerLiveData.setValue(new Resource.Error<>(message != null ? message : "Đăng ký thất bại"));
                }
            });
        } else {
            RegisterFieldsState registerFieldsState = new RegisterFieldsState(
                    ValidationCheck.validateEmail(user.getEmail()),
                    ValidationCheck.validatePassword(password)
            );
            validationLiveData.setValue(registerFieldsState);
        }
    }

    public void signInWithGoogle(String idToken) {
        registerLiveData.setValue(new Resource.Loading<>());

        authRepository.signInWithGoogle(idToken, new AuthRepository.Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
                    String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "";
                    String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
                    String[] parts = name.split(" ", 2);
                    String firstName = parts.length > 0 ? parts[0] : "";
                    String lastName = parts.length > 1 ? parts[1] : "";
                    String imagePath = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "";

                    User user = new User(firstName, lastName, email, imagePath);
                    registerLiveData.setValue(new Resource.Success<>(user));
                }
            }

            @Override
            public void onError(String message) {
                registerLiveData.setValue(new Resource.Error<>(message != null ? message : "Đăng nhập Google thất bại"));
            }
        });
    }

    private boolean checkValidation(User user, String password) {
        RegisterValidation emailValidation = ValidationCheck.validateEmail(user.getEmail());
        RegisterValidation passwordValidation = ValidationCheck.validatePassword(password);
        return emailValidation instanceof RegisterValidation.Success &&
                passwordValidation instanceof RegisterValidation.Success;
    }
}
