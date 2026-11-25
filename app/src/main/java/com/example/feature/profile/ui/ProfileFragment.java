package com.example.feature.profile.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.aroom.BuildConfig;
import com.example.aroom.R;
import com.example.aroom.databinding.FragmentProfileBinding;
import com.example.core.util.Resource;
import com.example.core.util.SoundHelper;
import com.example.core.util.UiUtil;
import com.example.feature.auth.ui.LoginRegisterActivity;
import com.example.feature.profile.viewmodel.ProfileViewModel;
import com.example.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        binding.tvVersion.setText("ARoom • Phiên bản " + BuildConfig.VERSION_NAME);
    }

    private void setupListeners() {
        NavController navController = NavHostFragment.findNavController(this);

        binding.constraintProfile.setOnClickListener(v -> navController.navigate(R.id.action_profileFragment_to_userAccountFragment));
        binding.linearAllOrders.setOnClickListener(v -> navigateToOrders(""));
        binding.btnStatusOrdered.setOnClickListener(v -> { SoundHelper.playClickSound(); navigateToOrders("Đã đặt"); });
        binding.btnStatusShipped.setOnClickListener(v -> { SoundHelper.playClickSound(); navigateToOrders("Đang giao"); });
        binding.btnStatusDelivered.setOnClickListener(v -> { SoundHelper.playClickSound(); navigateToOrders("Đã nhận"); });

        binding.linearVoucherWallet.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Ví Voucher ARoom")
                    .setMessage("Mã: AROOM50 - Giảm 10% tối đa 50k cho đơn từ 200k\nMã: SALE50 - Giảm 50k cho đơn từ 500k")
                    .setPositiveButton("Đóng", (d, w) -> d.dismiss())
                    .show();
        });

        binding.linearAddressBook.setOnClickListener(v -> navController.navigate(R.id.action_profileFragment_to_addressFragment));

        binding.linearSupportHotline.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Tổng đài CSKH ARoom")
                    .setMessage("Hotline hỗ trợ: 1900 6868\nEmail: hotro@aroom.vn\nGiờ làm việc: 8:00 - 21:00 hàng ngày")
                    .setPositiveButton("Đóng", (d, w) -> d.dismiss())
                    .show();
        });

        binding.linearWarrantyPolicy.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Chính sách bảo hành")
                    .setMessage("• Bảo hành 2 năm đối với khung gỗ và kết cấu\n• Đổi mới trong 30 ngày nếu có lỗi từ nhà sản xuất\n• Hỗ trợ vận chuyển lắp đặt tận nhà")
                    .setPositiveButton("Đóng", (d, w) -> d.dismiss())
                    .show();
        });

        binding.linearLogOut.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")
                    .setPositiveButton("Đăng xuất", (d, w) -> {
                        viewModel.signOut();
                        Intent intent = new Intent(requireActivity(), LoginRegisterActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                        Toast.makeText(requireContext(), "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                    .show();
        });
    }

    private void observeState() {
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (binding == null) return;
            if (resource instanceof Resource.Loading) {
                binding.progressbarSettings.setVisibility(View.VISIBLE);
            } else if (resource instanceof Resource.Success) {
                binding.progressbarSettings.setVisibility(View.GONE);
                User user = resource.getData();
                if (user != null) {
                    String name = (user.getFirstName() + " " + user.getLastName()).trim();
                    binding.tvUserName.setText(name.isEmpty() ? "Khách hàng ARoom" : name);
                    Glide.with(requireContext()).load(user.getImagePath())
                            .placeholder(new ColorDrawable(Color.parseColor("#E2E8F0")))
                            .error(new ColorDrawable(Color.parseColor("#E2E8F0")))
                            .into(binding.imageUser);
                }
            } else if (resource instanceof Resource.Error) {
                binding.progressbarSettings.setVisibility(View.GONE);
            }
        });
    }

    private void navigateToOrders(String initialStatus) {
        Bundle bundle = new Bundle();
        bundle.putString("initialStatus", initialStatus);
        NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_ordersFragment, bundle);
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
