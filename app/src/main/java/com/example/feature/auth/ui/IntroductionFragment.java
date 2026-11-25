package com.example.feature.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentIntroductionBinding;
import com.example.feature.auth.viewmodel.IntroductionViewModel;
import com.example.feature.main.ui.ShoppingActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class IntroductionFragment extends Fragment {

    private FragmentIntroductionBinding binding;
    private IntroductionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIntroductionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(IntroductionViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {}

    private void setupListeners() {
        NavController navController = NavHostFragment.findNavController(this);
        binding.btnStartIntroduction.setOnClickListener(v -> {
            viewModel.startBtnClick();
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.introductionFragment) {
                try {
                    navController.navigate(R.id.action_introductionFragment_to_accountOptionsFragment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void observeState() {
        NavController navController = NavHostFragment.findNavController(this);
        int target = viewModel.getNavigate().getValue();
        if (target == IntroductionViewModel.SHOPPING_ACTIVITY) {
            Intent intent = new Intent(requireActivity(), ShoppingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (target == IntroductionViewModel.ACCOUNT_OPTIONS_FRAGMENT) {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.introductionFragment) {
                try {
                    navController.navigate(target);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
