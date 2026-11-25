package com.example.feature.profile.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.core.util.Resource;
import com.example.feature.profile.repository.ProfileRepository;
import com.example.model.User;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    // ==========================================
    // 1. Khởi tạo & State
    // ==========================================
    private final ProfileRepository profileRepository;
    private final MutableLiveData<Resource<User>> userLiveData = new MutableLiveData<>();

    @Inject
    public ProfileViewModel(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        loadUser();
    }

    // ==========================================
    // 2. Getters LiveData
    // ==========================================
    public LiveData<Resource<User>> getUserLiveData() {
        return userLiveData;
    }

    // ==========================================
    // 3. Nghiệp vụ User Profile
    // ==========================================
    public void loadUser() {
        userLiveData.postValue(new Resource.Loading<>());

        profileRepository.getUserProfile(new ProfileRepository.Callback<User>() {
            @Override
            public void onSuccess(User user) {
                userLiveData.postValue(new Resource.Success<>(user));
            }

            @Override
            public void onError(String message) {
                userLiveData.postValue(new Resource.Error<>(message != null ? message : "Lỗi tải thông tin"));
            }
        });
    }

    public void signOut() {
        profileRepository.signOut();
    }
}
