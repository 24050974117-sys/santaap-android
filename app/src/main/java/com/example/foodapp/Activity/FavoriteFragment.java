package com.example.foodapp.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager; // <-- GANTI KE GRID
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Adapter.FoodListAdapter; // <-- GANTI KEMBALI KE FoodListAdapter
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.R;
import com.example.foodapp.databinding.FragmentFavoriteBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteFragment extends Fragment {

    private FragmentFavoriteBinding binding;
    private FoodListAdapter adapter; // <-- Menggunakan FoodListAdapter
    private ArrayList<Foods> favoriteList;
    private DatabaseReference favRef;
    private DatabaseReference foodsRef;
    private ValueEventListener favListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private ArrayList<String> favoriteFoodIds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        foodsRef = database.getReference("Foods");

        initRecyclerView();

        return binding.getRoot();
    }

    private void initRecyclerView() {
        favoriteList = new ArrayList<>();
        adapter = new FoodListAdapter(favoriteList); // Gunakan adapter grid Anda

        // --- INI PERUBAHAN UTAMA ---
        // Mengembalikan layout manager menjadi grid 2 kolom
        binding.favoriteRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // --------------------------

        binding.favoriteRecyclerView.setAdapter(adapter);
    }

    private void loadFavoriteFoodIds() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            favRef = database.getReference("Users").child(uid).child("Favorites");

            if (binding == null) return;
            binding.progressBar.setVisibility(View.VISIBLE);

            favoriteFoodIds = new ArrayList<>();

            favListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding == null) return;
                    favoriteFoodIds.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot issue : snapshot.getChildren()) {
                            favoriteFoodIds.add(issue.getKey());
                        }
                        loadFavoriteFoods();
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        favoriteList.clear();
                        adapter.notifyDataSetChanged();
                        showEmptyState(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Gagal memuat favorit", Toast.LENGTH_SHORT).show();
                }
            };
            favRef.addValueEventListener(favListener);
        } else {
            showEmptyState(true);
        }
    }

    private void loadFavoriteFoods() {
        if (favoriteFoodIds.isEmpty()) {
            favoriteList.clear();
            adapter.notifyDataSetChanged();
            binding.progressBar.setVisibility(View.GONE);
            showEmptyState(true);
            return;
        }

        showEmptyState(false);

        foodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                favoriteList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                        Foods food = foodSnapshot.getValue(Foods.class);
                        if (food != null && favoriteFoodIds.contains(food.getImagePath())) {
                            favoriteList.add(food);
                        }
                    }
                }

                if(favoriteList.isEmpty()){
                    showEmptyState(true);
                }

                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Gagal memuat data makanan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState(boolean show) {
        if (binding != null) {
            binding.emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.favoriteRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavoriteFoodIds();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (favRef != null && favListener != null) {
            favRef.removeEventListener(favListener);
        }
        binding = null;
    }
}