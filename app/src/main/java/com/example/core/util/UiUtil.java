package com.example.core.util;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroom.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Tiện ích giao diện người dùng tập trung (gộp dpToPx, ẩn/hiện BottomNav, khoảng cách RecyclerView)
 */
public class UiUtil {

    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                Resources.getSystem().getDisplayMetrics()
        );
    }

    public static void hideBottomNavigationView(Fragment fragment) {
        if (fragment != null && fragment.getActivity() != null) {
            Activity activity = fragment.getActivity();
            BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.GONE);
            }
        }
    }

    public static void showBottomNavigationView(Fragment fragment) {
        if (fragment != null && fragment.getActivity() != null) {
            Activity activity = fragment.getActivity();
            BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        }
    }

    public static class HorizontalItemDecoration extends RecyclerView.ItemDecoration {
        private final int amount;

        public HorizontalItemDecoration(int amount) {
            this.amount = amount;
        }

        public HorizontalItemDecoration() {
            this(15);
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.right = amount;
        }
    }

    public static class VerticalItemDecoration extends RecyclerView.ItemDecoration {
        private final int amount;

        public VerticalItemDecoration(int amount) {
            this.amount = amount;
        }

        public VerticalItemDecoration() {
            this(30);
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.bottom = amount;
        }
    }
}
