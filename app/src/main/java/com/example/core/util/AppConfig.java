package com.example.core.util;

import com.example.aroom.BuildConfig;

/**
 * Quản lý các cấu hình môi trường, cơ sở dữ liệu và token bảo mật được nạp an toàn từ file .env.
 */
public final class AppConfig {
    private AppConfig() {}

    public static final String FIREBASE_PROJECT_ID = BuildConfig.FIREBASE_PROJECT_ID;
    public static final String FIREBASE_DB_URL = BuildConfig.FIREBASE_DB_URL;
    public static final String FIREBASE_STORAGE_BUCKET = BuildConfig.FIREBASE_STORAGE_BUCKET;
    public static final String FIREBASE_API_KEY = BuildConfig.FIREBASE_API_KEY;

    public static final String SANDBOX_TOKEN = BuildConfig.SANDBOX_TOKEN;
    public static final String VNP_TMN_CODE = BuildConfig.VNP_TMN_CODE;
    public static final String VNP_HASH_SECRET = BuildConfig.VNP_HASH_SECRET;
    public static final String VNP_URL = BuildConfig.VNP_URL;
    public static final String VNP_RETURN_URL = BuildConfig.VNP_RETURN_URL;
}
