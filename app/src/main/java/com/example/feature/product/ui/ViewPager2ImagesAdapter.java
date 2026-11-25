package com.example.feature.product.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aroom.databinding.ViewpagerImageItemBinding;

public class ViewPager2ImagesAdapter extends RecyclerView.Adapter<ViewPager2ImagesAdapter.ViewPager2ImagesViewHolder> {

    public class ViewPager2ImagesViewHolder extends RecyclerView.ViewHolder {
        private final ViewpagerImageItemBinding binding;

        public ViewPager2ImagesViewHolder(@NonNull ViewpagerImageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String imagePath) {
            Glide.with(itemView).load(imagePath).into(binding.imgProductDetails);
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
    public ViewPager2ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewpagerImageItemBinding binding = ViewpagerImageItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewPager2ImagesViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPager2ImagesViewHolder holder, int position) {
        String image = differ.getCurrentList().get(position);
        holder.bind(image);
    }
}
