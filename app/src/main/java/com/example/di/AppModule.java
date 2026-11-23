package com.example.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.firebase.FirebaseCommon;
import com.example.util.Constants;
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
    public static FirebaseFirestore provideFirebaseFirestoreDb() {
        return FirebaseFirestore.getInstance();
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

    @Provides
    @Singleton
    public static StorageReference provideStorage() {
        return FirebaseStorage.getInstance().getReference();
    }
}
