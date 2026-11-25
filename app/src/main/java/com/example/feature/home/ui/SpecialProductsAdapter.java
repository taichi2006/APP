package com.example.feature.home.ui;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aroom.R;
import com.example.aroom.databinding.SpecialRvItemBinding;
import com.example.core.util.PriceCalculator;
import com.example.model.Product;

import java.util.Objects;

public class SpecialProductsAdapter extends RecyclerView.Adapter<SpecialProductsAdapter.SpecialProductsViewHolder> {

    public interface OnProductClickListener {
        void onClick(Product product);
    }

    public OnProductClickListener onClick;
    public OnProductClickListener onBtnClick;

    public void setOnClick(OnProductClickListener onClick) {
        this.onClick = onClick;
    }

    public void setOnBtnClick(OnProductClickListener onBtnClick) {
        this.onBtnClick = onBtnClick;
    }

    private final DiffUtil.ItemCallback<Product> diffCallback = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.equals(newItem);
        }
    };

    public final AsyncListDiffer<Product> differ = new AsyncListDiffer<>(this, diffCallback);

    @NonNull
    @Override
    public SpecialProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SpecialProductsViewHolder(
                SpecialRvItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialProductsViewHolder holder, int position) {
        Product product = differ.getCurrentList().get(position);
        holder.bind(product);

        holder.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(product);
        });

        holder.binding.btnAddToCart.setOnClickListener(v -> {
            if (onBtnClick != null) onBtnClick.onClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public class SpecialProductsViewHolder extends RecyclerView.ViewHolder {
        final SpecialRvItemBinding binding;

        public SpecialProductsViewHolder(SpecialRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(itemView).load(product.getImages().get(0))
                        .placeholder(R.drawable.ic_cart)
                        .error(R.drawable.ic_cart)
                        .into(binding.imgSpecialRvItem);
            } else {
                binding.imgSpecialRvItem.setImageResource(R.drawable.ic_cart);
            }
            binding.tvSpecialProductName.setText(product.getName());

            float discountPercent = product.getOfferPercentage() != null ? product.getOfferPercentage() : 0.25f;
            float remainingPricePercentage = 1f - discountPercent;
            float priceAfterOffer = remainingPricePercentage * product.getPrice();
            binding.tvSpecialProductPrice.setText(PriceCalculator.formatPriceVnd(priceAfterOffer));

            binding.tvSpecialProductOldPrice.setPaintFlags(binding.tvSpecialProductOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.tvSpecialProductOldPrice.setText(PriceCalculator.formatPriceVnd(product.getPrice()));
            binding.tvSpecialProductOldPrice.setVisibility(View.VISIBLE);

            binding.tvSpecialSoldCount.setText("• Đã bán " + product.getSoldCount());
        }
    }
}
