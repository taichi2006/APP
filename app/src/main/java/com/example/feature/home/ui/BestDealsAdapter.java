package com.example.feature.home.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aroom.R;
import com.example.aroom.databinding.BestDealsRvItemBinding;
import com.example.core.util.PriceCalculator;
import com.example.model.Product;

import java.util.Objects;

public class BestDealsAdapter extends RecyclerView.Adapter<BestDealsAdapter.BestDealsViewHolder> {

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
    public BestDealsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BestDealsViewHolder(
                BestDealsRvItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull BestDealsViewHolder holder, int position) {
        Product product = differ.getCurrentList().get(position);
        holder.bind(product);

        holder.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(product);
        });

        holder.binding.btnSeeProduct.setOnClickListener(v -> {
            if (onBtnClick != null) onBtnClick.onClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public static class BestDealsViewHolder extends RecyclerView.ViewHolder {
        final BestDealsRvItemBinding binding;

        public BestDealsViewHolder(BestDealsRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(itemView).load(product.getImages().get(0))
                        .placeholder(R.drawable.ic_cart)
                        .error(R.drawable.ic_cart)
                        .into(binding.imgBestDeal);
            } else {
                binding.imgBestDeal.setImageResource(R.drawable.ic_cart);
            }

            if (product.getOfferPercentage() != null) {
                float remainingPricePercentage = 1f - product.getOfferPercentage();
                float priceAfterOffer = remainingPricePercentage * product.getPrice();
                binding.tvPrice.setText(PriceCalculator.formatPriceVnd(priceAfterOffer));
            } else {
                binding.tvPrice.setText(PriceCalculator.formatPriceVnd(product.getPrice()));
            }

            binding.tvBestDealsOldPrice.setText(PriceCalculator.formatPriceVnd(product.getPrice()));
            binding.tvBestDealProductName.setText(product.getName());
        }
    }
}
