package com.example.feature.profile

import android.app.AlertDialog
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
        val vouchers = arrayOf(
            "TCFURNISH10: Giảm 10% đơn hàng đầu tiên",
            "VIP20: Giảm 20% đặc quyền thành viên TCFurnish",
            "NOITHAT100K: Giảm trực tiếp 100.000đ"
        )
        AlertDialog.Builder(requireContext())
            .setTitle("Kho Voucher Ưu Đãi")
            .setItems(vouchers) { _, which ->
                val code = when (which) {
                    0 -> "TCFURNISH10"
                    1 -> "VIP20"
                    else -> "NOITHAT100K"
                }
                copyToClipboard(code)
                Toast.makeText(requireContext(), "Đã sao chép mã $code", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("Đóng", null)
            .show()
    }

    private fun showSupportHotlineDialog() {
        SoundHelper.playClickSound()
        AlertDialog.Builder(requireContext())
            .setTitle("Hệ Thống Showroom & CSKH TCFurnish")
            .setMessage(
                "TCFurnish - Tuyệt tác không gian sống:\n\n" +
                "• Flagship Showroom: 54 Liễu Giai, Ba Đình, Hà Nội\n" +
                "• Showroom TP.HCM: 188 Nguyễn Văn Trỗi, Phú Nhuận, TP.HCM\n" +
                "• Hotline Chăm sóc Khách hàng: 1900 8888 (08:30 - 21:30 hàng ngày)\n" +
                "• Email hỗ trợ: cskh@tcfurnish.vn"
            )
            .setPositiveButton("Gọi ngay") { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:19008888")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Không thể mở ứng dụng cuộc gọi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun showWarrantyPolicyDialog() {
        SoundHelper.playClickSound()
        AlertDialog.Builder(requireContext())
            .setTitle("Chính Sách Bảo Hành & Đổi Trả TCFurnish")
            .setMessage(
                "Tiêu chuẩn dịch vụ hậu mãi TCFurnish:\n\n" +
                "1. Bảo hành kết cấu gỗ & khung sườn: 05 năm chính hãng.\n" +
                "2. Bảo hành đệm mút & bề mặt da/nỉ: 02 năm theo tiêu chuẩn.\n" +
                "3. Đổi mới trong 30 ngày đối với lỗi kỹ thuật từ nhà xưởng.\n" +
                "4. Miễn phí vận chuyển và lắp đặt hoàn thiện tại nhà.\n" +
                "5. Bảo dưỡng và đánh bóng bề mặt gỗ định kỳ trọn đời."
            )
            .setPositiveButton("Đóng", null)
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        SoundHelper.playClickSound()
        AlertDialog.Builder(requireContext())
            .setTitle("Đăng xuất tài khoản")
            .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Đăng xuất") { _, _ ->
                viewModel.signOut()
                val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
                Toast.makeText(requireContext(), "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
            }
            .show()
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
