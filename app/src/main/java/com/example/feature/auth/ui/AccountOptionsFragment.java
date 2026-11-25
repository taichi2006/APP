package com.example.feature.auth.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentAccountOptionsBinding;

public class AccountOptionsFragment extends Fragment {

    private FragmentAccountOptionsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountOptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {}

    private void setupListeners() {
        NavController navController = NavHostFragment.findNavController(this);
        binding.btnRegisterAccountOptions.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.accountOptionsFragment) {
                try {
                    navController.navigate(R.id.action_accountOptionsFragment_to_registerFragment);
                } catch (Exception ignored) {}
            }
        });

        binding.btnLoginAccountOptions.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.accountOptionsFragment) {
                try {
                    navController.navigate(R.id.action_accountOptionsFragment_to_loginFragment);
                } catch (Exception ignored) {}
            }
        });
    }

    private void observeState() {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
