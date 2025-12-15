package com.example.foodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy; // Import untuk perbaikan cache
import com.example.foodapp.Adapter.BestFoodsAdapter;
import com.example.foodapp.Adapter.CategoryAdapter;
import com.example.foodapp.Adapter.PromoSliderAdapter;
import com.example.foodapp.Domain.Category;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.Domain.FilterCategory; // Import untuk filter baru
import com.example.foodapp.Domain.Price;
import com.example.foodapp.Domain.Promo;
import com.example.foodapp.R;
import com.example.foodapp.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File; // Import untuk foto lokal
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private PromoSliderAdapter sliderAdapter;
    private ArrayList<Promo> promoList;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    private DatabaseReference userRef;
    private ValueEventListener userListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Memanggil semua fungsi inisialisasi
        loadUserNameAndPhoto(); // Memuat nama & foto
        initFilterCategory(); // Filter baru (Kategori)
        initPrice();          // Filter baru (Harga)
        initPromoSlider();    // Slider promo
        initBestFood();       // Makanan terbaik
        initCategory();       // Kategori (ikon)
        setVariable();        // Listener tombol
    }

    // --- FUNGSI UNTUK MEMUAT NAMA DAN FOTO PENGGUNA ---
    private void loadUserNameAndPhoto() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && binding != null) {
            String uid = currentUser.getUid();
            userRef = database.getReference("Users").child(uid);

            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Cek null safety
                    if (snapshot.exists() && binding != null && getContext() != null) {

                        // 1. Muat Nama
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            binding.textView10.setText(name);
                        } else {
                            binding.textView10.setText("Pengguna");
                        }

                        // 2. Muat Foto Profil (CARA LOKAL)
                        String profileImageName = snapshot.child("profileImageUrl").getValue(String.class);
                        if (profileImageName != null && !profileImageName.isEmpty()) {
                            File imageFile = new File(getContext().getFilesDir(), profileImageName);

                            if (imageFile.exists()) {
                                // PERBAIKAN CACHE: Paksa Glide memuat file baru
                                Glide.with(getContext())
                                        .load(imageFile)
                                        .placeholder(R.drawable.sample_avatar)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Jangan simpan di disk cache
                                        .skipMemoryCache(true) // Jangan simpan di memori cache
                                        .into(binding.profileImageHome);
                            } else {
                                binding.profileImageHome.setImageResource(R.drawable.sample_avatar);
                            }
                        } else {
                            binding.profileImageHome.setImageResource(R.drawable.sample_avatar);
                        }

                    } else if (binding != null) {
                        binding.textView10.setText("Pengguna");
                        binding.profileImageHome.setImageResource(R.drawable.sample_avatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (binding != null) {
                        binding.textView10.setText("Pengguna");
                        binding.profileImageHome.setImageResource(R.drawable.sample_avatar);
                    }
                }
            };
            userRef.addValueEventListener(userListener);

        } else if (binding != null) {
            binding.textView10.setText("Tamu");
            binding.profileImageHome.setImageResource(R.drawable.sample_avatar);
        }
    }
    // ---------------------------------------------

    private void initPromoSlider() {
        promoList = new ArrayList<>();

        int bgColor = R.color.white;
        int titleColor = R.color.red;
        int subtitleColor = R.color.red;
        int btnColor = R.color.red;
        int btnTextColor = R.color.white;

        // Definisi Promo
        promoList.add(new Promo("OKTOBER MURAH", "Gratis ongkir, diskon spesial...", "pana",
                bgColor, titleColor, subtitleColor, btnColor, btnTextColor, "VIEW_ALL", 0));
        promoList.add(new Promo("Diskon Pizza 50%", "Khusus pembelian akhir pekan.", "food_1",
                bgColor, titleColor, subtitleColor, btnColor, btnTextColor, "food_1", 1));
        promoList.add(new Promo("Gratis Minuman", "Setiap pembelian 2 Burger.", "food_19",
                bgColor, titleColor, subtitleColor, btnColor, btnTextColor, "food_7", 2));

        // --- PERBAIKAN CRASH DI SINI ---
        // sliderAdapter diinisialisasi SEBELUM digunakan
        sliderAdapter = new PromoSliderAdapter(promoList);
        binding.promoViewPager.setAdapter(sliderAdapter);

        View firstChild = binding.promoViewPager.getChildAt(0);
        if (firstChild instanceof RecyclerView) {
            firstChild.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        }

        // Kode ini sekarang aman karena sliderAdapter sudah ada
        binding.promoTabLayout.removeAllTabs();
        for (int i = 0; i < sliderAdapter.getRealCount(); i++) { // Ini baris 174 Anda
            binding.promoTabLayout.addTab(binding.promoTabLayout.newTab());
        }

        binding.promoViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int realPosition = position % sliderAdapter.getRealCount();
                if (binding != null && binding.promoTabLayout.getTabAt(realPosition) != null) {
                    binding.promoTabLayout.getTabAt(realPosition).select();
                }
                startSliderTimer();
            }
        });

        binding.promoViewPager.setCurrentItem(Integer.MAX_VALUE / 2, false);
    }

    private void startSliderTimer() {
        stopSliderTimer();
        sliderRunnable = () -> {
            if (binding != null) {
                int currentItem = binding.promoViewPager.getCurrentItem();
                binding.promoViewPager.setCurrentItem(currentItem + 1, true);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private void stopSliderTimer() {
        if (sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    private void setVariable() {
        if (getContext() == null || binding == null) return;

        binding.logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        binding.searchBtn.setOnClickListener(v -> performSearch());
        binding.searchEdt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        binding.cartBtn.setOnClickListener(v ->
                startActivity(new Intent(getContext(), CartActivity.class))
        );

        // --- LOGIKA TOMBOL FILTER DIPERBARUI ---
        binding.filterBtn.setOnClickListener(v -> {
            if (binding.categoryFilterSp.getSelectedItem() == null || binding.priceSp.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Memuat filter, harap tunggu...", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(getContext(), ListFoodsActivity2.class);
            FilterCategory selectedCategory = (FilterCategory) binding.categoryFilterSp.getSelectedItem();
            Price selectedPrice = (Price) binding.priceSp.getSelectedItem();

            intent.putExtra("filterCategoryId", selectedCategory.getId());
            intent.putExtra("priceId", selectedPrice.getId());
            intent.putExtra("isFilter", true);
            startActivity(intent);
        });

        binding.viewAllTxt.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ListFoodsActivity2.class);
            intent.putExtra("isViewAll", true);
            startActivity(intent);
        });
    }

    private void performSearch() {
        if (binding == null || getContext() == null) return;
        String text = binding.searchEdt.getText().toString();
        Intent intent = new Intent(getContext(), ListFoodsActivity2.class);
        if (text.isEmpty()) {
            intent.putExtra("isViewAll", true);
        } else {
            intent.putExtra("text", text);
            intent.putExtra("isSearch", true);
        }
        startActivity(intent);
    }

    private void initBestFood() {
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBarBestFood.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();
        Query query = myRef.orderByChild("bestFood").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Foods.class));
                    }
                    if (list.size() > 0 && getContext() != null) {
                        binding.bestFoodView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        RecyclerView.Adapter adapter = new BestFoodsAdapter(list);
                        binding.bestFoodView.setAdapter(adapter);
                    }
                }
                binding.progressBarBestFood.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (binding == null) return;
                binding.progressBarBestFood.setVisibility(View.GONE);
            }
        });
    }

    private void initCategory() {
        DatabaseReference myRef = database.getReference("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Category.class));
                    }
                    if (list.size() > 0 && getContext() != null) {
                        binding.categoryView.setLayoutManager(new GridLayoutManager(getContext(), 4));
                        RecyclerView.Adapter adapter = new CategoryAdapter(list);
                        binding.categoryView.setAdapter(adapter);
                    }
                }
                binding.progressBarCategory.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (binding == null) return;
                binding.progressBarCategory.setVisibility(View.GONE);
            }
        });
    }

    // --- FUNGSI BARU UNTUK FILTER KATEGORI ---
    private void initFilterCategory() {
        DatabaseReference myRef = database.getReference("FilterCategory");
        ArrayList<FilterCategory> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null || getContext() == null) return;

                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(FilterCategory.class));
                    }
                    ArrayAdapter<FilterCategory> adapter = new ArrayAdapter<>(getContext(), R.layout.sp_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.categoryFilterSp.setAdapter(adapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // --- FUNGSI INI TETAP ADA ---
    private void initPrice() {
        DatabaseReference myRef = database.getReference("Price");
        ArrayList<Price> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null || getContext() == null) return;

                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Price.class));
                    }
                    ArrayAdapter<Price> adapter = new ArrayAdapter<>(getContext(), R.layout.sp_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.priceSp.setAdapter(adapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopSliderTimer();

        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }

        binding = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopSliderTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(promoList != null && !promoList.isEmpty()) {
            startSliderTimer();
        }
    }
}