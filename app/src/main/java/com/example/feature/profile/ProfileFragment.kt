package com.example.feature.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.aroom.BuildConfig
import com.example.aroom.R
import com.example.aroom.databinding.FragmentProfileBinding
import com.example.feature.auth.LoginRegisterActivity
import com.example.util.Resource
import com.example.util.SoundHelper
import com.example.util.showBottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            constraintProfile.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_userAccountFragment)
            }

            // Tất cả đơn hàng
            linearAllOrders.setOnClickListener {
                navigateToOrders("")
            }

            // 4 Nút trạng thái đơn hàng cân đối
            btnStatusOrdered.setOnClickListener {
                SoundHelper.playClickSound()
                navigateToOrders("Đã đặt")
            }

            btnStatusConfirmed.setOnClickListener {
                SoundHelper.playClickSound()
                navigateToOrders("Đã xác nhận")
            }

            btnStatusShipped.setOnClickListener {
                SoundHelper.playClickSound()
                navigateToOrders("Đang giao")
            }

            btnStatusDelivered.setOnClickListener {
                SoundHelper.playClickSound()
                navigateToOrders("Đã giao")
            }

            // Tiện ích
            linearVoucherWallet.setOnClickListener {
                showVouchersDialog()
            }

            linearAddressBook.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_addressFragment)
            }

            linearSupportHotline.setOnClickListener {
                showSupportHotlineDialog()
            }

            linearWarrantyPolicy.setOnClickListener {
                showWarrantyPolicyDialog()
            }

            // Đăng xuất
            linearLogOut.setOnClickListener {
                showLogoutConfirmationDialog()
            }

            tvVersion.text = "TCFurnish • Phiên bản ${BuildConfig.VERSION_NAME}"
        }

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showProgressBar()
                    }
                    is Resource.Success -> {
                        hideProgressBar()
                        val user = it.data
                        if (user != null) {
                            binding.tvUserName.text = "${user.firstName} ${user.lastName}".trim().ifBlank { "Khách hàng TCFurnish" }
                            Glide.with(requireContext())
                                .load(user.imagePath)
                                .placeholder(ColorDrawable(Color.parseColor("#E2E8F0")))
                                .error(ColorDrawable(Color.parseColor("#E2E8F0")))
                                .into(binding.imageUser)
                        }
                    }
                    is Resource.Error -> {
                        hideProgressBar()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun showVouchersDialog() {
        SoundHelper.playClickSound()
        val dialog = android.app.Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_vouchers, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val bindVoucherCopy = { viewId: Int, code: String ->
            view.findViewById<View>(viewId).setOnClickListener {
                copyToClipboard(code)
                Toast.makeText(requireContext(), "Đã sao chép mã $code", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        bindVoucherCopy(R.id.item_voucher_10, "TCFURNISH10")
        bindVoucherCopy(R.id.item_voucher_20, "VIP20")
        bindVoucherCopy(R.id.item_voucher_100k, "NOITHAT100K")

        view.findViewById<View>(R.id.btn_voucher_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showSupportHotlineDialog() {
        SoundHelper.playClickSound()
        val dialog = android.app.Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_support_hotline, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.findViewById<View>(R.id.btn_support_close).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btn_support_call).setOnClickListener {
            dialog.dismiss()
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:19008888")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Không thể mở ứng dụng cuộc gọi", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showWarrantyPolicyDialog() {
        SoundHelper.playClickSound()
        val dialog = android.app.Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_warranty_policy, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.findViewById<View>(R.id.btn_warranty_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showLogoutConfirmationDialog() {
        SoundHelper.playClickSound()
        val dialog = android.app.Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_logout_confirmation, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.findViewById<View>(R.id.btn_logout_cancel).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btn_logout_confirm).setOnClickListener {
            dialog.dismiss()
            viewModel.signOut()
            val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            Toast.makeText(requireContext(), "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Voucher Code", text)
        clipboard?.setPrimaryClip(clip)
    }

    private fun navigateToOrders(initialStatus: String) {
        val action = ProfileFragmentDirections.actionProfileFragmentToOrdersFragment(initialStatus)
        findNavController().navigate(action)
    }

    private fun hideProgressBar() {
        binding.progressbarSettings.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.progressbarSettings.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }
}
