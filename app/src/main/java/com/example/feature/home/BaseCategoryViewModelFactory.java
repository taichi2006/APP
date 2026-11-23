package com.example.feature.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.data.Category;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseCategoryViewModelFactory implements ViewModelProvider.Factory {
    private final FirebaseFirestore firestore;
    private final Category category;

    public BaseCategoryViewModelFactory(FirebaseFirestore firestore, Category category) {
        this.firestore = firestore;
        this.category = category;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CategoryViewModel(firestore, category);
    }
}
