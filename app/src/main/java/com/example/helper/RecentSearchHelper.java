package com.example.helper;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecentSearchHelper {

    private static final String PREF_NAME = "aroom_recent_searches";
    private static final String KEY_SEARCHES = "searches_list";
    private static final int MAX_ITEMS = 8;

    public static void saveSearchQuery(Context context, String query) {
        if (context == null || query == null || query.trim().isEmpty()) {
            return;
        }
        String cleanQuery = query.trim();
        List<String> current = getRecentSearches(context);
        current.remove(cleanQuery);
        current.add(0, cleanQuery);
        if (current.size() > MAX_ITEMS) {
            current = current.subList(0, MAX_ITEMS);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < current.size(); i++) {
            sb.append(current.get(i));
            if (i < current.size() - 1) {
                sb.append(";;");
            }
        }

        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_SEARCHES, sb.toString()).apply();
    }

    public static List<String> getRecentSearches(Context context) {
        if (context == null) {
            return new ArrayList<>();
        }
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_SEARCHES, "");
        if (raw == null || raw.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = raw.split(";;");
        return new ArrayList<>(Arrays.asList(parts));
    }

    public static void clearRecentSearches(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_SEARCHES).apply();
    }
}
