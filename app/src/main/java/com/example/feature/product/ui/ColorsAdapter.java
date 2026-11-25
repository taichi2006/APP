package com.example.feature.product.ui;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroom.databinding.ColorRvItemBinding;

public class ColorsAdapter extends RecyclerView.Adapter<ColorsAdapter.ColorsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int color);
    }

    private int selectedPosition = -1;
    public OnItemClickListener onItemClick = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClick = listener;
    }

    public class ColorsViewHolder extends RecyclerView.ViewHolder {
        private final ColorRvItemBinding binding;

        public ColorsViewHolder(@NonNull ColorRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int color, int position) {
            ColorDrawable imageDrawable = new ColorDrawable(color);
            binding.imgColor.setImageDrawable(imageDrawable);

            if (position == selectedPosition) {
                binding.imgShadow.setVisibility(View.VISIBLE);
                binding.imgPicked.setVisibility(View.VISIBLE);
            } else {
                binding.imgShadow.setVisibility(View.INVISIBLE);
                binding.imgPicked.setVisibility(View.INVISIBLE);
            }
        }
    }

    private final DiffUtil.ItemCallback<Integer> diffCallback = new DiffUtil.ItemCallback<Integer>() {
        @Override
        public boolean areItemsTheSame(@NonNull Integer oldItem, @NonNull Integer newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Integer oldItem, @NonNull Integer newItem) {
            return oldItem.equals(newItem);
        }
    };

    public final AsyncListDiffer<Integer> differ = new AsyncListDiffer<>(this, diffCallback);

    @NonNull
    @Override
    public ColorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ColorRvItemBinding binding = ColorRvItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ColorsViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @Override
    public void onBindViewHolder(@NonNull ColorsViewHolder holder, int position) {
        int color = differ.getCurrentList().get(position);
        holder.bind(color, position);

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition >= 0) {
                notifyItemChanged(selectedPosition);
            }
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(selectedPosition);
            if (onItemClick != null) {
                onItemClick.onItemClick(color);
            }
        });
    }
}
