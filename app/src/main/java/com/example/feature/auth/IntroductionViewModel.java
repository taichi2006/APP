package com.example.feature.auth;

import android.content.SharedPreferences;
import androidx.lifecycle.ViewModel;
import com.example.aroom.R;
import com.example.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

@HiltViewModel
public class IntroductionViewModel extends ViewModel {
    public static final int SHOPPING_ACTIVITY = 23;
    public static final int ACCOUNT_OPTIONS_FRAGMENT = R.id.action_introductionFragment_to_accountOptionsFragment;

    private final SharedPreferences sharedPreferences;
    private final MutableStateFlow<Integer> _navigate;

    @Inject
    public IntroductionViewModel(SharedPreferences sharedPreferences, FirebaseAuth firebaseAuth) {
        this.sharedPreferences = sharedPreferences;
        this._navigate = StateFlowKt.MutableStateFlow(0);

        boolean isBtnClicked = sharedPreferences.getBoolean(Constants.INTRODUCTION_KEY, false);
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            _navigate.setValue(SHOPPING_ACTIVITY);
        } else if (isBtnClicked) {
            _navigate.setValue(ACCOUNT_OPTIONS_FRAGMENT);
        }
    }

    public StateFlow<Integer> getNavigate() {
        return _navigate;
    }

    public void startBtnClick() {
        sharedPreferences.edit().putBoolean(Constants.INTRODUCTION_KEY, true).apply();
    }
}
