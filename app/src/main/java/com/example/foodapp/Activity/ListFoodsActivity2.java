package com.example.foodapp.Activity;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodapp.Adapter.FoodListAdapter;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.databinding.ActivityListFoods2Binding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ListFoodsActivity2 extends BaseActivity {
    ActivityListFoods2Binding binding;
    private RecyclerView.Adapter adapterListFood;
    private int categoryId;
    private String categoryName;
    private String searchText;
    private boolean isSearch;
    private boolean isFilter;
    private boolean isViewAll;
    private int locationId;
    private int timeId;
    private int priceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFoods2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initList();
    }

    private void initList() {
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();

        // Logika untuk menentukan query atau cara pengambilan data
        if (isViewAll) {
            // 1. Jika "VIEW ALL", ambil semua makanan
            myRef.addListenerForSingleValueEvent(createValueEventListener(list, null));
        } else if (isSearch) {
            // 2. Jika "Search", ambil semua makanan lalu saring di aplikasi
            myRef.addListenerForSingleValueEvent(createValueEventListener(list, "search"));
        } else if (isFilter) {
            // 3. Jika "Filter", ambil semua makanan lalu saring di aplikasi
            myRef.addListenerForSingleValueEvent(createValueEventListener(list, "filter"));
        } else {
            // 4. Jika dari kategori, gunakan query Firebase yang efisien
            Query query = myRef.orderByChild("categoryId").equalTo(categoryId);
            query.addListenerForSingleValueEvent(createValueEventListener(list, null));
        }
    }

    // Method pembantu untuk membuat ValueEventListener agar tidak duplikat kode
    private ValueEventListener createValueEventListener(ArrayList<Foods> list, String filterType) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Foods food = issue.getValue(Foods.class);

                        // Logika penyaringan di dalam aplikasi
                        if (filterType != null) {
                            if (filterType.equals("search")) {
                                if (food.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                                    list.add(food);
                                }
                            } else if (filterType.equals("filter")) {
                                boolean locationMatch = locationId == 0 || food.getLocationId() == locationId;
                                boolean timeMatch = timeId == 0 || food.getTimeId() == timeId;
                                boolean priceMatch = priceId == 0 || food.getPriceId() == priceId;
                                if (locationMatch && timeMatch && priceMatch) {
                                    list.add(food);
                                }
                            }
                        } else {
                            // Jika bukan search atau filter, tambahkan semua
                            list.add(food);

                        }
                    }
                }

                if (!list.isEmpty()) {
                    setupRecyclerView(list);
                } else {
                    // Tampilkan pesan jika tidak ada hasil
                    binding.emptyTxt.setVisibility(View.VISIBLE);
                    binding.foodListView.setVisibility(View.GONE);
                }

                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        };
    }

    private void setupRecyclerView(ArrayList<Foods> list) {
        binding.foodListView.setLayoutManager(new GridLayoutManager(ListFoodsActivity2.this, 2));
        adapterListFood = new FoodListAdapter(list);
        binding.foodListView.setAdapter(adapterListFood);
    }

    private void getIntentExtra() {
        categoryId = getIntent().getIntExtra("categoryId", 0);
        categoryName = getIntent().getStringExtra("categoryName");
        searchText = getIntent().getStringExtra("text");
        isSearch = getIntent().getBooleanExtra("isSearch", false);
        isFilter = getIntent().getBooleanExtra("isFilter", false);
        isViewAll = getIntent().getBooleanExtra("isViewAll", false);
        locationId = getIntent().getIntExtra("locationId", 0);
        timeId = getIntent().getIntExtra("timeId", 0);
        priceId = getIntent().getIntExtra("priceId", 0);

        // Mengatur judul halaman berdasarkan aksi
        if (isViewAll) {
            binding.titleTxt.setText("All Foods");
        } else if (isSearch) {
            binding.titleTxt.setText("Result for '" + searchText + "'");
        } else if (isFilter) {
            binding.titleTxt.setText("Filtered Foods");
        } else {
            binding.titleTxt.setText(categoryName);
        }

        binding.backBtn.setOnClickListener(v -> finish());
    }
}

