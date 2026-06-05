package com.example.todaytoeat;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.todaytoeat.fragment.MainFragment;
import com.example.todaytoeat.fragment.SettingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// 没事做一个手机版的今天吃什么，核心代码没怎么动，算是学以致用
public class MainActivity extends AppCompatActivity {
    private final MainFragment mainFragment = new MainFragment();
    private final SettingFragment settingFragment = new SettingFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

         if (savedInstanceState == null) {
             FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
             transaction.add(R.id.fl_main_content, mainFragment);
             transaction.commit();
         }

         BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
         bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
             @Override
             public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                 if (menuItem.getItemId() == R.id.nav_home){
                     switchFragment(mainFragment);
                 }else if (menuItem.getItemId() == R.id.nav_settings){
                     switchFragment(settingFragment);
                 }

                 return true;
             }
         });

         ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
             Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
             v.setPadding(0, 0, 0, systemBars.bottom);
             return insets;
         });

    }

    public void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_main_content, fragment);
        transaction.commit();
    }
}
