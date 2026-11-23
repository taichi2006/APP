package com.example.feature.order

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aroom.R
import com.example.aroom.databinding.FragmentOrderDetailBinding
import com.example.data.order.Order
import com.example.feature.product.DetailsViewModel
import com.example.helper.PriceCalculator
import com.example.util.SoundHelper
import com.example.util.hideBottomNavigationView
import com.example.util.showBottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderDetailsFragment : Fragment() {
    private lateinit var binding: FragmentOrderDetailBinding
    private val args by navArgs<OrderDetailsFragmentArgs>()
    private val orderDetailsAdapter by lazy { OrderDetailProductAdapter() }
    private val detailsViewModel by viewModels<DetailsViewModel>()
    private val ordersViewModel by viewModels<OrdersViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomNavigationView()

        val order = args.order
        setupRv()

        binding.apply {
            // Back button (quiet, no noise)
            cardBackOrder.setOnClickListener {
                findNavController().navigateUp()
            }
            imageCloseOrder.setOnClickListener {
                findNavController().navigateUp()
            }

            // Header & Tracking Code
            tvOrderId.text = "Order #${order.orderId}"
            tvInvoiceOrderId.text = "#${order.orderId}"
            tvTrackingCode.text = "ARVN${order.orderId}"
            tvInvoiceDate.text = order.date.ifBlank { "Không xác định" }
            tvInvoicePaymentMethod.text = order.paymentMethod.ifBlank { "COD (Tiền mặt)" }
            tvInvoicePaymentStatus.text = order.paymentStatus.ifBlank { "Chờ thu tiền khi giao" }

            // Copy Order ID
            btnCopyOrderId.setOnClickListener {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Order ID", "#${order.orderId}")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Đã sao chép mã đơn hàng: #${order.orderId}", Toast.LENGTH_SHORT).show()
            }

            // Pricing
            tvSubtotalPrice.text = PriceCalculator.formatPriceVnd(order.totalPrice)
            tvTotalPrice.text = PriceCalculator.formatPriceVnd(order.totalPrice)

            // Address
            tvFullName.text = order.address.fullName.ifBlank { "Khách hàng TCFurnish" }
            tvPhoneNumber.text = order.address.phoneNumb.ifBlank { "Chưa cập nhật SĐT" }
            val addressParts = listOf(
                order.address.street,
                order.address.city,
                order.address.state
            ).filter { it.isNotBlank() }
            tvAddress.text = if (addressParts.isNotEmpty()) {
                addressParts.joinToString(", ")
            } else {
                "Địa chỉ nhận hàng mặc định"
            }

            // Products count
            val totalQty = order.products.sumOf { it.quantity }
            tvProductsCount.text = "${order.products.size} sản phẩm • Tổng $totalQty món"

            // Order Status & Timeline
            stepView.setSteps(
                mutableListOf(
                    "Đã đặt",
                    "Đã xác nhận",
                    "Đang giao",
                    "Đã giao"
                )
            )

            fun updateStatusUi(status: String) {
                val (statusText, statusDesc, stepIndex, bgColor, textColor) = when (status) {
                    "Ordered", "Đã đặt" -> Tuple5(
                        "ĐÃ ĐẶT",
                        "Đơn hàng đã được ghi nhận vào hệ thống và đang chờ cửa hàng xác nhận.",
                        0,
                        Color.parseColor("#FAF3E8"),
                        Color.parseColor("#B88A44")
                    )
                    "Confirmed", "Đã xác nhận" -> Tuple5(
                        "ĐÃ XÁC NHẬN",
                        "Đơn hàng đã được xác nhận. Đội ngũ TCFurnish đang chuẩn bị đóng gói sản phẩm.",
                        1,
                        Color.parseColor("#F3EBE1"),
                        Color.parseColor("#684128")
                    )
                    "Shipped", "Đang giao hàng", "Đang giao" -> Tuple5(
                        "ĐANG GIAO HÀNG",
                        "Đơn hàng đang trên đường vận chuyển tới địa chỉ nhận hàng của bạn.",
                        2,
                        Color.parseColor("#EFEBE9"),
                        Color.parseColor("#4A2C18")
                    )
                    "Delivered", "Đã giao hàng", "Đã giao" -> Tuple5(
                        "GIAO THÀNH CÔNG",
                        "Đơn hàng đã được giao thành công. Chúc bạn có trải nghiệm tuyệt vời với TCFurnish!",
                        3,
                        Color.parseColor("#E8F5E9"),
                        Color.parseColor("#2E7D32")
                    )
                    "Canceled", "Đã hủy" -> Tuple5(
                        "ĐÃ HỦY",
                        "Đơn hàng này đã bị hủy theo yêu cầu của bạn.",
                        0,
                        Color.parseColor("#FDF2F2"),
                        Color.parseColor("#A33B39")
                    )
                    else -> Tuple5(
                        status.uppercase().ifBlank { "ĐÃ ĐẶT" },
                        "Đơn hàng đang được cập nhật tiến trình trên hệ thống.",
                        0,
                        Color.parseColor("#F9F7F2"),
                        Color.parseColor("#786F66")
                    )
                }

                tvDetailStatusText.text = statusText
                tvDetailStatusText.setTextColor(textColor)
                tvStatusDescription.text = statusDesc

                val badgeBg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 24f
                    setColor(bgColor)
                }
                layoutDetailStatusBadge.background = badgeBg

                try {
                    stepView.go(stepIndex, false)
                    if (stepIndex == 3) {
                        stepView.done(true)
                    }
                } catch (ignored: Exception) {}

                // Check cancelable
                val isCancelable = status.equals("Ordered", ignoreCase = true) ||
                        status.equals("Đã đặt", ignoreCase = true) ||
                        status.equals("Confirmed", ignoreCase = true) ||
                        status.equals("Đã xác nhận", ignoreCase = true)

                if (isCancelable) {
                    btnCancelOrder.visibility = View.VISIBLE
                    btnCancelOrder.setOnClickListener {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Xác nhận hủy đơn hàng")
                            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #${order.orderId} (Tổng: ${PriceCalculator.formatPriceVnd(order.totalPrice)}) không?")
                            .setPositiveButton("Hủy đơn hàng") { dialog, _ ->
                                dialog.dismiss()
                                ordersViewModel.cancelOrder(order.orderId) { success, msg ->
                                    if (success) {
                                        SoundHelper.playSuccessSound()
                                        context?.let { ctx ->
                                            SoundHelper.showCancelNotification(ctx, order.orderId.toString())
                                        }
                                        updateStatusUi("Đã hủy")
                                        Toast.makeText(requireContext(), "Đã hủy đơn hàng #${order.orderId} thành công", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Lỗi hủy đơn: $msg", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .setNegativeButton("Đóng", null)
                            .show()
                    }
                } else {
                    btnCancelOrder.visibility = View.GONE
                }

                // Check if order is delivered to allow customer review
                val isDelivered = status.equals("Delivered", ignoreCase = true) ||
                        status.equals("Đã giao", ignoreCase = true) ||
                        status.equals("Hoàn thành", ignoreCase = true)

                if (isDelivered) {
                    btnRateOrder.visibility = View.VISIBLE
                    btnRateOrder.setOnClickListener {
                        showReviewDialog(order)
                    }
                } else {
                    btnRateOrder.visibility = View.GONE
                }
            }

            updateStatusUi(order.orderStatus)

            // Contact Support
            btnContactSupport.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Hỗ trợ đơn hàng #${order.orderId}")
                    .setMessage("Tổng đài CSKH: 1900 8888\nEmail: cskh@tcfurnish.vn\nGiờ làm việc: 08:30 - 21:30 hàng ngày\n\nBạn có muốn sao chép số tổng đài để liên hệ ngay?")
                    .setPositiveButton("Sao chép số") { _, _ ->
                        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Hotline", "19008888")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(requireContext(), "Đã sao chép: 19008888", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Đóng", null)
                    .show()
            }

            // Shopee Action: Re-order (Mua lại)
            btnReorder.setOnClickListener {
                SoundHelper.playSuccessSound()
                for (cartProd in order.products) {
                    detailsViewModel.addUpdateProductInCart(cartProd)
                }
                Toast.makeText(requireContext(), "Đã thêm ${order.products.size} sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.cartFragment)
            }
        }

        orderDetailsAdapter.differ.submitList(order.products)
    }

    private fun showReviewDialog(order: Order) {
        val dialog = android.app.Dialog(requireContext())
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        val dialogBinding = com.example.aroom.databinding.DialogAddReviewBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.tvDialogOrderInfo.text = "Đơn hàng #${order.orderId} • Đã nhận hàng thành công"

        dialogBinding.ratingBarInput.setOnRatingBarChangeListener { _, rating, _ ->
            dialogBinding.tvRatingDescription.text = when {
                rating >= 5 -> "Tuyệt vời - Rất hài lòng"
                rating >= 4 -> "Hài lòng - Chất lượng tốt"
                rating >= 3 -> "Bình thường - Tạm ổn"
                rating >= 2 -> "Chưa hài lòng"
                else -> "Rất thất vọng"
            }
        }

        dialogBinding.btnCancelReview.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSubmitReview.setOnClickListener {
            val comment = dialogBinding.edComment.text.toString().trim()
            val rating = dialogBinding.ratingBarInput.rating
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val userName = auth.currentUser?.displayName ?: "Khách hàng"
            val userEmail = auth.currentUser?.email ?: ""
            val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())

            for (cartProd in order.products) {
                val reviewId = java.util.UUID.randomUUID().toString()
                val review = com.example.data.Review(
                    reviewId,
                    cartProd.product.id,
                    order.orderId.toString(),
                    userName,
                    userEmail,
                    rating,
                    comment,
                    dateStr
                )
                firestore.collection("reviews").document(reviewId).set(review)
            }

            SoundHelper.playSuccessSound()
            Toast.makeText(requireContext(), "Cảm ơn bạn đã gửi đánh giá sản phẩm!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = orderDetailsAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavigationView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNavigationView()
    }

    private data class Tuple5<A, B, C, D, E>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
        val e: E
    )
}
