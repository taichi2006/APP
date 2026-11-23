package com.example.feature.order

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aroom.R
import com.example.aroom.databinding.FragmentBillingBinding
import com.example.data.Address
import com.example.data.CartProduct
import com.example.data.order.Order
import com.example.data.order.OrderStatus
import com.example.helper.PriceCalculator
import com.example.util.HorizontalItemDecoration
import com.example.util.Resource
import com.example.util.SoundHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class BillingFragment : Fragment() {
    private lateinit var binding: FragmentBillingBinding
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductAdapter by lazy { BillingProductAdapter() }
    private val billingViewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f
    private var discountAmount = 0f
    private var appliedVoucherCode: String? = null

    private val finalTotalPrice: Float
        get() = maxOf(0f, totalPrice - discountAmount)

    private var selectedAddress: Address? = null
    private var selectedPaymentMethod = "COD"
    private val orderViewModel by viewModels<PlaceOrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        products = args.products.toList()
        totalPrice = args.totalPrice
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAddressRv()
        setupBillingProductsRv()
        setupVoucherSection()

        if (!args.payment) {
            binding.apply {
                buttonPlaceOrder.visibility = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                cardPaymentSection.visibility = View.INVISIBLE
                cardVoucherSection.visibility = View.INVISIBLE
            }
        }

        binding.apply {
            imageAddAddress.setOnClickListener {
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
            }
        }

        lifecycleScope.launchWhenStarted {
            billingViewModel.addresses.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        addressAdapter.differ.submitList(it.data)
                        binding.progressbarAddress.visibility = View.GONE
                        if (!it.data.isNullOrEmpty() && selectedAddress == null) {
                            selectedAddress = it.data[0]
                        }
                    }
                    is Resource.Error -> {
                        binding.progressbarAddress.visibility = View.GONE
                        Toast.makeText(requireContext(), "Lỗi tải địa chỉ: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            orderViewModel.order.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.buttonPlaceOrder.startAnimation()
                    }
                    is Resource.Success -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        val placedOrder = it.data
                        SoundHelper.playSuccessSound()

                        val orderIdStr = placedOrder?.orderId?.toString() ?: "${(System.currentTimeMillis() % 100000)}"
                        val methodLabel = if (selectedPaymentMethod == "Sandbox") {
                            "Cổng thanh toán Sandbox (Test Gateway)"
                        } else {
                            "Thanh toán tiền mặt khi nhận hàng (COD)"
                        }

                        // Gửi thông báo hệ thống trên Notification Bar
                        context?.let { ctx ->
                            SoundHelper.showOrderNotification(
                                ctx,
                                orderIdStr,
                                PriceCalculator.formatPriceVnd(finalTotalPrice),
                                methodLabel
                            )
                        }

                        // Hiển thị hộp thoại thông báo đặt hàng thành công chuyên nghiệp
                        showOrderSuccessDialog(orderIdStr, methodLabel)
                    }
                    is Resource.Error -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        Toast.makeText(requireContext(), "Lỗi đặt hàng: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        billingProductAdapter.differ.submitList(products)

        binding.apply {
            tvSubtotalPrice.text = PriceCalculator.formatPriceVnd(totalPrice)
            tvTotalPrice.text = PriceCalculator.formatPriceVnd(finalTotalPrice)

            imageCloseBilling.setOnClickListener {
                findNavController().navigateUp()
            }

            addressAdapter.onClick = {
                selectedAddress = it
                if (!args.payment) {
                    val b = Bundle().apply { putParcelable("address", selectedAddress) }
                    findNavController().navigate(R.id.action_billingFragment_to_addressFragment, b)
                }
            }

            // Quản lý chọn phương thức thanh toán: Chỉ còn Tiền mặt (COD) và Cổng Sandbox
            layoutOptionCod.setOnClickListener {
                radioCod.isChecked = true
                radioSandbox.isChecked = false
                selectedPaymentMethod = "COD"
                layoutSandboxDetails.visibility = View.GONE
            }
            radioCod.setOnClickListener { layoutOptionCod.performClick() }

            layoutOptionSandbox.setOnClickListener {
                radioCod.isChecked = false
                radioSandbox.isChecked = true
                selectedPaymentMethod = "Sandbox"
                layoutSandboxDetails.visibility = View.VISIBLE
            }
            radioSandbox.setOnClickListener { layoutOptionSandbox.performClick() }

            buttonPlaceOrder.setOnClickListener {
                if (selectedAddress == null) {
                    Toast.makeText(requireContext(), "Vui lòng chọn địa chỉ nhận hàng", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                SoundHelper.playClickSound()

                if (selectedPaymentMethod == "Sandbox") {
                    // Sandbox: Tự động cập nhật đã thanh toán mà không cần xác nhận lại
                    val randomTxnId = "SBX${System.currentTimeMillis() % 1000000}"
                    val order = Order(
                        OrderStatus.Ordered.status,
                        finalTotalPrice,
                        products,
                        selectedAddress!!,
                        "Cổng thanh toán Sandbox (Trực tuyến)",
                        "Đã thanh toán qua Sandbox (Mã GD: $randomTxnId)"
                    )
                    Toast.makeText(requireContext(), "Đã tự động thanh toán qua Sandbox (Mã GD: $randomTxnId)", Toast.LENGTH_SHORT).show()
                    orderViewModel.placeOrder(order)
                } else {
                    // Tiền mặt: Nhận hàng sẽ đưa tiền (COD)
                    val order = Order(
                        OrderStatus.Ordered.status,
                        finalTotalPrice,
                        products,
                        selectedAddress!!,
                        "Thanh toán tiền mặt khi nhận hàng (COD)",
                        "Chờ thu tiền mặt khi giao hàng"
                    )
                    orderViewModel.placeOrder(order)
                }
            }
        }
    }

    private fun setupVoucherSection() {
        fun applyVoucher(code: String) {
            val cleanCode = code.trim().uppercase()
            when (cleanCode) {
                "TCFURNISH10", "AROOM10" -> {
                    appliedVoucherCode = "TCFURNISH10"
                    discountAmount = totalPrice * 0.10f
                    binding.tvVoucherStatus.text = "Đã áp dụng mã -10%"
                    binding.tvVoucherStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_success))
                    binding.layoutVoucherDiscount.visibility = View.VISIBLE
                    binding.tvVoucherDiscount.text = "-${PriceCalculator.formatPriceVnd(discountAmount)}"
                    binding.tvTotalPrice.text = PriceCalculator.formatPriceVnd(finalTotalPrice)
                    binding.edVoucherCode.setText("TCFURNISH10")
                    Toast.makeText(requireContext(), "Áp dụng mã TCFURNISH10 thành công (-10%)", Toast.LENGTH_SHORT).show()
                    SoundHelper.playSuccessSound()
                }
                "VIP20" -> {
                    appliedVoucherCode = "VIP20"
                    discountAmount = totalPrice * 0.20f
                    binding.tvVoucherStatus.text = "Đã áp dụng mã -20%"
                    binding.tvVoucherStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_success))
                    binding.layoutVoucherDiscount.visibility = View.VISIBLE
                    binding.tvVoucherDiscount.text = "-${PriceCalculator.formatPriceVnd(discountAmount)}"
                    binding.tvTotalPrice.text = PriceCalculator.formatPriceVnd(finalTotalPrice)
                    binding.edVoucherCode.setText("VIP20")
                    Toast.makeText(requireContext(), "Áp dụng mã VIP20 thành công (-20%)", Toast.LENGTH_SHORT).show()
                    SoundHelper.playSuccessSound()
                }
                "NOITHAT100K" -> {
                    appliedVoucherCode = "NOITHAT100K"
                    discountAmount = minOf(totalPrice, 100000f)
                    binding.tvVoucherStatus.text = "Đã áp dụng mã -100.000đ"
                    binding.tvVoucherStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_success))
                    binding.layoutVoucherDiscount.visibility = View.VISIBLE
                    binding.tvVoucherDiscount.text = "-${PriceCalculator.formatPriceVnd(discountAmount)}"
                    binding.tvTotalPrice.text = PriceCalculator.formatPriceVnd(finalTotalPrice)
                    binding.edVoucherCode.setText("NOITHAT100K")
                    Toast.makeText(requireContext(), "Áp dụng mã NOITHAT100K thành công (-100.000đ)", Toast.LENGTH_SHORT).show()
                    SoundHelper.playSuccessSound()
                }
                else -> {
                    Toast.makeText(requireContext(), "Mã giảm giá không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnApplyVoucher.setOnClickListener {
            val code = binding.edVoucherCode.text.toString().trim()
            if (code.isNotEmpty()) {
                applyVoucher(code)
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show()
            }
        }

        binding.chipVoucher10.setOnClickListener {
            applyVoucher("TCFURNISH10")
        }

        binding.chipVoucher20.setOnClickListener {
            applyVoucher("VIP20")
        }

        binding.chipVoucher100k.setOnClickListener {
            applyVoucher("NOITHAT100K")
        }
    }

    private fun showOrderSuccessDialog(orderIdStr: String, methodLabel: String) {
        val addressText = selectedAddress?.let { "${it.street}, ${it.city}" } ?: "Địa chỉ của bạn"

        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("🎉 Đặt hàng thành công!")
            setMessage(
                "Cảm ơn bạn đã mua sắm tại TCFurnish!\n\n" +
                "• Mã đơn hàng: #$orderIdStr\n" +
                "• Hình thức: $methodLabel\n" +
                "• Tổng thanh toán: ${PriceCalculator.formatPriceVnd(finalTotalPrice)}\n" +
                (if (discountAmount > 0) "• Giảm giá voucher: -${PriceCalculator.formatPriceVnd(discountAmount)}\n" else "") +
                "• Giao đến: $addressText\n\n" +
                "Đơn hàng của bạn đã được ghi nhận trên hệ thống và đang được chuẩn bị để vận chuyển."
            )
            setCancelable(false)
            setPositiveButton("Theo dõi đơn hàng") { dialog, _ ->
                dialog.dismiss()
                try {
                    findNavController().navigate(R.id.action_billingFragment_to_ordersFragment)
                } catch (e: Exception) {
                    findNavController().navigateUp()
                }
            }
            setNegativeButton("Về trang chủ") { dialog, _ ->
                dialog.dismiss()
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }
        alertDialog.create().show()
    }

    private fun setupAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }
}
