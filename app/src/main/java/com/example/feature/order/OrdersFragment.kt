package com.example.feature.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aroom.databinding.FragmentOrdersBinding
import com.example.data.order.Order
import com.example.helper.PriceCalculator
import com.example.util.Resource
import com.example.util.SoundHelper
import com.example.util.hideBottomNavigationView
import com.example.util.showBottomNavigationView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class OrdersFragment : Fragment() {
    private lateinit var binding: FragmentOrdersBinding
    private val viewModel by viewModels<OrdersViewModel>()
    private val orderAdapter by lazy { OrdersAdapter() }
    private val args by navArgs<OrdersFragmentArgs>()

    private var allOrdersList: List<Order> = emptyList()
    private var currentTabIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomNavigationView()

        setupRv()
        setupTabs()

        binding.cardBackOrders.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.imageCloseOrders.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnShopNow.setOnClickListener {
            findNavController().navigateUp()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.orders.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAllOrders.visibility = View.VISIBLE
                        binding.layoutEmptyOrders.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressbarAllOrders.visibility = View.GONE
                        allOrdersList = it.data ?: emptyList()
                        binding.tvOrdersCount.text = "${allOrdersList.size} đơn hàng trong hệ thống"
                        filterOrdersByTab(currentTabIndex)
                    }
                    is Resource.Error -> {
                        binding.progressbarAllOrders.visibility = View.GONE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        orderAdapter.onClick = { order ->
            val action = OrdersFragmentDirections.actionOrdersFragmentToOrderDetailsFragment(order)
            findNavController().navigate(action)
        }

        orderAdapter.onCancelClick = { order ->
            AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #${order.orderId} (Tổng: ${PriceCalculator.formatPriceVnd(order.totalPrice)}) không?")
                .setPositiveButton("Hủy đơn hàng") { dialog, _ ->
                    dialog.dismiss()
                    viewModel.cancelOrder(order.orderId) { success, msg ->
                        if (success) {
                            SoundHelper.playSuccessSound()
                            context?.let { ctx ->
                                SoundHelper.showCancelNotification(ctx, order.orderId.toString())
                            }
                            Toast.makeText(requireContext(), "Đã hủy đơn hàng #${order.orderId} thành công", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Lỗi hủy đơn: $msg", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Đóng", null)
                .show()
        }
    }

    private fun setupTabs() {
        val tabTitles = listOf(
            "Tất cả",
            "Đã đặt",
            "Đã xác nhận",
            "Đang giao",
            "Đã giao",
            "Đã hủy"
        )

        binding.tabLayoutOrders.removeAllTabs()
        for (title in tabTitles) {
            binding.tabLayoutOrders.addTab(binding.tabLayoutOrders.newTab().setText(title))
        }

        // Pre-select tab if passed from ProfileFragment
        val initialStatus = args.initialStatus
        val initialIndex = when {
            initialStatus.contains("đặt", ignoreCase = true) -> 1
            initialStatus.contains("xác nhận", ignoreCase = true) -> 2
            initialStatus.contains("giao", ignoreCase = true) && !initialStatus.contains("đã", ignoreCase = true) -> 3
            initialStatus.contains("đã giao", ignoreCase = true) -> 4
            initialStatus.contains("hủy", ignoreCase = true) -> 5
            else -> 0
        }

        currentTabIndex = initialIndex
        binding.tabLayoutOrders.getTabAt(initialIndex)?.select()

        binding.tabLayoutOrders.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                currentTabIndex = position
                filterOrdersByTab(position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                filterOrdersByTab(position)
            }
        })
    }

    private fun filterOrdersByTab(tabIndex: Int) {
        val filteredList = when (tabIndex) {
            1 -> allOrdersList.filter { it.orderStatus.equals("Ordered", ignoreCase = true) || it.orderStatus.equals("Đã đặt", ignoreCase = true) }
            2 -> allOrdersList.filter { it.orderStatus.equals("Confirmed", ignoreCase = true) || it.orderStatus.equals("Đã xác nhận", ignoreCase = true) }
            3 -> allOrdersList.filter { it.orderStatus.equals("Shipped", ignoreCase = true) || it.orderStatus.contains("Đang giao", ignoreCase = true) }
            4 -> allOrdersList.filter { it.orderStatus.equals("Delivered", ignoreCase = true) || it.orderStatus.contains("Đã giao", ignoreCase = true) }
            5 -> allOrdersList.filter { it.orderStatus.equals("Canceled", ignoreCase = true) || it.orderStatus.contains("Đã hủy", ignoreCase = true) }
            else -> allOrdersList
        }

        orderAdapter.differ.submitList(filteredList)

        if (filteredList.isEmpty()) {
            binding.layoutEmptyOrders.visibility = View.VISIBLE
            binding.rvAllOrders.visibility = View.GONE
            val tabName = binding.tabLayoutOrders.getTabAt(tabIndex)?.text ?: "này"
            binding.tvEmptySubtitle.text = "Bạn chưa có đơn hàng nào ở trạng thái \"$tabName\""
        } else {
            binding.layoutEmptyOrders.visibility = View.GONE
            binding.rvAllOrders.visibility = View.VISIBLE
        }
    }

    private fun setupRv() {
        binding.rvAllOrders.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = orderAdapter
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
}
