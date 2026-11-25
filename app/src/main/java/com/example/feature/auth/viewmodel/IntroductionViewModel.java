package com.example.feature.auth.viewmodel;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aroom.R;
import com.example.core.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class IntroductionViewModel extends ViewModel {

    public static final int SHOPPING_ACTIVITY = 23;
    public static final int ACCOUNT_OPTIONS_FRAGMENT = R.id.action_introductionFragment_to_accountOptionsFragment;

    // ==========================================
    // 1. Khởi tạo & State
    // ==========================================
    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<Integer> navigateLiveData = new MutableLiveData<>(0);

    @Inject
    public IntroductionViewModel(SharedPreferences sharedPreferences, FirebaseAuth firebaseAuth) {
        this.sharedPreferences = sharedPreferences;

        boolean isBtnClicked = sharedPreferences.getBoolean(Constants.INTRODUCTION_KEY, false);
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            navigateLiveData.setValue(SHOPPING_ACTIVITY);
        } else if (isBtnClicked) {
            navigateLiveData.setValue(ACCOUNT_OPTIONS_FRAGMENT);
        }
    }

    // ==========================================
    // 2. Getters LiveData
    // ==========================================
    public LiveData<Integer> getNavigateLiveData() {
        return navigateLiveData;
    }

    public LiveData<Integer> getNavigate() {
        return navigateLiveData;
    }

    // ==========================================
    // 3. Nghiệp vụ bắt đầu
    // ==========================================
    public void startBtnClick() {
        sharedPreferences.edit().putBoolean(Constants.INTRODUCTION_KEY, true).apply();
    }
}
