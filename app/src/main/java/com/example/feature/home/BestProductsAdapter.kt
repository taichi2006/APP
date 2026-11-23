package com.example.feature.home

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aroom.databinding.ProductRvItemBinding
import com.example.data.Product
import com.example.helper.PriceCalculator

class BestProductsAdapter : RecyclerView.Adapter<BestProductsAdapter.BestProductsViewHolder>() {

    inner class BestProductsViewHolder(private val binding: ProductRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                if (product.images.isNotEmpty()) {
                    Glide.with(itemView).load(product.images[0]).into(imgProduct)
                }
                tvName.text = product.name

                val ratingVal = if (product.rating > 0) product.rating else 4.9f
                tvProductRating.text = "★ ${String.format(java.util.Locale.US, "%.1f", ratingVal)}"
                tvSoldCount.text = "Đã bán ${product.soldCount}"

                val offer = product.offerPercentage
                if (offer != null && offer > 0) {
                    val percentInt = (offer * 100).toInt()
                    tvDiscountBadge.text = "-${percentInt}%"
                    tvDiscountBadge.visibility = View.VISIBLE

                    val priceAfterOffer = PriceCalculator.getProductPrice(offer, product.price)
                    tvPrice.text = PriceCalculator.formatPriceVnd(priceAfterOffer)
                    tvPrice.paintFlags = 0 // Clear strike thru for current payable price

                    tvNewPrice.text = PriceCalculator.formatPriceVnd(product.price)
                    tvNewPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    tvNewPrice.visibility = View.VISIBLE
                } else {
                    tvDiscountBadge.visibility = View.GONE
                    tvPrice.text = PriceCalculator.formatPriceVnd(product.price)
                    tvPrice.paintFlags = 0
                    tvNewPrice.visibility = View.GONE
                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestProductsViewHolder {
        return BestProductsViewHolder(
            ProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.count()
    }

    override fun onBindViewHolder(holder: BestProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    var onClick: ((Product) -> Unit)? = null
}
