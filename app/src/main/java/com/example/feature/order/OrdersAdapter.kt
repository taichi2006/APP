package com.example.feature.order

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.aroom.R
import com.example.aroom.databinding.OrderItemBinding
import com.example.data.order.Order
import com.example.helper.PriceCalculator

class OrdersAdapter : RecyclerView.Adapter<OrdersAdapter.OrdersViewHolder>() {

    inner class OrdersViewHolder(private val binding: OrderItemBinding) : ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.apply {
                tvOrderId.text = "#${order.orderId}"
                tvOrderDate.text = order.date
                tvOrderTotalPrice.text = PriceCalculator.formatPriceVnd(order.totalPrice)
                tvOrderPaymentMethod.text = order.paymentMethod.ifBlank { "COD" }

                // First product thumbnail & name
                val firstProduct = order.products.firstOrNull()
                if (firstProduct != null) {
                    tvOrderProductName.text = firstProduct.product.name
                    val firstImg = firstProduct.product.images.firstOrNull() ?: ""
                    Glide.with(itemView)
                        .load(firstImg)
                        .placeholder(R.drawable.ic_cart)
                        .into(imageOrderThumb)

                    if (order.products.size > 1) {
                        val extraCount = order.products.size - 1
                        val totalQty = order.products.sumOf { it.quantity }
                        tvOrderProductExtra.text = "+$extraCount sản phẩm khác • Tổng $totalQty món"
                    } else {
                        tvOrderProductExtra.text = "Số lượng: ${firstProduct.quantity} món"
                    }
                } else {
                    tvOrderProductName.text = "Đơn hàng nội thất TCFurnish"
                    tvOrderProductExtra.text = "Chi tiết đơn hàng"
                    imageOrderThumb.setImageResource(R.drawable.ic_cart)
                }

                // Shopee-style Status text with Noble Luxury colors
                val (statusText, statusColor) = when (order.orderStatus) {
                    "Ordered", "Đã đặt" -> Pair("ĐÃ ĐẶT", Color.parseColor("#B88A44"))
                    "Confirmed", "Đã xác nhận" -> Pair("ĐÃ XÁC NHẬN", Color.parseColor("#684128"))
                    "Shipped", "Đang giao hàng", "Đang giao" -> Pair("ĐANG GIAO HÀNG", Color.parseColor("#4A2C18"))
                    "Delivered", "Đã giao hàng", "Đã giao" -> Pair("GIAO THÀNH CÔNG", Color.parseColor("#2E7D32"))
                    "Canceled", "Đã hủy" -> Pair("ĐÃ HỦY", Color.parseColor("#A33B39"))
                    else -> Pair(order.orderStatus.uppercase().ifBlank { "ĐÃ ĐẶT" }, Color.parseColor("#786F66"))
                }

                tvOrderStatusBadge.text = statusText
                tvOrderStatusBadge.setTextColor(statusColor)

                // Cancel button on order card
                val isCancelable = order.orderStatus.equals("Ordered", ignoreCase = true) ||
                        order.orderStatus.equals("Đã đặt", ignoreCase = true) ||
                        order.orderStatus.equals("Confirmed", ignoreCase = true) ||
                        order.orderStatus.equals("Đã xác nhận", ignoreCase = true)

                if (isCancelable) {
                    btnCancelOrderItem.visibility = View.VISIBLE
                    btnCancelOrderItem.setOnClickListener {
                        onCancelClick?.invoke(order)
                    }
                } else {
                    btnCancelOrderItem.visibility = View.GONE
                }

                // Clicks
                cardOrder.setOnClickListener {
                    onClick?.invoke(order)
                }
                btnViewOrderDetail.setOnClickListener {
                    onClick?.invoke(order)
                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.orderId == newItem.orderId &&
                    oldItem.orderStatus == newItem.orderStatus &&
                    oldItem.totalPrice == newItem.totalPrice &&
                    oldItem.products.size == newItem.products.size
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        return OrdersViewHolder(
            OrderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Order) -> Unit)? = null
    var onCancelClick: ((Order) -> Unit)? = null
}
