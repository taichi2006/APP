package com.example.feature.cart.ui;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aroom.databinding.CartProductItemBinding;
import com.example.core.util.PriceCalculator;
import com.example.model.CartProduct;

import java.util.Objects;

public class CartProductAdapter extends RecyclerView.Adapter<CartProductAdapter.CartProductViewHolder> {

    public interface OnItemClickListener {
        void onClick(CartProduct cartProduct);
    }

    private OnItemClickListener onProductClick;
    private OnItemClickListener onPlusClick;
    private OnItemClickListener onMinusClick;

    public void setOnProductClick(OnItemClickListener onProductClick) { this.onProductClick = onProductClick; }
    public void setOnPlusClick(OnItemClickListener onPlusClick) { this.onPlusClick = onPlusClick; }
    public void setOnMinusClick(OnItemClickListener onMinusClick) { this.onMinusClick = onMinusClick; }

    public class CartProductViewHolder extends RecyclerView.ViewHolder {
        final CartProductItemBinding binding;

        public CartProductViewHolder(@NonNull CartProductItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartProduct cartProduct) {
            if (cartProduct == null || cartProduct.getProduct() == null) return;

            if (cartProduct.getProduct().getImages() != null && !cartProduct.getProduct().getImages().isEmpty()) {
                Glide.with(itemView).load(cartProduct.getProduct().getImages().get(0)).into(binding.imgCartProduct);
            }

            binding.tvProductCartName.setText(cartProduct.getProduct().getName());
            binding.tvCartProdQuantity.setText(String.valueOf(Math.max(1, cartProduct.getQuantity())));

            float priceAfterOffer = PriceCalculator.getProductPrice(cartProduct.getProduct().getOfferPercentage(), cartProduct.getProduct().getPrice());
            binding.tvProductCartPrice.setText(PriceCalculator.formatPriceVnd(priceAfterOffer));

            Integer colorVal = cartProduct.getSelectedColor();
            boolean hasColor = colorVal != null && colorVal != 0;
            if (hasColor) {
                binding.tvCartColorLabel.setVisibility(View.VISIBLE);
                binding.imgCartProdColor.setVisibility(View.VISIBLE);
                binding.imgCartProdColor.setImageDrawable(new ColorDrawable(colorVal));
            } else {
                binding.tvCartColorLabel.setVisibility(View.GONE);
                binding.imgCartProdColor.setVisibility(View.GONE);
            }

            String sizeText = cartProduct.getSelectedSize();
            boolean hasSize = sizeText != null && !sizeText.trim().isEmpty();
            if (hasSize) {
                binding.tvCartProdSize.setVisibility(View.VISIBLE);
                binding.tvCartProdSize.setText(sizeText);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.tvCartProdSize.getLayoutParams();
                params.setMarginStart(hasColor ? (int) (8 * itemView.getResources().getDisplayMetrics().density) : 0);
                binding.tvCartProdSize.setLayoutParams(params);
            } else {
                binding.tvCartProdSize.setVisibility(View.GONE);
            }

            binding.layoutOptions.setVisibility((hasColor || hasSize) ? View.VISIBLE : View.GONE);
        }
    }

    private final DiffUtil.ItemCallback<CartProduct> diffCallback = new DiffUtil.ItemCallback<CartProduct>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartProduct oldItem, @NonNull CartProduct newItem) {
            if (oldItem.getProduct() == null || newItem.getProduct() == null) return false;
            return Objects.equals(oldItem.getProduct().getId(), newItem.getProduct().getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartProduct oldItem, @NonNull CartProduct newItem) {
            return Objects.equals(oldItem, newItem) && oldItem.getQuantity() == newItem.getQuantity();
        }
    };

    public final AsyncListDiffer<CartProduct> differ = new AsyncListDiffer<>(this, diffCallback);

    @NonNull
    @Override
    public CartProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CartProductItemBinding binding = CartProductItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CartProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartProductViewHolder holder, int position) {
        CartProduct cartProduct = differ.getCurrentList().get(position);
        holder.bind(cartProduct);

        holder.itemView.setOnClickListener(v -> {
            if (onProductClick != null) onProductClick.onClick(cartProduct);
        });

        holder.binding.btnCheckoutSingleItem.setOnClickListener(v -> {
            if (onProductClick != null) onProductClick.onClick(cartProduct);
        });

        holder.binding.imgPlus.setOnClickListener(v -> {
            if (onPlusClick != null) onPlusClick.onClick(cartProduct);
        });

        holder.binding.imgMinus.setOnClickListener(v -> {
            if (onMinusClick != null) onMinusClick.onClick(cartProduct);
        });
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }
}
