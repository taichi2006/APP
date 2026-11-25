package com.example.feature.order.ui;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aroom.R;
import com.example.aroom.databinding.BillingProductsRvItemBinding;
import com.example.core.util.PriceCalculator;
import com.example.model.CartProduct;

import java.util.Objects;

public class BillingProductsAdapter extends RecyclerView.Adapter<BillingProductsAdapter.BillingViewHolder> {

    public static class BillingViewHolder extends RecyclerView.ViewHolder {
        public final BillingProductsRvItemBinding binding;

        public BillingViewHolder(@NonNull BillingProductsRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartProduct billingProduct) {
            if (billingProduct == null || billingProduct.getProduct() == null) return;

            String firstImg = "";
            if (billingProduct.getProduct().getImages() != null && !billingProduct.getProduct().getImages().isEmpty()) {
                firstImg = billingProduct.getProduct().getImages().get(0);
            }

            Glide.with(itemView)
                .load(firstImg)
                .placeholder(R.drawable.ic_cart)
                .into(binding.imageCartProduct);

            binding.tvProductCartName.setText(billingProduct.getProduct().getName());
            binding.tvBillingProductQuantity.setText(
                itemView.getContext().getString(R.string.quantity_format, billingProduct.getQuantity())
            );

            Float offerPercentage = billingProduct.getProduct().getOfferPercentage();
            if (offerPercentage != null) {
                float priceAfterOffer = PriceCalculator.getProductPrice(offerPercentage, billingProduct.getProduct().getPrice());
                binding.tvProductCartPrice.setText(PriceCalculator.formatPriceVnd(priceAfterOffer));
            } else {
                binding.tvProductCartPrice.setText(PriceCalculator.formatPriceVnd(billingProduct.getProduct().getPrice()));
            }

            Integer colorVal = billingProduct.getSelectedColor();
            boolean hasColor = colorVal != null && colorVal != 0;
            if (hasColor) {
                binding.tvBillingColorLabel.setVisibility(View.VISIBLE);
                binding.imageCartProductColor.setVisibility(View.VISIBLE);
                binding.imageCartProductColor.setImageDrawable(new ColorDrawable(colorVal));
            } else {
                binding.tvBillingColorLabel.setVisibility(View.GONE);
                binding.imageCartProductColor.setVisibility(View.GONE);
            }

            String sizeText = billingProduct.getSelectedSize();
            boolean hasSize = sizeText != null && !sizeText.trim().isEmpty();
            if (hasSize) {
                binding.tvCartProductSize.setVisibility(View.VISIBLE);
                binding.tvCartProductSize.setText(sizeText);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.tvCartProductSize.getLayoutParams();
                params.setMarginStart(hasColor ? (int) (6 * itemView.getResources().getDisplayMetrics().density) : 0);
                binding.tvCartProductSize.setLayoutParams(params);
            } else {
                binding.tvCartProductSize.setVisibility(View.GONE);
            }

            binding.layoutBillingOptions.setVisibility((hasColor || hasSize) ? View.VISIBLE : View.GONE);
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
            return Objects.equals(oldItem, newItem);
        }
    };

    public final AsyncListDiffer<CartProduct> differ = new AsyncListDiffer<>(this, diffCallback);

    @NonNull
    @Override
    public BillingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BillingProductsRvItemBinding binding = BillingProductsRvItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new BillingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BillingViewHolder holder, int position) {
        CartProduct billingProduct = differ.getCurrentList().get(position);
        holder.bind(billingProduct);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }
}
