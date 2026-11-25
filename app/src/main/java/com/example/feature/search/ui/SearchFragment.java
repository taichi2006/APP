package com.example.feature.search.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentSearchBinding;
import com.example.core.util.RecentSearchHelper;
import com.example.core.util.Resource;
import com.example.core.util.SoundHelper;
import com.example.core.util.UiUtil;
import com.example.feature.search.viewmodel.SearchViewModel;
import com.example.model.Product;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private SearchAdapter searchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        searchAdapter = new SearchAdapter();
        binding.searchRv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.searchRv.setAdapter(searchAdapter);
        setupSortChips();
    }

    private void setupListeners() {
        NavController navController = NavHostFragment.findNavController(this);
        searchAdapter.setOnClick(product -> {
            RecentSearchHelper.saveSearchQuery(requireContext(), product.getName());
            Bundle b = new Bundle();
            b.putParcelable("product", product);
            navController.navigate(R.id.action_searchFragment_to_productDetailsFragment, b);
        });

        binding.searchIcon.setOnClickListener(v -> submitQuery());
        binding.edSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                submitQuery();
                return true;
            }
            return false;
        });

        binding.edSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchProduct(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void submitQuery() {
        String query = binding.edSearch.getText() != null ? binding.edSearch.getText().toString().trim() : "";
        if (!query.isEmpty()) RecentSearchHelper.saveSearchQuery(requireContext(), query);
        viewModel.searchProduct(query);
    }

    private void setupSortChips() {
        TextView[] chips = new TextView[]{
                binding.chipSortSold, binding.chipSortPriceAsc, binding.chipSortPriceDesc,
                binding.chipSortRating, binding.chipSortDiscount
        };
        SearchViewModel.SortType[] types = new SearchViewModel.SortType[]{
                SearchViewModel.SortType.SOLD_DESC, SearchViewModel.SortType.PRICE_ASC,
                SearchViewModel.SortType.PRICE_DESC, SearchViewModel.SortType.RATING_DESC,
                SearchViewModel.SortType.OFFER_DESC
        };

        for (int i = 0; i < chips.length; i++) {
            final int index = i;
            chips[i].setOnClickListener(v -> {
                SoundHelper.playClickSound();
                updateChipStyles(chips[index], chips);
                viewModel.setSortType(types[index]);
            });
        }
    }

    private void updateChipStyles(TextView selectedChip, TextView[] allChips) {
        int selectedBg = ContextCompat.getColor(requireContext(), R.color.brand_primary);
        int unselectedBg = Color.parseColor("#FAF3E8");
        int textDark = ContextCompat.getColor(requireContext(), R.color.brand_text_dark);
        Typeface font = ResourcesCompat.getFont(requireContext(), R.font.sf_pro_display);

        for (TextView chip : allChips) {
            if (chip == selectedChip) {
                chip.setBackgroundTintList(ColorStateList.valueOf(selectedBg));
                chip.setTextColor(Color.WHITE);
                chip.setTypeface(font, Typeface.BOLD);
            } else {
                chip.setBackgroundTintList(ColorStateList.valueOf(unselectedBg));
                chip.setTextColor(textDark);
                chip.setTypeface(font, Typeface.NORMAL);
            }
        }
    }

    private void observeState() {
        viewModel.getSearchLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (binding == null) return;
            if (resource instanceof Resource.Loading) {
                binding.progressBarSearch.setVisibility(View.VISIBLE);
                binding.layoutEmptySearch.setVisibility(View.GONE);
            } else if (resource instanceof Resource.Success) {
                binding.progressBarSearch.setVisibility(View.INVISIBLE);
                List<Product> products = resource.getData() != null ? resource.getData() : new ArrayList<>();
                searchAdapter.differ.submitList(products);
                boolean empty = products.isEmpty();
                binding.layoutEmptySearch.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.searchRv.setVisibility(empty ? View.GONE : View.VISIBLE);
            } else if (resource instanceof Resource.Error) {
                binding.progressBarSearch.setVisibility(View.INVISIBLE);
                binding.layoutEmptySearch.setVisibility(View.VISIBLE);
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
