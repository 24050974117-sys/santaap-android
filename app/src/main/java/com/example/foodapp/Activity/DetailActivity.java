package com.example.foodapp.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.Helper.ManagmentCart;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

// --- IMPORT BARU YANG DIPERLUKAN ---
import java.text.NumberFormat;
import java.util.Locale;
// -----------------------------------

public class DetailActivity extends BaseActivity {
    private ActivityDetailBinding binding;
    private Foods object;
    private int numberInCart = 1;
    private ManagmentCart managmentCart;

    private NumberFormat formatter; // <-- VARIABEL BARU UNTUK FORMATTER

    // --- Variabel baru untuk Fitur Favorit ---
    private DatabaseReference favRef;
    private boolean isFavorite = false;
    private String foodId;
    private String uid;
    // ----------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- PENYESUAIAN: Inisialisasi formatter ---
        formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        // ------------------------------------------

        getIntentExtra();
        setVariable();
        initFavorite();
    }

    private void setVariable() {
        managmentCart = new ManagmentCart(this);

        binding.backBtn.setOnClickListener(v -> finish());

        int drawableResourceId = this.getResources().getIdentifier(object.getImagePath(),
                "drawable", this.getPackageName());

        Glide.with(this)
                .load(drawableResourceId)
                .into(binding.foodPic);

        // --- PENYESUAIAN HARGA: Gunakan formatter ---
        binding.priceTxt.setText(formatter.format(object.getPrice()));
        binding.titleTxt.setText(object.getTitle());
        binding.descriptionTxt.setText(object.getDescription());
        binding.rateTxt.setText(object.getStar() + " Rating");
        binding.timeTxt.setText(object.getTimeValue() + " min");
        binding.totalTxt.setText(formatter.format(object.getPrice())); // Harga total awal
        // -------------------------------------------

        binding.numTxt.setText(String.valueOf(numberInCart));

        binding.plusBtn.setOnClickListener(v -> {
            numberInCart = numberInCart + 1;
            binding.numTxt.setText(String.valueOf(numberInCart));
            // --- PENYESUAIAN HARGA: Gunakan formatter ---
            binding.totalTxt.setText(formatter.format(numberInCart * object.getPrice()));
        });

        binding.minusBtn.setOnClickListener(v -> {
            if (numberInCart > 1) {
                numberInCart = numberInCart - 1;
                binding.numTxt.setText(String.valueOf(numberInCart));
                // --- PENYESUAIAN HARGA: Gunakan formatter ---
                binding.totalTxt.setText(formatter.format(numberInCart * object.getPrice()));
            }
        });

        // Logika "Add to Cart" (tetap sama)
        binding.addBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Pesanan")
                    .setMessage("Tambahkan " + numberInCart + " " + object.getTitle() + " ke keranjang?")
                    .setPositiveButton("Ya, Tambahkan", (dialog, which) -> {
                        object.setNumberInCart(numberInCart);
                        managmentCart.insertFood(object);
                        startActivity(new Intent(DetailActivity.this, CartActivity.class));
                    })
                    .setNegativeButton("Batal", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });
    }

    // --- METODE UNTUK FITUR FAVORIT (Tetap sama) ---

    private void initFavorite() {
        if (mAuth.getCurrentUser() != null) {
            uid = mAuth.getCurrentUser().getUid();
            foodId = object.getImagePath();
            favRef = database.getReference("Users").child(uid).child("Favorites").child(foodId);

            checkFavoriteStatus();

            binding.favBtn.setOnClickListener(v -> toggleFavorite());
        } else {
            binding.favBtn.setVisibility(View.GONE);
        }
    }

    private void checkFavoriteStatus() {
        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isFavorite = true;
                    binding.favBtn.setImageResource(R.drawable.ic_favorite_filled);
                } else {
                    isFavorite = false;
                    binding.favBtn.setImageResource(R.drawable.ic_favorite_border);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void toggleFavorite() {
        if (isFavorite) {
            favRef.removeValue();
            binding.favBtn.setImageResource(R.drawable.ic_favorite_border);
            Toast.makeText(this, "Dihapus dari favorit", Toast.LENGTH_SHORT).show();
            isFavorite = false;
        } else {
            favRef.setValue(true);
            binding.favBtn.setImageResource(R.drawable.ic_favorite_filled);
            Toast.makeText(this, "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show();
            isFavorite = true;
        }
    }
    // ----------------------------------------

    private void getIntentExtra() {
        object = (Foods) getIntent().getSerializableExtra("object");
    }
}

