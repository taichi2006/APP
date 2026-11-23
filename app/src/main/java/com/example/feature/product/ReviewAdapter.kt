package com.example.feature.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.aroom.databinding.ReviewItemBinding
import com.example.data.Review

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(val binding: ReviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.apply {
                tvReviewerName.text = if (review.userName.isNullOrBlank()) "Khách hàng" else review.userName
                tvReviewDate.text = if (review.date.isNullOrBlank()) "Gần đây" else review.date
                ratingBarItem.rating = if (review.rating > 0) review.rating else 5f
                tvRatingScore.text = String.format("%.1f", ratingBarItem.rating)
                tvReviewComment.text = review.comment
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.rating == newItem.rating &&
                    oldItem.comment == newItem.comment
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        return ReviewViewHolder(
            ReviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = differ.currentList[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int = differ.currentList.size
}
