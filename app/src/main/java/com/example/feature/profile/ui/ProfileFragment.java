package com.example.feature.profile.ui;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

        binding.linearVoucherWallet.setOnClickListener(v -> showVouchersDialog());

        binding.linearAddressBook.setOnClickListener(v -> navController.navigate(R.id.action_profileFragment_to_addressFragment));

        binding.linearSupportHotline.setOnClickListener(v -> showSupportHotlineDialog());

        binding.linearWarrantyPolicy.setOnClickListener(v -> showWarrantyPolicyDialog());

        binding.linearLogOut.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showVouchersDialog() {
        SoundHelper.playClickSound();
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_vouchers);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View item10 = dialog.findViewById(R.id.item_voucher_10);
        View item20 = dialog.findViewById(R.id.item_voucher_20);
        View item100k = dialog.findViewById(R.id.item_voucher_100k);
        View btnClose = dialog.findViewById(R.id.btn_voucher_close);

        if (item10 != null) {
            item10.setOnClickListener(v -> {
                SoundHelper.playClickSound();
                copyToClipboard("TCFURNISH10");
                Toast.makeText(requireContext(), "Đã sao chép mã TCFURNISH10", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
        if (item20 != null) {
            item20.setOnClickListener(v -> {
                SoundHelper.playClickSound();
                copyToClipboard("VIP20");
                Toast.makeText(requireContext(), "Đã sao chép mã VIP20", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
        if (item100k != null) {
            item100k.setOnClickListener(v -> {
                SoundHelper.playClickSound();
                copyToClipboard("NOITHAT100K");
                Toast.makeText(requireContext(), "Đã sao chép mã NOITHAT100K", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                SoundHelper.playClickSound();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Voucher Code", text));
        }
    }

    private void showSupportHotlineDialog() {
        SoundHelper.playClickSound();
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_support_hotline);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View btnClose = dialog.findViewById(R.id.btn_support_close);
        View btnCall = dialog.findViewById(R.id.btn_support_call);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                SoundHelper.playClickSound();
                dialog.dismiss();
            });
        }
        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                SoundHelper.playClickSound();
                try {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:19008888"));
                    startActivity(dialIntent);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Không thể mở ứng dụng cuộc gọi", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void showWarrantyPolicyDialog() {
        SoundHelper.playClickSound();
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_warranty_policy);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View btnClose = dialog.findViewById(R.id.btn_warranty_close);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                SoundHelper.playClickSound();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void showLogoutConfirmationDialog() {
        SoundHelper.playClickSound();
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_logout_confirmation);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View btnCancel = dialog.findViewById(R.id.btn_logout_cancel);
        View btnConfirm = dialog.findViewById(R.id.btn_logout_confirm);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                SoundHelper.playClickSound();
                dialog.dismiss();
            });
        }
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                SoundHelper.playClickSound();
                dialog.dismiss();
                viewModel.signOut();
                Intent intent = new Intent(requireActivity(), LoginRegisterActivity.class);
                startActivity(intent);
                requireActivity().finish();
                Toast.makeText(requireContext(), "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
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
