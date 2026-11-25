package com.example.feature.order.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentOrderDetailBinding;
import com.example.core.util.PriceCalculator;
import com.example.core.util.SoundHelper;
import com.example.core.util.UiUtil;
import com.example.feature.order.viewmodel.OrdersViewModel;
import com.example.feature.product.viewmodel.DetailsViewModel;
import com.example.model.CartProduct;
import com.example.model.Order;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OrderDetailsFragment extends Fragment {

    private FragmentOrderDetailBinding binding;
    private final OrderDetailProductAdapter orderDetailsAdapter = new OrderDetailProductAdapter();
    private DetailsViewModel detailsViewModel;
    private OrdersViewModel ordersViewModel;
    private Order order;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) order = getArguments().getParcelable("order");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UiUtil.hideBottomNavigationView(this);
        detailsViewModel = new ViewModelProvider(this).get(DetailsViewModel.class);
        ordersViewModel = new ViewModelProvider(this).get(OrdersViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.rvProducts.setAdapter(orderDetailsAdapter);
        if (order == null) return;

        binding.tvOrderId.setText("Order #" + order.getOrderId());
        binding.tvInvoiceOrderId.setText("#" + order.getOrderId());
        binding.tvTrackingCode.setText("ARVN" + order.getOrderId());
        binding.tvInvoiceDate.setText(order.getDate() != null ? order.getDate() : "Không xác định");
        binding.tvInvoicePaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
        binding.tvInvoicePaymentStatus.setText(order.getPaymentStatus() != null ? order.getPaymentStatus() : "Chờ xử lý");
        binding.tvSubtotalPrice.setText(PriceCalculator.formatPriceVnd(order.getTotalPrice()));
        binding.tvTotalPrice.setText(PriceCalculator.formatPriceVnd(order.getTotalPrice()));

        if (order.getAddress() != null) {
            binding.tvFullName.setText(order.getAddress().getFullName() != null ? order.getAddress().getFullName() : "Khách hàng");
            binding.tvPhoneNumber.setText(order.getAddress().getPhoneNumb() != null ? order.getAddress().getPhoneNumb() : "Chưa có SĐT");
            binding.tvAddress.setText(order.getAddress().getStreet() + ", " + order.getAddress().getCity());
        }

        bindStatus();
        if (order.getProducts() != null) orderDetailsAdapter.differ.submitList(order.getProducts());
    }

    private void setupListeners() {
        View.OnClickListener back = v -> Navigation.findNavController(v).navigateUp();
        binding.cardBackOrder.setOnClickListener(back);
        binding.imageCloseOrder.setOnClickListener(back);

        binding.btnCopyOrderId.setOnClickListener(v -> {
            if (order == null) return;
            ClipboardManager cb = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) cb.setPrimaryClip(ClipData.newPlainText("Order ID", "#" + order.getOrderId()));
            Toast.makeText(requireContext(), "Đã sao chép mã đơn hàng", Toast.LENGTH_SHORT).show();
        });

        binding.btnReorder.setOnClickListener(v -> {
            if (order == null) return;
            SoundHelper.playSuccessSound();
            if (order.getProducts() != null) {
                for (CartProduct cp : order.getProducts()) detailsViewModel.addUpdateProductInCart(cp);
            }
            Navigation.findNavController(requireView()).navigate(R.id.cartFragment);
        });

        binding.btnCancelOrder.setOnClickListener(v -> {
            if (order == null) return;
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Hủy đơn hàng #" + order.getOrderId())
                    .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                    .setPositiveButton("Hủy đơn", (dialog, which) -> {
                        ordersViewModel.cancelOrder(order.getOrderId(), (success, msg) -> {
                            if (success) {
                                order.setOrderStatus("Đã hủy");
                                bindStatus();
                                Toast.makeText(requireContext(), "Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), msg != null ? msg : "Hủy đơn thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.dismiss();
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        binding.btnRateOrder.setOnClickListener(v -> showReviewDialog());
    }

    private void showReviewDialog() {
        if (order == null) return;
        AddReviewDialog.show(requireContext(), order, (rating, comment) -> {
            ordersViewModel.submitOrderReview(order, rating, comment, (success, message) -> {
                if (success) {
                    order.setReviewed(true);
                    bindStatus();
                    Toast.makeText(requireContext(), "Cảm ơn bạn đã gửi đánh giá cho đơn hàng #" + order.getOrderId(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), message != null ? message : "Gửi đánh giá thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void observeState() {}

    private void bindStatus() {
        if (order == null) return;
        try { binding.stepView.setSteps(new ArrayList<>(Arrays.asList("Đã đặt", "Đang giao", "Đã nhận"))); } catch (Exception ignored) {}

        String st = order.getOrderStatus() != null ? order.getOrderStatus() : "Đã đặt";
        int textColor = Color.parseColor("#2563EB");
        int bgColor = Color.parseColor("#DBEAFE");
        int step = 0;
        boolean isCancelable = false;
        boolean isReceived = false;

        if (st.contains("hủy") || st.equalsIgnoreCase("Canceled")) {
            textColor = Color.parseColor("#DC2626");
            bgColor = Color.parseColor("#FEE2E2");
        } else if (st.contains("nhận") || st.contains("Delivered") || st.contains("giao hàng")) {
            textColor = Color.parseColor("#16A34A");
            bgColor = Color.parseColor("#DCFCE7");
            step = 2;
            isReceived = true;
        } else if (st.contains("giao") || st.contains("Shipped")) {
            textColor = Color.parseColor("#D97706");
            bgColor = Color.parseColor("#FEF3C7");
            step = 1;
        } else {
            isCancelable = true;
        }

        binding.tvDetailStatusText.setText(st);
        binding.tvDetailStatusText.setTextColor(textColor);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(24f);
        bg.setColor(bgColor);
        binding.layoutDetailStatusBadge.setBackground(bg);

        try {
            binding.stepView.go(step, false);
            if (step == 2) binding.stepView.done(true);
        } catch (Exception ignored) {}

        binding.btnCancelOrder.setVisibility(isCancelable ? View.VISIBLE : View.GONE);
        binding.btnRateOrder.setVisibility(isReceived ? View.VISIBLE : View.GONE);
        if (isReceived) {
            if (order.isReviewed()) {
                binding.btnRateOrder.setText("Đã đánh giá");
                binding.btnRateOrder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#64748B")));
            } else {
                binding.btnRateOrder.setText("Đánh giá");
                binding.btnRateOrder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#B88A44")));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UiUtil.hideBottomNavigationView(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        UiUtil.showBottomNavigationView(this);
        binding = null;
    }
}
