package com.example.feature.main.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.aroom.R;
import com.example.aroom.databinding.ActivityShoppingBinding;
import com.example.core.util.Resource;
import com.example.feature.cart.viewmodel.CartViewModel;
import com.example.model.CartProduct;
import com.google.android.material.badge.BadgeDrawable;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ShoppingActivity extends AppCompatActivity {

    private ActivityShoppingBinding binding;
    private CartViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShoppingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CartViewModel.class);

        setupNavigation();
        observeCartBadge();
    }

    private void setupNavigation() {
        NavController navController = Navigation.findNavController(this, R.id.shopping_host_fragment);
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }

    private void observeCartBadge() {
        viewModel.getCartProductsLiveData().observe(this, resource -> {
            if (resource instanceof Resource.Success) {
                List<CartProduct> list = resource.getData();
                int count = (list != null) ? list.size() : 0;
                BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.cartFragment);
                badge.setNumber(count);
                badge.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_primary));
                badge.setVisible(count > 0);
            }
        });
    }

    public ActivityShoppingBinding getBinding() {
        return binding;
    }
}
