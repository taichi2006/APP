package com.example.feature.profile.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.core.util.ValidationCheck;
import com.example.core.util.RegisterValidation;
import com.example.feature.profile.repository.ProfileRepository;
import com.example.model.User;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserAccountViewModel extends ViewModel {

    private final ProfileRepository profileRepository;
    private final MutableLiveData<Resource<User>> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<User>> updateInfoLiveData = new MutableLiveData<>();

    @Inject
    public UserAccountViewModel(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        getUserData();
    }

    public LiveData<Resource<User>> getUserLiveData() { return userLiveData; }
    public LiveData<Resource<User>> getUpdateInfoLiveData() { return updateInfoLiveData; }

    public void getUserData() {
        userLiveData.setValue(new Resource.Loading<>());
        profileRepository.getUserProfile(new ProfileRepository.Callback<User>() {
            @Override
            public void onSuccess(User result) {
                userLiveData.setValue(new Resource.Success<>(result));
            }

            @Override
            public void onError(String message) {
                userLiveData.setValue(new Resource.Error<>(message));
            }
        });
    }

    public void updateUser(User user, byte[] imageBytes) {
        boolean valid = ValidationCheck.validateEmail(user.getEmail()) instanceof RegisterValidation.Success
                && user.getFirstName() != null && !user.getFirstName().trim().isEmpty()
                && user.getLastName() != null && !user.getLastName().trim().isEmpty();

        if (!valid) {
            updateInfoLiveData.setValue(new Resource.Error<>("Vui lòng kiểm tra lại thông tin nhập"));
            return;
        }

        updateInfoLiveData.setValue(new Resource.Loading<>());
        profileRepository.updateUser(user, imageBytes, new ProfileRepository.Callback<User>() {
            @Override
            public void onSuccess(User result) {
                updateInfoLiveData.setValue(new Resource.Success<>(result));
            }

            @Override
            public void onError(String message) {
                updateInfoLiveData.setValue(new Resource.Error<>(message));
            }
        });
    }
}
