package com.example.feature.order.ui;

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
import com.example.aroom.databinding.FragmentOrdersBinding;
import com.example.core.util.Resource;
import com.example.core.util.UiUtil;
import com.example.feature.order.viewmodel.OrdersViewModel;
import com.example.model.Order;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private OrdersViewModel viewModel;
    private final AllOrdersAdapter orderAdapter = new AllOrdersAdapter();

    private List<Order> allOrdersList = new ArrayList<>();
    private int currentTabIndex = 0;
    private String initialStatus = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String status = getArguments().getString("initialStatus", "");
            initialStatus = (status != null) ? status : "";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UiUtil.hideBottomNavigationView(this);
        viewModel = new ViewModelProvider(this).get(OrdersViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        binding.rvAllOrders.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.rvAllOrders.setAdapter(orderAdapter);
        setupTabs();
    }

    private void setupTabs() {
        String[] titles = new String[]{"Tất cả", "Đã đặt", "Đang giao", "Đã nhận", "Đã hủy"};
        binding.tabLayoutOrders.removeAllTabs();
        for (String title : titles) binding.tabLayoutOrders.addTab(binding.tabLayoutOrders.newTab().setText(title));

        String s = initialStatus.toLowerCase();
        currentTabIndex = s.contains("đặt") ? 1 : (s.contains("giao") ? 2 : (s.contains("nhận") ? 3 : (s.contains("hủy") ? 4 : 0)));
        TabLayout.Tab tab = binding.tabLayoutOrders.getTabAt(currentTabIndex);
        if (tab != null) tab.select();

        binding.tabLayoutOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { currentTabIndex = tab != null ? tab.getPosition() : 0; filterOrders(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) { filterOrders(); }
        });
    }

    private void setupListeners() {
        View.OnClickListener back = v -> Navigation.findNavController(v).navigateUp();
        binding.cardBackOrders.setOnClickListener(back);
        binding.imageCloseOrders.setOnClickListener(back);
        binding.btnShopNow.setOnClickListener(back);

        orderAdapter.setOnClick(order -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("order", order);
            Navigation.findNavController(requireView()).navigate(R.id.action_ordersFragment_to_orderDetailsFragment, bundle);
        });

        orderAdapter.setOnCancelClick(this::showCancelDialog);
        orderAdapter.setOnRateClick(this::showReviewDialog);
    }

    private void showReviewDialog(Order order) {
        if (order == null) return;
        AddReviewDialog.show(requireContext(), order, (rating, comment) -> {
            viewModel.submitOrderReview(order, rating, comment, (success, message) -> {
                if (success) {
                    Toast.makeText(requireContext(), "Cảm ơn bạn đã gửi đánh giá cho đơn hàng #" + order.getOrderId(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), message != null ? message : "Gửi đánh giá thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showCancelDialog(Order order) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Hủy đơn hàng #" + order.getOrderId())
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    viewModel.cancelOrder(order.getOrderId(), (success, message) -> {
                        if (success) {
                            Toast.makeText(requireContext(), "Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), message != null ? message : "Hủy đơn thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.dismiss();
                })
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void observeState() {
        viewModel.getOrdersLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Loading) {
                binding.progressbarAllOrders.setVisibility(View.VISIBLE);
                binding.layoutEmptyOrders.setVisibility(View.GONE);
            } else if (resource instanceof Resource.Success) {
                binding.progressbarAllOrders.setVisibility(View.GONE);
                allOrdersList = resource.getData() != null ? resource.getData() : new ArrayList<>();
                binding.tvOrdersCount.setText(allOrdersList.size() + " đơn hàng");
                filterOrders();
            } else if (resource instanceof Resource.Error) {
                binding.progressbarAllOrders.setVisibility(View.GONE);
            }
        });
    }

    private void filterOrders() {
        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrdersList) {
            String s = o.getOrderStatus() != null ? o.getOrderStatus().toLowerCase() : "";
            if (currentTabIndex == 0) filtered.add(o);
            else if (currentTabIndex == 1 && (s.contains("đặt") || s.contains("ordered") || s.contains("chờ"))) filtered.add(o);
            else if (currentTabIndex == 2 && (s.contains("giao") || s.contains("shipped"))) filtered.add(o);
            else if (currentTabIndex == 3 && (s.contains("nhận") || s.contains("delivered") || s.contains("giao hàng"))) filtered.add(o);
            else if (currentTabIndex == 4 && (s.contains("hủy") || s.contains("canceled"))) filtered.add(o);
        }
        orderAdapter.differ.submitList(filtered);
        boolean empty = filtered.isEmpty();
        binding.layoutEmptyOrders.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvAllOrders.setVisibility(empty ? View.GONE : View.VISIBLE);
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
