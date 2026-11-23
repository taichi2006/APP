package com.example.feature.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.feature.main.ShoppingActivity
import com.example.aroom.R
import com.example.aroom.databinding.FragmentRegisterBinding
import com.example.data.User
import com.example.util.RegisterValidation
import com.example.util.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "RegisterFragment"

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()
    private var googleSignInClient: GoogleSignInClient? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrEmpty()) {
                    viewModel.signInWithGoogle(idToken)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Không lấy được Google ID Token. Vui lòng kiểm tra Web Client ID trong strings.xml",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(
                    requireContext(),
                    "Đăng ký bằng Google thất bại (Mã lỗi ${e.statusCode}): ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleSignIn()

        binding.apply {

            tvDoYouHaveAccount.setOnClickListener {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }

            btnRegisterRegister.setOnClickListener {
                val user = User(
                    edFirstNameRegister.text.toString().trim(),
                    edLastNameRegister.text.toString().trim(),
                    edEmailRegister.text.toString().trim(),
                )
                val password = edPasswordRegister.text.toString()
                viewModel.createAccountWithEmailAndPassword(user, password)
            }

            googleRegister.setOnClickListener {
                launchGoogleSignIn()
            }

            facebookRegister.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "Đăng nhập Facebook đang được cập nhật. Bạn vui lòng sử dụng Google hoặc Email!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.register.collect {
                when (it) {
                    is Resource.Loading -> {
                        binding.btnRegisterRegister.startAnimation()
                    }
                    is Resource.Success -> {
                        Log.d("test", it.data.toString())
                        binding.btnRegisterRegister.revertAnimation()
                        Toast.makeText(requireContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }

                    is Resource.Error -> {
                        Log.e(TAG, it.message.toString())
                        binding.btnRegisterRegister.revertAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.validation.collect { validation ->
                val emailVal = validation.email
                if (emailVal is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.edEmailRegister.apply {
                            requestFocus()
                            error = emailVal.message
                        }
                    }
                }

                val passwordVal = validation.password
                if (passwordVal is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.edPasswordRegister.apply {
                            requestFocus()
                            error = passwordVal.message
                        }
                    }
                }
            }
        }
    }

    private fun setupGoogleSignIn() {
        try {
            val configuredClientId = getString(R.string.default_web_client_id)
            val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()

            if (configuredClientId.isNotBlank()) {
                gsoBuilder.requestIdToken(configuredClientId)
            }

            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gsoBuilder.build())
        } catch (e: Exception) {
            Log.e(TAG, "setupGoogleSignIn error: ${e.message}")
        }
    }

    private fun launchGoogleSignIn() {
        val googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
        if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
            Toast.makeText(
                requireContext(),
                "Máy ảo hoặc thiết bị này thiếu Google Play Services. Để đăng nhập Google, bạn cần dùng máy ảo có Google Play Store hoặc máy thật!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        googleSignInClient?.let { client ->
            client.signOut().addOnCompleteListener {
                val signInIntent = client.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        } ?: run {
            Toast.makeText(requireContext(), "Không thể khởi tạo Google Sign In", Toast.LENGTH_SHORT).show()
        }
    }
}
