package com.example.feature.home.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class HomeViewPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragments;

    public HomeViewPagerAdapter(List<Fragment> fragments, @NonNull FragmentManager fm, @NonNull Lifecycle lifecycle) {
        super(fm, lifecycle);
        this.fragments = fragments;
    }

    @Override
    public int getItemCount() {
        return fragments != null ? fragments.size() : 0;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }
}
