package com.example.todaytoeat.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.todaytoeat.fragment.MainFragment;
import com.example.todaytoeat.fragment.SettingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private static final int PAGE_COUNT = 2;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new SettingFragment();
        }
        return new MainFragment();
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}