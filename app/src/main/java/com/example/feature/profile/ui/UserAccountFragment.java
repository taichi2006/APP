package com.example.feature.profile.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.aroom.databinding.FragmentUserAccountBinding;
import com.example.core.util.Resource;
import com.example.feature.profile.viewmodel.UserAccountViewModel;
import com.example.model.User;

import java.io.ByteArrayOutputStream;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserAccountFragment extends Fragment {

    private FragmentUserAccountBinding binding;
    private UserAccountViewModel viewModel;
    private ActivityResultLauncher<Intent> imageActivityResultLauncher;
    private byte[] imageBytes = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                                imageBytes = baos.toByteArray();
                                if (binding != null) {
                                    Glide.with(this).load(bitmap).into(binding.imageUser);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserAccountViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {}

    private void setupListeners() {
        binding.buttonSave.setOnClickListener(v -> {
            String fName = binding.edFirstName.getText() != null ? binding.edFirstName.getText().toString().trim() : "";
            String lName = binding.edLastName.getText() != null ? binding.edLastName.getText().toString().trim() : "";
            String email = binding.edEmail.getText() != null ? binding.edEmail.getText().toString().trim() : "";
            viewModel.updateUser(new User(fName, lName, email), imageBytes);
        });

        binding.imageEdit.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imageActivityResultLauncher.launch(intent);
        });

        binding.imageCloseUserAccount.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void observeState() {
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (binding == null) return;
            if (resource instanceof Resource.Loading) {
                binding.progressbarAccount.setVisibility(View.VISIBLE);
            } else if (resource instanceof Resource.Success) {
                binding.progressbarAccount.setVisibility(View.GONE);
                User user = resource.getData();
                if (user != null) {
                    Glide.with(this).load(user.getImagePath()).error(new ColorDrawable(Color.BLACK)).into(binding.imageUser);
                    binding.edFirstName.setText(user.getFirstName());
                    binding.edLastName.setText(user.getLastName());
                    binding.edEmail.setText(user.getEmail());
                }
            } else if (resource instanceof Resource.Error) {
                binding.progressbarAccount.setVisibility(View.GONE);
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateInfoLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (binding == null) return;
            if (resource instanceof Resource.Loading) {
                binding.buttonSave.startAnimation();
            } else if (resource instanceof Resource.Success) {
                binding.buttonSave.revertAnimation();
                Navigation.findNavController(requireView()).navigateUp();
            } else if (resource instanceof Resource.Error) {
                binding.buttonSave.revertAnimation();
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
