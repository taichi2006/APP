package com.example.feature.auth.ui;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ResetPasswordDialog {

    public interface OnResetListener {
        void onReset(String email);
    }

    public static void show(Fragment fragment, OnResetListener listener) {
        Context context = fragment.requireContext();
        final EditText input = new EditText(context);
        input.setHint("Nhập email đã đăng ký");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Đặt lại mật khẩu")
                .setMessage("Chúng tôi sẽ gửi liên kết để đặt lại mật khẩu đến email của bạn:")
                .setView(input)
                .setPositiveButton("Gửi", null)
                .setNegativeButton("Hủy", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String email = input.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (listener != null) listener.onReset(email);
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
