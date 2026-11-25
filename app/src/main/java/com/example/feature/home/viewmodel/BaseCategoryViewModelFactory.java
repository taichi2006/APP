package com.example.feature.home.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.feature.home.repository.HomeRepository;
import com.example.model.Category;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseCategoryViewModelFactory implements ViewModelProvider.Factory {
    private final HomeRepository homeRepository;
    private final Category category;

    public BaseCategoryViewModelFactory(HomeRepository homeRepository, Category category) {
        this.homeRepository = homeRepository;
        this.category = category;
    }

    public BaseCategoryViewModelFactory(FirebaseFirestore firestore, Category category) {
        this(new HomeRepository(firestore), category);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CategoryViewModel(homeRepository, category);
    }
}
