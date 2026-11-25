package com.example.feature.product.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aroom.databinding.FragmentVrBinding;
import com.example.model.Product;

public class ARFragment extends Fragment {

    private FragmentVrBinding binding;
    private ARSceneDelegate sceneDelegate;
    private Product product;
    private int currentColor = 0;

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted && sceneDelegate != null) sceneDelegate.enableCameraMode(true);
                else Toast.makeText(requireContext(), "Cần cấp quyền Camera", Toast.LENGTH_SHORT).show();
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = getArguments().getParcelable("product");
            currentColor = getArguments().getInt("selectedColor", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        if (product != null) binding.tvVrTitle.setText(product.getName());
        sceneDelegate = new ARSceneDelegate(requireContext(), binding.sceneView3D, binding.cameraSurfaceView, binding.btnSwitchMode, binding.tvInstruction);

        if (product != null && product.getColors() != null && !product.getColors().isEmpty()) {
            binding.rvVrColors.setVisibility(View.VISIBLE);
            ColorsAdapter adapter = new ColorsAdapter();
            binding.rvVrColors.setAdapter(adapter);
            binding.rvVrColors.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            adapter.differ.submitList(product.getColors());
            adapter.setOnItemClickListener(c -> {
                currentColor = c;
                if (sceneDelegate != null) sceneDelegate.applyColorToModel(c);
            });
        }

        String modelUri = (product != null) ? product.getModel() : null;
        sceneDelegate.loadModel(modelUri, currentColor);
    }

    private void setupListeners() {
        binding.btnBackVr.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnResetView.setOnClickListener(v -> {
            if (sceneDelegate != null) sceneDelegate.resetView();
        });

        binding.btnSwitchMode.setOnClickListener(v -> {
            if (sceneDelegate == null) return;
            if (!sceneDelegate.isCameraMode()) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    sceneDelegate.enableCameraMode(true);
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            } else {
                sceneDelegate.enableCameraMode(false);
            }
        });
    }

    private void observeState() {}

    @Override
    public void onResume() {
        super.onResume();
        if (sceneDelegate != null) sceneDelegate.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sceneDelegate != null) sceneDelegate.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sceneDelegate != null) sceneDelegate.onDestroy();
        sceneDelegate = null;
        binding = null;
    }
}
