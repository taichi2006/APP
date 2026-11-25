package com.example.feature.order.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aroom.R;
import com.example.aroom.databinding.OrderItemBinding;
import com.example.core.util.PriceCalculator;
import com.example.model.CartProduct;
import com.example.model.Order;

import java.util.Objects;

public class AllOrdersAdapter extends RecyclerView.Adapter<AllOrdersAdapter.OrdersViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OnOrderClickListener onClick = null;
    public OnOrderClickListener onCancelClick = null;
    public OnOrderClickListener onRateClick = null;

    public void setOnClick(OnOrderClickListener listener) { this.onClick = listener; }
    public void setOnCancelClick(OnOrderClickListener listener) { this.onCancelClick = listener; }
    public void setOnRateClick(OnOrderClickListener listener) { this.onRateClick = listener; }

    public class OrdersViewHolder extends RecyclerView.ViewHolder {
        private final OrderItemBinding binding;

        public OrdersViewHolder(@NonNull OrderItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            if (order == null) return;

            binding.tvOrderId.setText("#" + order.getOrderCode());
            binding.tvOrderDate.setText(order.getDate());
            binding.tvOrderTotalPrice.setText(PriceCalculator.formatPriceVnd(order.getTotalPrice()));

            String method = order.getPaymentMethod() != null ? order.getPaymentMethod().toLowerCase() : "";
            binding.tvOrderPaymentMethod.setText(method.contains("vnpay") || method.contains("online") ? "VNPAY" : "COD");

            if (order.getProducts() != null && !order.getProducts().isEmpty()) {
                CartProduct first = order.getProducts().get(0);
                if (first.getProduct() != null) {
                    binding.tvOrderProductName.setText(first.getProduct().getName());
                    String img = (first.getProduct().getImages() != null && !first.getProduct().getImages().isEmpty())
                            ? first.getProduct().getImages().get(0) : "";
                    Glide.with(itemView).load(img).placeholder(R.drawable.ic_cart).into(binding.imageOrderThumb);
                }
                int count = order.getProducts().size();
                binding.tvOrderProductExtra.setText(count > 1 ? "+" + (count - 1) + " sản phẩm khác" : "Số lượng: " + first.getQuantity() + " món");
            }

            String st = order.getOrderStatus() != null ? order.getOrderStatus() : "Đã đặt";
            int textColor = Color.parseColor("#2563EB");
            int bgColor = Color.parseColor("#DBEAFE");
            boolean isCancelable = false;
            boolean isReceived = false;

            if (st.contains("hủy") || st.equalsIgnoreCase("Canceled")) {
                textColor = Color.parseColor("#DC2626");
                bgColor = Color.parseColor("#FEE2E2");
            } else if (st.contains("nhận") || st.contains("Delivered") || st.contains("giao hàng")) {
                textColor = Color.parseColor("#16A34A");
                bgColor = Color.parseColor("#DCFCE7");
                isReceived = true;
            } else if (st.contains("giao") || st.contains("Shipped")) {
                textColor = Color.parseColor("#D97706");
                bgColor = Color.parseColor("#FEF3C7");
            } else {
                isCancelable = true;
            }

            binding.tvOrderStatusBadge.setText(st);
            binding.tvOrderStatusBadge.setTextColor(textColor);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(20f);
            bg.setColor(bgColor);
            binding.tvOrderStatusBadge.setBackground(bg);

            binding.btnCancelOrderItem.setVisibility(isCancelable ? View.VISIBLE : View.GONE);
            binding.btnCancelOrderItem.setOnClickListener(v -> { if (onCancelClick != null) onCancelClick.onOrderClick(order); });

            binding.btnRateOrderItem.setVisibility(isReceived ? View.VISIBLE : View.GONE);
            if (isReceived) {
                if (order.isReviewed()) {
                    binding.btnRateOrderItem.setText("Đã đánh giá");
                    binding.btnRateOrderItem.setEnabled(false);
                    binding.btnRateOrderItem.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#64748B")));
                } else {
                    binding.btnRateOrderItem.setText("Đánh giá");
                    binding.btnRateOrderItem.setEnabled(true);
                    binding.btnRateOrderItem.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#B88A44")));
                }
            }
            binding.btnRateOrderItem.setOnClickListener(v -> {
                if (order.isReviewed()) {
                    Toast.makeText(v.getContext(), "Đơn hàng này đã được đánh giá rồi!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (onRateClick != null) onRateClick.onOrderClick(order);
            });

            binding.cardOrder.setOnClickListener(v -> { if (onClick != null) onClick.onOrderClick(order); });
            binding.btnViewOrderDetail.setOnClickListener(v -> { if (onClick != null) onClick.onOrderClick(order); });
        }
    }

    private final DiffUtil.ItemCallback<Order> diffCallback = new DiffUtil.ItemCallback<Order>() {
        @Override
        public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            return oldItem.getOrderId() == newItem.getOrderId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            return oldItem.getOrderId() == newItem.getOrderId()
                    && Objects.equals(oldItem.getOrderStatus(), newItem.getOrderStatus())
                    && oldItem.isReviewed() == newItem.isReviewed()
                    && oldItem.getTotalPrice() == newItem.getTotalPrice();
        }
    };

    public final AsyncListDiffer<Order> differ = new AsyncListDiffer<>(this, diffCallback);

    @NonNull
    @Override
    public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrdersViewHolder(OrderItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersViewHolder holder, int position) {
        holder.bind(differ.getCurrentList().get(position));
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }
}
