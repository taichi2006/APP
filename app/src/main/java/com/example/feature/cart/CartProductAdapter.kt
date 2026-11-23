package com.example.feature.cart

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aroom.databinding.CartProductItemBinding
import com.example.data.CartProduct
import com.example.helper.PriceCalculator

class CartProductAdapter : RecyclerView.Adapter<CartProductAdapter.CartProductViewHolder> (){

    inner class CartProductViewHolder( val binding: CartProductItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(cartProduct: CartProduct){
            binding.apply {
                Glide.with(itemView).load(cartProduct.product.images[0]).into(imgCartProduct)
                tvProductCartName.text = cartProduct.product.name
                tvCartProdQuantity.text = Math.max(1, cartProduct.quantity).toString()

                val priceAfterOffer = PriceCalculator.getProductPrice(cartProduct.product.offerPercentage, cartProduct.product.price)
                tvProductCartPrice.text = PriceCalculator.formatPriceVnd(priceAfterOffer)

                val colorVal = cartProduct.selectedColor
                val hasColor = colorVal != null && colorVal != 0
                if (hasColor) {
                    tvCartColorLabel.visibility = View.VISIBLE
                    imgCartProdColor.visibility = View.VISIBLE
                    imgCartProdColor.setImageDrawable(ColorDrawable(colorVal!!))
                } else {
                    tvCartColorLabel.visibility = View.GONE
                    imgCartProdColor.visibility = View.GONE
                }

                val sizeText = cartProduct.selectedSize
                val hasSize = !sizeText.isNullOrBlank()
                if (hasSize) {
                    tvCartProdSize.visibility = View.VISIBLE
                    tvCartProdSize.text = sizeText
                    val params = tvCartProdSize.layoutParams as ViewGroup.MarginLayoutParams
                    params.marginStart = if (hasColor) (8 * itemView.resources.displayMetrics.density).toInt() else 0
                    tvCartProdSize.layoutParams = params
                } else {
                    tvCartProdSize.visibility = View.GONE
                }

                layoutOptions.visibility = if (hasColor || hasSize) View.VISIBLE else View.GONE
            }
        }

    }

    private val diffCallback = object : DiffUtil.ItemCallback<CartProduct>(){
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem && oldItem.quantity == newItem.quantity
        }
    }

    val differ = AsyncListDiffer(this,diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductViewHolder {
        return CartProductViewHolder(
            CartProductItemBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.count()
    }

    override fun onBindViewHolder(holder: CartProductViewHolder, position: Int) {
        val cartProduct = differ.currentList[position]
        holder.bind(cartProduct)

        holder.itemView.setOnClickListener {
            onProductClick?.invoke(cartProduct)
        }

        holder.binding.btnCheckoutSingleItem.setOnClickListener {
            onProductClick?.invoke(cartProduct)
        }

        holder.binding.imgPlus.setOnClickListener {
            onPlusClick?.invoke(cartProduct)
        }

        holder.binding.imgMinus.setOnClickListener {
            onMinusClick?.invoke(cartProduct)
        }



    }

    var onProductClick:( (CartProduct) -> Unit )?  = null
    var onPlusClick:( (CartProduct) -> Unit )?  = null
    var onMinusClick:( (CartProduct) -> Unit )?  = null
}