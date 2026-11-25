package com.example.feature.product.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroom.databinding.ReviewItemBinding;
import com.example.model.Review;

import java.util.Locale;
import java.util.Objects;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ReviewItemBinding binding;

        public ReviewViewHolder(@NonNull ReviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Review review) {
            String name = (review.getUserName() != null && !review.getUserName().trim().isEmpty())
                    ? review.getUserName() : "Khách hàng";
            binding.tvReviewerName.setText(name);

            String date = (review.getDate() != null && !review.getDate().trim().isEmpty())
                    ? review.getDate() : "Gần đây";
            binding.tvReviewDate.setText(date);

            float rating = review.getRating() > 0 ? review.getRating() : 5.0f;
            binding.ratingBarItem.setRating(rating);
            binding.tvRatingScore.setText(String.format(Locale.US, "%.1f", rating));
            binding.tvReviewComment.setText(review.getComment() != null ? review.getComment() : "");
        }
    }

    private final DiffUtil.ItemCallback<Review> diffCallback = new DiffUtil.ItemCallback<Review>() {
        @Override
        public boolean areItemsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId())
                    && Float.compare(oldItem.getRating(), newItem.getRating()) == 0
                    && Objects.equals(oldItem.getComment(), newItem.getComment());
        }
    };

    public final AsyncListDiffer<Review> differ = new AsyncListDiffer<>(this, diffCallback);

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ReviewItemBinding binding = ReviewItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(differ.getCurrentList().get(position));
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }
}
