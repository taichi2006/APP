package com.example.core.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.aroom.R;
import com.example.feature.main.ShoppingActivity;

/**
 * Quản lý thông báo cục bộ (Local Notifications) cho đơn hàng và hệ thống
 */
public class AppNotificationHelper {

    private static final String CHANNEL_ID = "aroom_orders_channel";

    private static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Thông báo đơn hàng TCFurnish",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Cập nhật trạng thái đặt hàng, giao hàng và hủy đơn hàng");
                channel.enableVibration(true);
                nm.createNotificationChannel(channel);
            }
        }
    }

    public static void showOrderNotification(
            Context context,
            String orderId,
            String totalFormatted,
            String paymentMethod
    ) {
        ensureChannel(context);
        int notificationId = (int) (System.currentTimeMillis() % 10000);

        Intent intent = new Intent(context, ShoppingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("🎉 Đặt hàng thành công #" + orderId)
                .setContentText("Đơn hàng " + totalFormatted + " đã được tiếp nhận.")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        "Cảm ơn bạn đã mua sắm tại TCFurnish!\n"
                                + "• Mã đơn: #" + orderId + "\n"
                                + "• Tổng thanh toán: " + totalFormatted + "\n"
                                + "• Phương thức: " + paymentMethod + "\n"
                                + "Nhân viên chăm sóc khách hàng sẽ liên hệ xác nhận trong ít phút."
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        }
    }

    public static void showCancelNotification(Context context, String orderId) {
        ensureChannel(context);
        int notificationId = (int) (System.currentTimeMillis() % 10000);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Đã hủy đơn hàng #" + orderId)
                .setContentText("Đơn hàng #" + orderId + " đã được hủy thành công theo yêu cầu.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        }
    }
}
