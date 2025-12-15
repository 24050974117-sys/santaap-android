package com.example.foodapp.Activity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityMainBinding;

// Perhatikan: Tidak perlu import Fragment secara spesifik karena mereka ada di package yang sama

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Tampilkan halaman Home sebagai halaman default
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
            } else if (itemId == R.id.nav_favorite) {
                replaceFragment(new FavoriteFragment());
            } else if (itemId == R.id.nav_orders) {
                replaceFragment(new OrdersFragment());
            } else if (itemId == R.id.nav_support) {
                replaceFragment(new SupportFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}

