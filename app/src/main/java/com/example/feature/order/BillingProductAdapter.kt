package com.example.feature.order

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.aroom.R
import com.example.aroom.databinding.BillingProductsRvItemBinding
import com.example.data.CartProduct
import com.example.helper.PriceCalculator

class BillingProductAdapter : RecyclerView.Adapter<BillingProductAdapter.BillingViewHolder>() {

    inner class BillingViewHolder(val binding: BillingProductsRvItemBinding) : ViewHolder(binding.root) {
        fun bind(billingProduct: CartProduct) {
            binding.apply {
                val firstImg = billingProduct.product.images.firstOrNull() ?: ""
                Glide.with(itemView)
                    .load(firstImg)
                    .placeholder(R.drawable.ic_cart)
                    .into(imageCartProduct)

                tvProductCartName.text = billingProduct.product.name
                tvBillingProductQuantity.text = billingProduct.quantity.toString()
                tvProductCartPrice.text = PriceCalculator.formatPriceVnd(billingProduct.product.price)

                billingProduct.product.offerPercentage?.let {
                    val priceAfterOffer =
                        PriceCalculator.getProductPrice(billingProduct.product.offerPercentage, billingProduct.product.price)
                    tvProductCartPrice.text = PriceCalculator.formatPriceVnd(priceAfterOffer)
                }

                imageCartProductColor.setImageDrawable(
                    ColorDrawable(
                        billingProduct.selectedColor ?: Color.TRANSPARENT
                    )
                )
                tvCartProductSize.text = billingProduct.selectedSize ?: "".also {
                    imageCartProductSize.setImageDrawable(
                        ColorDrawable(Color.TRANSPARENT)
                    )
                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingViewHolder {
        return BillingViewHolder(
            BillingProductsRvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BillingViewHolder, position: Int) {
        val billingProduct = differ.currentList[position]
        holder.bind(billingProduct)
    }
}
