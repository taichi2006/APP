package com.example.core.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VNPayHelper {

    public static final String DEFAULT_TMN_CODE = "YB3TI7G3";
    public static final String DEFAULT_HASH_SECRET = "TCJWCJVZLOQWTHSTCQNPSXWWFOLYSASF";
    public static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_RETURN_URL = "https://sandbox.vnpayment.vn/merchantv2/";

    private static final String PREF_NAME = "vnpay_config";
    private static final String KEY_TMN_CODE = "tmn_code";
    private static final String KEY_HASH_SECRET = "hash_secret";

    public static String getTmnCode(Context context) {
        if (context == null) return DEFAULT_TMN_CODE;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_TMN_CODE, DEFAULT_TMN_CODE);
    }

    public static String getHashSecret(Context context) {
        if (context == null) return DEFAULT_HASH_SECRET;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_HASH_SECRET, DEFAULT_HASH_SECRET);
    }

    public static void saveConfig(Context context, String tmnCode, String hashSecret) {
        if (context == null) return;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_TMN_CODE, (tmnCode != null && !tmnCode.trim().isEmpty()) ? tmnCode.trim() : DEFAULT_TMN_CODE)
                .putString(KEY_HASH_SECRET, (hashSecret != null && !hashSecret.trim().isEmpty()) ? hashSecret.trim() : DEFAULT_HASH_SECRET)
                .apply();
    }

    public static String createPaymentUrl(Context context, long orderId, float amount, String orderInfo) {
        String tmnCode = getTmnCode(context);
        String secretKey = getHashSecret(context);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        String createDate = sdf.format(new Date());

        long vnpAmount = (long) (amount * 100);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", String.valueOf(orderId));
        vnp_Params.put("vnp_OrderInfo", (orderInfo != null && !orderInfo.isEmpty()) ? orderInfo : ("Thanh toan don hang #" + orderId));
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNP_RETURN_URL);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", createDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(secretKey, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            return VNP_URL + "?" + queryUrl;
        } catch (Exception e) {
            return VNP_URL;
        }
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) return "";
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
