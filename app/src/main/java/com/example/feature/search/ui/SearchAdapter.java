package com.example.feature.search.ui;

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
import com.example.aroom.databinding.ProductRvItemBinding;
import com.example.core.util.PriceCalculator;
import com.example.model.Product;

import java.util.Locale;
import java.util.Objects;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    public interface OnProductClickListener {
        void onClick(Product product);
    }

    private OnProductClickListener onClick;

    public void setOnClick(OnProductClickListener onClick) {
        this.onClick = onClick;
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
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchViewHolder(
                ProductRvItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Product product = differ.getCurrentList().get(position);
        holder.bind(product);

        holder.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        final ProductRvItemBinding binding;

        public SearchViewHolder(ProductRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(itemView).load(product.getImages().get(0))
                        .placeholder(R.drawable.ic_cart)
                        .error(R.drawable.ic_cart)
                        .into(binding.imgProduct);
            } else {
                binding.imgProduct.setImageResource(R.drawable.ic_cart);
            }
            binding.tvName.setText(product.getName());

            float ratingVal = product.getRating() > 0 ? product.getRating() : 4.9f;
            binding.tvProductRating.setText(String.format(Locale.US, "★ %.1f", ratingVal));
            binding.tvSoldCount.setText("Đã bán " + product.getSoldCount());

            Float offer = product.getOfferPercentage();
            if (offer != null && offer > 0) {
                int percentInt = (int) (offer * 100);
                binding.tvDiscountBadge.setText("-" + percentInt + "%");
                binding.tvDiscountBadge.setVisibility(View.VISIBLE);

                float priceAfterOffer = PriceCalculator.getProductPrice(offer, product.getPrice());
                binding.tvPrice.setText(PriceCalculator.formatPriceVnd(priceAfterOffer));
                binding.tvPrice.setPaintFlags(0);

                binding.tvNewPrice.setText(PriceCalculator.formatPriceVnd(product.getPrice()));
                binding.tvNewPrice.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvNewPrice.setVisibility(View.VISIBLE);
            } else {
                binding.tvDiscountBadge.setVisibility(View.GONE);
                binding.tvPrice.setText(PriceCalculator.formatPriceVnd(product.getPrice()));
                binding.tvPrice.setPaintFlags(0);
                binding.tvNewPrice.setVisibility(View.GONE);
            }
        }
    }
}
