package com.example.feature.cart.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentCartBinding;
import com.example.core.firebase.FirebaseCommon;
import com.example.core.util.PriceCalculator;
import com.example.core.util.Resource;
import com.example.core.util.SoundHelper;
import com.example.core.util.UiUtil;
import com.example.feature.cart.viewmodel.CartViewModel;
import com.example.model.CartProduct;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartProductAdapter cartAdapter;
    private CartViewModel viewModel;
    private float totalPrice = 0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        cartAdapter = new CartProductAdapter();
        binding.rvCart.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.rvCart.setAdapter(cartAdapter);
        binding.rvCart.addItemDecoration(new UiUtil.VerticalItemDecoration());
    }

    private void setupListeners() {
        NavController navController = NavHostFragment.findNavController(this);

        cartAdapter.setOnProductClick(cartProduct -> {
            SoundHelper.playClickSound();
            float effPrice = (cartProduct.getProduct().getOfferPercentage() != null && cartProduct.getProduct().getOfferPercentage() > 0)
                    ? cartProduct.getProduct().getPrice() * (1f - cartProduct.getProduct().getOfferPercentage())
                    : cartProduct.getProduct().getPrice();
            Bundle bundle = new Bundle();
            bundle.putFloat("totalPrice", effPrice * cartProduct.getQuantity());
            bundle.putParcelableArray("products", new CartProduct[]{ cartProduct });
            bundle.putBoolean("payment", true);
            navController.navigate(R.id.action_cartFragment_to_billingFragment, bundle);
        });

        cartAdapter.setOnPlusClick(cp -> viewModel.changeQuantity(cp, FirebaseCommon.QuantityChanging.INCREASE));
        cartAdapter.setOnMinusClick(cp -> viewModel.changeQuantity(cp, FirebaseCommon.QuantityChanging.DECREASE));
        binding.imgCloseCart.setOnClickListener(v -> navController.navigateUp());

        binding.btnCheckoutCart.setOnClickListener(v -> {
            SoundHelper.playClickSound(v);
            List<CartProduct> currentList = cartAdapter.differ.getCurrentList();
            Bundle bundle = new Bundle();
            bundle.putFloat("totalPrice", totalPrice);
            bundle.putParcelableArray("products", currentList.toArray(new CartProduct[0]));
            bundle.putBoolean("payment", true);
            navController.navigate(R.id.action_cartFragment_to_billingFragment, bundle);
        });
    }

    private void observeState() {
        viewModel.getProductPriceLiveData().observe(getViewLifecycleOwner(), price -> {
            if (binding == null || price == null) return;
            totalPrice = price;
            binding.tvTotalPrice.setText(PriceCalculator.formatPriceVnd(price));
        });

        viewModel.getCartProductsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (binding == null) return;
            if (resource instanceof Resource.Loading) {
                binding.progressBarCart.setVisibility(View.VISIBLE);
            } else if (resource instanceof Resource.Success) {
                binding.progressBarCart.setVisibility(View.INVISIBLE);
                List<CartProduct> list = resource.getData() != null ? resource.getData() : new ArrayList<>();
                boolean empty = list.isEmpty();
                binding.layoutEmptyCart.setVisibility(empty ? View.VISIBLE : View.GONE);
                int vis = empty ? View.GONE : View.VISIBLE;
                binding.rvCart.setVisibility(vis);
                binding.toolbarCart.setVisibility(vis);
                binding.btnCheckoutCart.setVisibility(vis);
                binding.totalBoxContainer.setVisibility(vis);
                if (!empty) cartAdapter.differ.submitList(list);
            } else if (resource instanceof Resource.Error) {
                binding.progressBarCart.setVisibility(View.INVISIBLE);
            }
        });

        viewModel.getDeleteDialogLiveData().observe(getViewLifecycleOwner(), cp -> {
            if (cp != null) showDeleteDialog(cp);
        });
    }

    private void showDeleteDialog(CartProduct cartProduct) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?")
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteCartProduct(cartProduct);
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
