package com.example.core.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.core.firebase.FirebaseCommon;
import com.example.core.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public static FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public static FirebaseFirestore provideFirebaseFirestore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    @Singleton
    public static StorageReference provideStorageReference() {
        return FirebaseStorage.getInstance().getReference();
    }

    @Provides
    public static SharedPreferences provideIntroductionSP(Application application) {
        return application.getSharedPreferences(Constants.INTRODUCTION_SP, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    public static FirebaseCommon provideFirebaseCommon(FirebaseAuth firebaseAuth, FirebaseFirestore firestore) {
        return new FirebaseCommon(firestore, firebaseAuth);
    }
}
