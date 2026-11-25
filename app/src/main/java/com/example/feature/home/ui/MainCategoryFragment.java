package com.example.feature.home.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentMainCategoryBinding;
import com.example.core.util.RecentSearchHelper;
import com.example.core.util.Resource;
import com.example.core.util.UiUtil;
import com.example.feature.home.viewmodel.MainCategoryViewModel;
import com.example.model.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainCategoryFragment extends Fragment {

    private FragmentMainCategoryBinding binding;
    private final SpecialProductsAdapter specialProductAdapter = new SpecialProductsAdapter();
    private final BestDealsAdapter bestDealsProductAdapter = new BestDealsAdapter();
    private final BestProductsAdapter bestProductAdapter = new BestProductsAdapter();
    private MainCategoryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainCategoryViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    @Override
    public void onResume() {
        super.onResume();
        UiUtil.showBottomNavigationView(this);
        setupRecentSearches();
    }

    private void setupViews() {
        binding.rvSpecialProducts.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvSpecialProducts.setAdapter(specialProductAdapter);
        binding.rvBestDealsProducts.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvBestDealsProducts.setAdapter(bestDealsProductAdapter);
        binding.rvBestProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvBestProducts.setAdapter(bestProductAdapter);
        setupRecentSearches();
    }

    private void setupListeners() {
        binding.cardHome3dGuide.setOnClickListener(v -> {
            binding.nestedScrollMainCategory.smoothScrollTo(0, binding.tvBestProducts.getTop());
            Toast.makeText(requireContext(), "Chọn sản phẩm bất kỳ để trải nghiệm mô hình 3D & AR!", Toast.LENGTH_SHORT).show();
        });

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.resetPaging();
            viewModel.fetchSpecialProducts();
            viewModel.fetchBestDealsProducts();
            viewModel.fetchBestProducts();
            setupRecentSearches();
        });

        specialProductAdapter.setOnClick(this::openProductDetails);
        specialProductAdapter.setOnBtnClick(this::openProductDetails);
        bestDealsProductAdapter.setOnClick(this::openProductDetails);
        bestDealsProductAdapter.setOnBtnClick(this::openProductDetails);
        bestProductAdapter.setOnClick(this::openProductDetails);

        binding.nestedScrollMainCategory.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v.getChildAt(0).getBottom() <= v.getHeight() + scrollY) {
                viewModel.fetchBestProducts();
            }
        });
    }

    private void setupRecentSearches() {
        Context context = getContext();
        if (context == null || binding == null) return;
        List<String> recentList = RecentSearchHelper.getRecentSearches(context);
        if (recentList.isEmpty()) {
            binding.headerRecentSearches.setVisibility(View.GONE);
        } else {
            binding.headerRecentSearches.setVisibility(View.VISIBLE);
            binding.btnClearRecent.setOnClickListener(v -> {
                RecentSearchHelper.clearRecentSearches(context);
                setupRecentSearches();
            });
            RecentSearchHelper.populateChips(context, binding.layoutRecentChips, recentList, query -> {
                if (getActivity() != null) {
                    BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                    if (nav != null) nav.setSelectedItemId(R.id.searchFragment);
                }
            });
        }
    }

    private void openProductDetails(Product product) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("product", product);
        NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_productDetailsFragment, bundle);
    }

    private void observeState() {
        viewModel.getSpecialProductsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Loading) binding.mainCategoryProgressBar.setVisibility(View.VISIBLE);
            else if (resource instanceof Resource.Success) {
                specialProductAdapter.differ.submitList(resource.getData());
                binding.mainCategoryProgressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
            } else if (resource instanceof Resource.Error) binding.mainCategoryProgressBar.setVisibility(View.GONE);
        });

        viewModel.getBestDealsProductsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Success) {
                bestDealsProductAdapter.differ.submitList(resource.getData());
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        viewModel.getBestProductsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Loading) binding.bestProductsProgressBar.setVisibility(View.VISIBLE);
            else if (resource instanceof Resource.Success) {
                bestProductAdapter.differ.submitList(resource.getData());
                binding.bestProductsProgressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
            } else if (resource instanceof Resource.Error) binding.bestProductsProgressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
