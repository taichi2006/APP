package com.example.feature.order

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
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
                tvBillingProductQuantity.text = itemView.context.getString(R.string.quantity_format, billingProduct.quantity)
                tvProductCartPrice.text = PriceCalculator.formatPriceVnd(billingProduct.product.price)

                billingProduct.product.offerPercentage?.let {
                    val priceAfterOffer =
                        PriceCalculator.getProductPrice(billingProduct.product.offerPercentage, billingProduct.product.price)
                    tvProductCartPrice.text = PriceCalculator.formatPriceVnd(priceAfterOffer)
                }

                val colorVal = billingProduct.selectedColor
                val hasColor = colorVal != null && colorVal != 0
                if (hasColor) {
                    tvBillingColorLabel.visibility = View.VISIBLE
                    imageCartProductColor.visibility = View.VISIBLE
                    imageCartProductColor.setImageDrawable(ColorDrawable(colorVal!!))
                } else {
                    tvBillingColorLabel.visibility = View.GONE
                    imageCartProductColor.visibility = View.GONE
                }

                val sizeText = billingProduct.selectedSize
                val hasSize = !sizeText.isNullOrBlank()
                if (hasSize) {
                    tvCartProductSize.visibility = View.VISIBLE
                    tvCartProductSize.text = sizeText
                    val params = tvCartProductSize.layoutParams as ViewGroup.MarginLayoutParams
                    params.marginStart = if (hasColor) (6 * itemView.resources.displayMetrics.density).toInt() else 0
                    tvCartProductSize.layoutParams = params
                } else {
                    tvCartProductSize.visibility = View.GONE
                }

                layoutBillingOptions.visibility = if (hasColor || hasSize) View.VISIBLE else View.GONE
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
