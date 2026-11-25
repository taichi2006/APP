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
import com.example.aroom.databinding.FragmentRegisterBinding;
import com.example.core.util.RegisterFieldsState;
import com.example.core.util.RegisterValidation;
import com.example.core.util.Resource;
import com.example.feature.auth.viewmodel.RegisterViewModel;
import com.example.feature.main.ShoppingActivity;
import com.example.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private RegisterViewModel viewModel;
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
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

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
        binding.tvDoYouHaveAccount.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment));

        binding.btnRegisterRegister.setOnClickListener(v -> {
            String firstName = binding.edFirstNameRegister.getText() != null ? binding.edFirstNameRegister.getText().toString().trim() : "";
            String lastName = binding.edLastNameRegister.getText() != null ? binding.edLastNameRegister.getText().toString().trim() : "";
            String email = binding.edEmailRegister.getText() != null ? binding.edEmailRegister.getText().toString().trim() : "";
            String password = binding.edPasswordRegister.getText() != null ? binding.edPasswordRegister.getText().toString() : "";
            viewModel.createAccountWithEmailAndPassword(new User(firstName, lastName, email, ""), password);
        });

        binding.googleRegister.setOnClickListener(v -> {
            if (googleSignInClient != null) {
                googleSignInClient.signOut().addOnCompleteListener(task -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
            }
        });
        binding.facebookRegister.setOnClickListener(v -> Toast.makeText(requireContext(), "Vui lòng sử dụng Google hoặc Email!", Toast.LENGTH_SHORT).show());
    }

    private void observeState() {
        viewModel.getRegisterLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (binding == null) return;
            if (resource instanceof Resource.Loading) {
                binding.btnRegisterRegister.startAnimation();
            } else if (resource instanceof Resource.Success) {
                binding.btnRegisterRegister.revertAnimation();
                Toast.makeText(requireContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireActivity(), ShoppingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (resource instanceof Resource.Error) {
                binding.btnRegisterRegister.revertAnimation();
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getValidationLiveData().observe(getViewLifecycleOwner(), fields -> {
            if (fields.getEmail() instanceof RegisterValidation.Failed) {
                binding.edEmailRegister.requestFocus();
                binding.edEmailRegister.setError(((RegisterValidation.Failed) fields.getEmail()).getMessage());
            }
            if (fields.getPassword() instanceof RegisterValidation.Failed) {
                binding.edPasswordRegister.requestFocus();
                binding.edPasswordRegister.setError(((RegisterValidation.Failed) fields.getPassword()).getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
