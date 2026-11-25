package com.example.feature.auth.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentLoginBinding;
import com.example.core.util.Resource;
import com.example.feature.auth.viewmodel.LoginViewModel;
import com.example.feature.main.ShoppingActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null && account.getIdToken() != null) {
                            viewModel.signInWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        Toast.makeText(requireContext(), "Google sign in error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        try {
            String webClientId = getString(R.string.default_web_client_id);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        } catch (Exception ignored) {}
    }

    private void setupListeners() {
        binding.tvDontHaveAccount.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment));

        binding.btnLoginLogin.setOnClickListener(v -> {
            String email = binding.edEmailLogin.getText() != null ? binding.edEmailLogin.getText().toString().trim() : "";
            String password = binding.edPasswordLogin.getText() != null ? binding.edPasswordLogin.getText().toString() : "";
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(email, password);
        });

        binding.googleLogin.setOnClickListener(v -> {
            if (googleSignInClient != null) {
                googleSignInClient.signOut().addOnCompleteListener(task -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
            }
        });
    }

    private void observeState() {
        viewModel.getLoginLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (binding == null) return;
            if (resource instanceof Resource.Loading) {
                binding.btnLoginLogin.startAnimation();
            } else if (resource instanceof Resource.Success) {
                binding.btnLoginLogin.revertAnimation();
                Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireActivity(), ShoppingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (resource instanceof Resource.Error) {
                binding.btnLoginLogin.revertAnimation();
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getResetPasswordLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Success) {
                Snackbar.make(requireView(), "Liên kết đặt lại mật khẩu đã được gửi đến email", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
