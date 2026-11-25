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

import com.example.aroom.databinding.FragmentAddressBinding;
import com.example.core.util.Resource;
import com.example.feature.order.viewmodel.AddressViewModel;
import com.example.model.Address;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddressFragment extends Fragment {

    private FragmentAddressBinding binding;
    private AddressViewModel viewModel;
    private Address address;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            address = getArguments().getParcelable("address");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        if (address == null) {
            binding.btnDelete.setVisibility(View.GONE);
            binding.tvPaymentMethods.setText("Thêm địa chỉ giao hàng");
            binding.btnSave.setText("Lưu địa chỉ");
        } else {
            binding.tvPaymentMethods.setText("Chỉnh sửa địa chỉ");
            binding.btnSave.setText("Cập nhật địa chỉ");
            binding.edAddressTitle.setText(address.getAddressTitle());
            binding.edFullName.setText(address.getFullName());
            binding.edState.setText(address.getState());
            binding.edPhone.setText(address.getPhoneNumb());
            binding.edCity.setText(address.getCity());
            binding.edStreet.setText(address.getStreet());
            binding.btnDelete.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> {
            String addressFullName = binding.edFullName.getText() != null ? binding.edFullName.getText().toString().trim() : "";
            String state = binding.edState.getText() != null ? binding.edState.getText().toString().trim() : "";
            String phone = binding.edPhone.getText() != null ? binding.edPhone.getText().toString().trim() : "";
            String city = binding.edCity.getText() != null ? binding.edCity.getText().toString().trim() : "";
            String street = binding.edStreet.getText() != null ? binding.edStreet.getText().toString().trim() : "";
            String title = binding.edAddressTitle.getText() != null ? binding.edAddressTitle.getText().toString().trim() : "";

            Address newAddress = new Address(title, addressFullName, street, phone, city, state);
            if (address != null && address.getId() != null) {
                newAddress.setId(address.getId());
            }
            viewModel.addAddress(newAddress);
        });

        binding.btnDelete.setOnClickListener(v -> {
            if (address != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xóa địa chỉ")
                        .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này?")
                        .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteAddress(address))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        binding.imgAddressClose.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void observeState() {
        viewModel.getAddNewAddressLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Loading) {
                if (binding != null) binding.progressAddress.setVisibility(View.VISIBLE);
            } else if (resource instanceof Resource.Success) {
                if (binding != null) binding.progressAddress.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Lưu địa chỉ thành công!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            } else if (resource instanceof Resource.Error) {
                if (binding != null) binding.progressAddress.setVisibility(View.GONE);
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDeleteAddressLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Loading) {
                if (binding != null) binding.progressAddress.setVisibility(View.VISIBLE);
            } else if (resource instanceof Resource.Success) {
                if (binding != null) binding.progressAddress.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Đã xóa địa chỉ!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            } else if (resource instanceof Resource.Error) {
                if (binding != null) binding.progressAddress.setVisibility(View.GONE);
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
