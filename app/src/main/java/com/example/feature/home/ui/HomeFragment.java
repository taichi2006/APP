package com.example.feature.home.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentHomeBinding;
import com.example.model.Category;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new MainCategoryFragment());
        fragments.add(CategoryFragment.newInstance(Category.Chair));
        fragments.add(CategoryFragment.newInstance(Category.Cupboard));
        fragments.add(CategoryFragment.newInstance(Category.Table));
        fragments.add(CategoryFragment.newInstance(Category.Sofa));
        fragments.add(CategoryFragment.newInstance(Category.Bed));

        binding.viewPagerHome.setUserInputEnabled(false);
        HomeViewPagerAdapter adapter = new HomeViewPagerAdapter(fragments, getChildFragmentManager(), getViewLifecycleOwner().getLifecycle());
        binding.viewPagerHome.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPagerHome, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Trang chủ"); break;
                case 1: tab.setText("Ghế"); break;
                case 2: tab.setText("Tủ"); break;
                case 3: tab.setText("Bàn"); break;
                case 4: tab.setText("Sofa"); break;
                case 5: tab.setText("Giường"); break;
            }
        }).attach();
    }

    private void setupListeners() {
        binding.searchBar.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                if (nav != null) nav.setSelectedItemId(R.id.searchFragment);
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
