package com.example.feature.order.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.aroom.databinding.DialogAddReviewBinding;
import com.example.core.util.SoundHelper;
import com.example.model.Order;

public class AddReviewDialog {

    public interface OnReviewSubmitListener {
        void onSubmit(float rating, String comment);
    }

    public static void show(Context context, Order order, OnReviewSubmitListener listener) {
        if (context == null) return;
        if (order != null && order.isReviewed()) {
            Toast.makeText(context, "Đơn hàng này đã được đánh giá rồi!", Toast.LENGTH_SHORT).show();
            return;
        }

        SoundHelper.playClickSound();
        Dialog dialog = new Dialog(context);
        DialogAddReviewBinding binding = DialogAddReviewBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (order != null) {
            binding.tvDialogTitle.setText("Đánh giá đơn hàng #" + order.getOrderCode());
            binding.tvDialogOrderInfo.setText("Đơn hàng #" + order.getOrderCode() + " • Đã nhận hàng thành công");
        }

        binding.ratingBarInput.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            int stars = Math.round(rating);
            switch (stars) {
                case 1:
                    binding.tvRatingDescription.setText("Rất không hài lòng");
                    break;
                case 2:
                    binding.tvRatingDescription.setText("Chưa hài lòng");
                    break;
                case 3:
                    binding.tvRatingDescription.setText("Bình thường - Tạm ổn");
                    break;
                case 4:
                    binding.tvRatingDescription.setText("Hài lòng - Chất lượng tốt");
                    break;
                case 5:
                default:
                    binding.tvRatingDescription.setText("Tuyệt vời - Rất hài lòng");
                    break;
            }
        });

        binding.btnCancelReview.setOnClickListener(v -> {
            SoundHelper.playClickSound();
            dialog.dismiss();
        });

        binding.btnSubmitReview.setOnClickListener(v -> {
            SoundHelper.playSuccessSound();
            float rating = binding.ratingBarInput.getRating();
            if (rating <= 0f) rating = 5.0f;

            String comment = binding.edComment.getText() != null
                    ? binding.edComment.getText().toString().trim() : "";
            if (comment.isEmpty()) {
                comment = "Sản phẩm đẹp đúng mô tả, chất lượng hoàn thiện rất tốt và đóng gói cẩn thận!";
            }

            dialog.dismiss();
            if (listener != null) {
                listener.onSubmit(rating, comment);
            }
        });

        dialog.show();
    }
}
