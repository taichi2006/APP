package com.example.core.util;

import java.text.DecimalFormat;

public class PriceCalculator {

    public static float getProductPrice(Float offerPercentage, float price) {
        if (offerPercentage == null) {
            return price;
        }
        float remainingPricePercentage = 1.0f - offerPercentage;
        return remainingPricePercentage * price;
    }

    public static String formatPriceVnd(float price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format((long) price) + " đ";
    }

    public static String formatPriceVnd(Float price) {
        if (price == null) {
            return "0 đ";
        }
        return formatPriceVnd(price.floatValue());
    }

    public static String formatPriceVnd(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format((long) price) + " đ";
    }

    public static String formatPriceVnd(long price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price) + " đ";
    }

    public static String formatPriceVnd(int price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format((long) price) + " đ";
    }
}
