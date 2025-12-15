package com.example.foodapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodapp.Activity.ListFoodsActivity2;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.Domain.Promo;
import com.example.foodapp.Helper.ManagmentCart;
import com.example.foodapp.databinding.PromoCardItemBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PromoSliderAdapter extends RecyclerView.Adapter<PromoSliderAdapter.ViewHolder> {

    private ArrayList<Promo> items;
    private Context context;

    // --- TAMBAHAN BARU ---
    private ManagmentCart managmentCart;
    private DatabaseReference foodsRef;
    // --------------------

    public PromoSliderAdapter(ArrayList<Promo> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        PromoCardItemBinding binding = PromoCardItemBinding.inflate(LayoutInflater.from(context), parent, false);

        // --- INISIALISASI BARU ---
        managmentCart = new ManagmentCart(context);
        foodsRef = FirebaseDatabase.getInstance().getReference("Foods");
        // -----------------------

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Logika looping
        Promo promo = items.get(position % getRealCount());

        holder.binding.titleTxt.setText(promo.getTitle());
        holder.binding.subtitleTxt.setText(promo.getSubtitle());

        // Set Warna
        int bgColor = context.getResources().getColor(promo.getBgColor());
        int titleColor = context.getResources().getColor(promo.getTitleColor());
        int subtitleColor = context.getResources().getColor(promo.getSubtitleColor());
        int btnColor = context.getResources().getColor(promo.getBtnColor());
        int btnTextColor = context.getResources().getColor(promo.getBtnTextColor());

        holder.binding.cardLayout.setCardBackgroundColor(bgColor);
        holder.binding.titleTxt.setTextColor(titleColor);
        holder.binding.subtitleTxt.setTextColor(subtitleColor);
        holder.binding.orderNowBtn.setBackgroundTintList(ColorStateList.valueOf(btnColor));
        holder.binding.orderNowBtn.setTextColor(btnTextColor);

        // Muat Gambar
        int drawableResourceId = context.getResources().getIdentifier(
                promo.getPicUrl(), "drawable", context.getPackageName());

        Glide.with(context)
                .load(drawableResourceId)
                .into(holder.binding.pic);

        // --- LOGIKA KLIK TOMBOL BARU ---
        holder.binding.orderNowBtn.setOnClickListener(v -> {
            String foodId = promo.getFoodId();
            int quantity = promo.getQuantityToAdd();

            if (foodId == null || foodId.isEmpty() || quantity == 0) {
                // Kasus spesial untuk promo "OKTOBER MURAH"
                if ("VIEW_ALL".equals(foodId)) {
                    Intent intent = new Intent(context, ListFoodsActivity2.class);
                    intent.putExtra("isViewAll", true);
                    context.startActivity(intent);
                } else {
                    // Promo lain yang tidak menambah item
                    Toast.makeText(context, "Promo spesial untuk Anda!", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // Jika ini promo untuk item spesifik, cari dan tambahkan
            findFoodAndAddToCart(foodId, quantity);
        });
    }

    // --- FUNGSI HELPER BARU ---
    private void findFoodAndAddToCart(String foodId, int quantity) {
        // Kita query berdasarkan 'imagePath' karena itu yang kita gunakan sebagai ID
        Query query = foodsRef.orderByChild("imagePath").equalTo(foodId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Foods foodToAdd = null;
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        foodToAdd = issue.getValue(Foods.class);
                        break; // Ambil item pertama yang cocok
                    }

                    if (foodToAdd != null) {
                        foodToAdd.setNumberInCart(quantity);
                        managmentCart.insertFood(foodToAdd);
                        Toast.makeText(context, quantity + "x " + foodToAdd.getTitle() + " ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Item promo tidak dapat ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error database: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public int getRealCount() {
        return items.size();
    }

    @Override
    public int getItemCount() {
        // Kita buat "tak terbatas" untuk looping
        return Integer.MAX_VALUE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        PromoCardItemBinding binding;

        public ViewHolder(PromoCardItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}