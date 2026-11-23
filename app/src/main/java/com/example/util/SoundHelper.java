package com.example.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.view.SoundEffectConstants;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.aroom.R;
import com.example.feature.main.ShoppingActivity;

public class SoundHelper {

    private static ToneGenerator toneGenerator;

    static {
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playClickSound() {
        playClickSound(null);
    }

    public static void playClickSound(View view) {
        try {
            if (view != null) {
                view.playSoundEffect(SoundEffectConstants.CLICK);
            }
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 25);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playSuccessSound() {
        try {
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showOrderNotification(
            Context context,
            String orderId,
            String totalFormatted,
            String paymentMethod
    ) {
        String channelId = "aroom_orders_channel";
        int notificationId = (int) (System.currentTimeMillis() % 10000);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Thông báo đơn hàng TCFurnish",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Cập nhật trạng thái và xác nhận đặt hàng thành công");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, ShoppingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_truck_delivery)
                .setContentTitle("🎉 Đặt hàng TCFurnish thành công!")
                .setContentText("Đơn hàng #" + orderId + " (" + totalFormatted + ") đã được gửi đến hệ thống.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Đơn hàng #" + orderId + " trị giá " + totalFormatted + " thanh toán qua " + paymentMethod + " đã được ghi nhận thành công. TCFurnish sẽ liên hệ và giao hàng tới bạn trong thời gian sớm nhất!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            try {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build());
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public static void showCancelNotification(Context context, String orderId) {
        String channelId = "aroom_orders_channel";
        int notificationId = (int) (System.currentTimeMillis() % 10000);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Thông báo đơn hàng TCFurnish",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Cập nhật trạng thái và xác nhận đặt hàng thành công");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_receipt_long)
                .setContentTitle("Đơn hàng #" + orderId + " đã được hủy")
                .setContentText("Đơn hàng #" + orderId + " đã được hủy theo yêu cầu của bạn.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            try {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build());
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}
