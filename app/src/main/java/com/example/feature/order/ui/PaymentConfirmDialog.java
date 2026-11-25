package com.example.feature.order.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.aroom.R;
import com.example.core.util.PriceCalculator;
import com.example.model.Address;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PaymentConfirmDialog {

    public interface OnPaymentConfirmListener {
        void onConfirmed(String paymentMethod, String paymentStatus);
    }

    public static void show(Context context, float totalPrice, Address address, OnPaymentConfirmListener listener) {
        if (context == null) return;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Xác nhận thanh toán");
        builder.setMessage("Tổng tiền: " + PriceCalculator.formatPriceVnd(totalPrice)
                + "\nĐịa chỉ: " + (address != null ? address.getStreet() + ", " + address.getCity() : "Mặc định")
                + "\n\nChọn phương thức thanh toán:");

        String[] methods = new String[]{"Tiền mặt khi nhận hàng (COD)", "Thanh toán trực tuyến (VNPAY)"};
        final int[] selectedIndex = {0};

        builder.setSingleChoiceItems(methods, 0, (dialog, which) -> selectedIndex[0] = which);

        builder.setPositiveButton("Xác nhận đặt hàng", (dialog, which) -> {
            dialog.dismiss();
            if (listener != null) {
                if (selectedIndex[0] == 1) {
                    listener.onConfirmed("VNPAY", "Đã thanh toán");
                } else {
                    listener.onConfirmed("COD", "Chờ thu tiền khi giao");
                }
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
