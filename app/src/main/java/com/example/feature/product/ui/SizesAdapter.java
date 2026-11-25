package com.example.feature.product.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroom.databinding.SizeRvItemBinding;

public class SizesAdapter extends RecyclerView.Adapter<SizesAdapter.SizesViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String size);
    }

    private int selectedPosition = -1;
    public OnItemClickListener onItemClick = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClick = listener;
    }

    public class SizesViewHolder extends RecyclerView.ViewHolder {
        private final SizeRvItemBinding binding;

        public SizesViewHolder(@NonNull SizeRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String size, int position) {
            binding.tvSize.setText(size);

            if (position == selectedPosition) {
                binding.imgShadow.setVisibility(View.VISIBLE);
                binding.cardSize.setCardBackgroundColor(Color.parseColor("#684128"));
                binding.cardSize.setStrokeColor(Color.parseColor("#B88A44"));
                binding.tvSize.setTextColor(Color.WHITE);
            } else {
                binding.imgShadow.setVisibility(View.INVISIBLE);
                binding.cardSize.setCardBackgroundColor(Color.WHITE);
                binding.cardSize.setStrokeColor(Color.parseColor("#E8DFD3"));
                binding.tvSize.setTextColor(Color.parseColor("#334155"));
            }
        }
    }

    private final DiffUtil.ItemCallback<String> diffCallback = new DiffUtil.ItemCallback<String>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }
    };

    public final AsyncListDiffer<String> differ = new AsyncListDiffer<>(this, diffCallback);

    @NonNull
    @Override
    public SizesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SizeRvItemBinding binding = SizeRvItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new SizesViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @Override
    public void onBindViewHolder(@NonNull SizesViewHolder holder, int position) {
        String size = differ.getCurrentList().get(position);
        holder.bind(size, position);

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition >= 0) {
                notifyItemChanged(selectedPosition);
            }
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(selectedPosition);
            if (onItemClick != null) {
                onItemClick.onItemClick(size);
            }
        });
    }
}
