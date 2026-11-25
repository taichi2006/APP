package com.example;

import android.app.Application;
import android.util.Log;

import com.example.core.util.DatabaseSeeder;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ARoomApplication extends Application {
    private static final String TAG = "ARoomApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            DatabaseSeeder.seedAll();
        } catch (Exception e) {
            Log.e(TAG, "DatabaseSeeder error: " + e.getMessage());
        }
    }
}
