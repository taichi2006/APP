package com.example.core.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.SoundEffectConstants;
import android.view.View;

/**
 * Tiện ích phát âm thanh hiệu ứng (Click, Thành công)
 */
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

    public static void showOrderNotification(Context context, String orderId, String totalFormatted, String paymentMethod) {
        AppNotificationHelper.showOrderNotification(context, orderId, totalFormatted, paymentMethod);
    }

    public static void showCancelNotification(Context context, String orderId) {
        AppNotificationHelper.showCancelNotification(context, orderId);
    }
}
