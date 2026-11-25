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
import com.example.aroom.databinding.OrderDetailProductItemBinding;
import com.example.core.util.PriceCalculator;
import com.example.model.CartProduct;

import java.util.Objects;

public class OrderDetailProductAdapter extends RecyclerView.Adapter<OrderDetailProductAdapter.OrderDetailViewHolder> {

    public class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        public final OrderDetailProductItemBinding binding;

        public OrderDetailViewHolder(@NonNull OrderDetailProductItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartProduct cartProduct) {
            if (cartProduct == null || cartProduct.getProduct() == null) return;

            binding.tvProductName.setText(cartProduct.getProduct().getName());

            String firstImg = "";
            if (cartProduct.getProduct().getImages() != null && !cartProduct.getProduct().getImages().isEmpty()) {
                firstImg = cartProduct.getProduct().getImages().get(0);
            }

            Glide.with(itemView)
                .load(firstImg)
                .placeholder(R.drawable.ic_cart)
                .into(binding.imgProduct);

            float unitPrice;
            if (cartProduct.getProduct().getOfferPercentage() != null) {
                unitPrice = PriceCalculator.getProductPrice(cartProduct.getProduct().getOfferPercentage(), cartProduct.getProduct().getPrice());
            } else {
                unitPrice = cartProduct.getProduct().getPrice();
            }
            binding.tvProductPrice.setText(PriceCalculator.formatPriceVnd(unitPrice));
            binding.tvProductQuantity.setText("x" + cartProduct.getQuantity());

            Integer colorVal = cartProduct.getSelectedColor();
            boolean hasColor = colorVal != null && colorVal != 0;
            if (hasColor) {
                binding.imgProductColor.setVisibility(View.VISIBLE);
                binding.tvProductColorLabel.setVisibility(View.VISIBLE);
                binding.imgProductColor.setImageDrawable(new ColorDrawable(colorVal));
            } else {
                binding.imgProductColor.setVisibility(View.GONE);
                binding.tvProductColorLabel.setVisibility(View.GONE);
            }

            String sizeText = cartProduct.getSelectedSize();
            boolean hasSize = sizeText != null && !sizeText.trim().isEmpty();
            if (hasSize) {
                binding.tvProductSize.setVisibility(View.VISIBLE);
                binding.tvProductSize.setText("Kích thước: " + sizeText);
            } else {
                binding.tvProductSize.setVisibility(View.GONE);
            }
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
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderDetailViewHolder(OrderDetailProductItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        holder.bind(differ.getCurrentList().get(position));
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }
}
