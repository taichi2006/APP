package com.example.feature.home.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentBaseCategoryBinding;
import com.example.core.util.Resource;
import com.example.core.util.UiUtil;
import com.example.feature.home.repository.HomeRepository;
import com.example.feature.home.viewmodel.BaseCategoryViewModelFactory;
import com.example.feature.home.viewmodel.CategoryViewModel;
import com.example.model.Category;
import com.example.model.Product;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryFragment extends Fragment {

    private static final String ARG_CATEGORY_NAME = "arg_category_name";

    private FragmentBaseCategoryBinding binding;
    private BestProductsAdapter offerAdapter;
    private BestProductsAdapter bestProductAdapter;
    private CategoryViewModel viewModel;

    @Inject
    HomeRepository homeRepository;

    public static CategoryFragment newInstance(Category category) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_NAME, category != null ? category.getCategory() : "");
        fragment.setArguments(args);
        return fragment;
    }

    public static CategoryFragment newInstance(String categoryName) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBaseCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String categoryName = getArguments() != null ? getArguments().getString(ARG_CATEGORY_NAME, "") : "";
        Category category = new Category(categoryName) {};
        BaseCategoryViewModelFactory factory = new BaseCategoryViewModelFactory(homeRepository, category);
        viewModel = new ViewModelProvider(this, factory).get(CategoryViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        offerAdapter = new BestProductsAdapter();
        bestProductAdapter = new BestProductsAdapter();

        binding.rvOffer.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvOffer.setAdapter(offerAdapter);

        binding.rvBestProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false));
        binding.rvBestProducts.setAdapter(bestProductAdapter);
    }

    private void setupListeners() {
        offerAdapter.setOnClick(this::openProductDetails);
        bestProductAdapter.setOnClick(this::openProductDetails);
    }

    private void openProductDetails(Product product) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("product", product);
        NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_productDetailsFragment, bundle);
    }

    private void observeState() {
        viewModel.getOfferProductsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Loading) {
                binding.offerProductProgressBar.setVisibility(View.VISIBLE);
            } else if (resource instanceof Resource.Success) {
                binding.offerProductProgressBar.setVisibility(View.GONE);
                offerAdapter.differ.submitList(resource.getData());
            } else if (resource instanceof Resource.Error) {
                binding.offerProductProgressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getBestProductsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Loading) {
                binding.bestProductProgressBar.setVisibility(View.VISIBLE);
            } else if (resource instanceof Resource.Success) {
                binding.bestProductProgressBar.setVisibility(View.GONE);
                bestProductAdapter.differ.submitList(resource.getData());
            } else if (resource instanceof Resource.Error) {
                binding.bestProductProgressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        UiUtil.showBottomNavigationView(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
