package com.example.core.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.aroom.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecentSearchHelper {

    public interface OnSearchClickListener {
        void onSearchClick(String query);
    }

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

    public static void populateChips(Context context, LinearLayout container, List<String> list, OnSearchClickListener listener) {
        if (context == null || container == null || list == null) return;
        container.removeAllViews();

        float density = context.getResources().getDisplayMetrics().density;
        int heightPx = (int) (32 * density);
        int marginEndPx = (int) (8 * density);
        int paddingStartEndPx = (int) (12 * density);

        for (String query : list) {
            TextView chip = new TextView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, heightPx);
            lp.setMargins(0, 0, marginEndPx, 0);
            chip.setLayoutParams(lp);

            chip.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_rounded_badge));
            chip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.brand_surface)));
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(paddingStartEndPx, 0, paddingStartEndPx, 0);
            chip.setText(query);
            chip.setTypeface(ResourcesCompat.getFont(context, R.font.be_vietnam_pro));
            chip.setTextColor(ContextCompat.getColor(context, R.color.brand_text_dark));
            chip.setTextSize(12f);
            chip.setClickable(true);
            chip.setFocusable(true);

            chip.setOnClickListener(v -> {
                saveSearchQuery(context, query);
                if (listener != null) listener.onSearchClick(query);
            });

            container.addView(chip);
        }
    }
}
