package com.example.feature.order

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aroom.R
import com.example.aroom.databinding.OrderDetailProductItemBinding
import com.example.data.CartProduct
import com.example.helper.PriceCalculator

class OrderDetailProductAdapter : RecyclerView.Adapter<OrderDetailProductAdapter.OrderDetailViewHolder>() {

    inner class OrderDetailViewHolder(val binding: OrderDetailProductItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cartProduct: CartProduct) {
            binding.apply {
                tvProductName.text = cartProduct.product.name

                val firstImg = cartProduct.product.images.firstOrNull() ?: ""
                Glide.with(itemView)
                    .load(firstImg)
                    .placeholder(R.drawable.ic_cart)
                    .into(imgProduct)

                val unitPrice = if (cartProduct.product.offerPercentage != null) {
                    PriceCalculator.getProductPrice(cartProduct.product.offerPercentage, cartProduct.product.price)
                } else {
                    cartProduct.product.price
                }

                val lineTotal = unitPrice * cartProduct.quantity
                tvProductPrice.text = PriceCalculator.formatPriceVnd(lineTotal)
                tvProductQuantity.text = "x${cartProduct.quantity}"

                if (cartProduct.quantity > 1) {
                    tvProductUnitPrice.visibility = View.VISIBLE
                    tvProductUnitPrice.text = "Đơn giá: ${PriceCalculator.formatPriceVnd(unitPrice)}"
                } else {
                    tvProductUnitPrice.visibility = View.GONE
                }

                val colorVal = cartProduct.selectedColor
                if (colorVal != null && colorVal != 0) {
                    imgProductColor.visibility = View.VISIBLE
                    imgProductColor.setImageDrawable(ColorDrawable(colorVal))
                } else {
                    imgProductColor.visibility = View.GONE
                }

                if (!cartProduct.selectedSize.isNullOrBlank()) {
                    tvProductSize.visibility = View.VISIBLE
                    tvProductSize.text = cartProduct.selectedSize
                } else {
                    tvProductSize.visibility = View.GONE
                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product.id == newItem.product.id &&
                    oldItem.selectedColor == newItem.selectedColor &&
                    oldItem.selectedSize == newItem.selectedSize
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.quantity == newItem.quantity &&
                    oldItem.product.id == newItem.product.id &&
                    oldItem.selectedColor == newItem.selectedColor &&
                    oldItem.selectedSize == newItem.selectedSize
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        return OrderDetailViewHolder(
            OrderDetailProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderDetailViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}
