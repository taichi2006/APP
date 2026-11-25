package com.example.feature.auth.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aroom.R;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginRegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
    }
}
