package com.example.foodapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.foodapp.Activity.DetailActivity;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.R;

import java.text.NumberFormat; // <-- IMPORT BARU
import java.util.ArrayList;
import java.util.Locale; // <-- IMPORT BARU

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.viewholder> {

    ArrayList<Foods> items;
    Context context;

    public FoodListAdapter(ArrayList<Foods> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public FoodListAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_list_food, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodListAdapter.viewholder holder, int position) {
        Foods currentFood = items.get(position);
        holder.titleTxt.setText(currentFood.getTitle());
        holder.timeTxt.setText(currentFood.getTimeValue() + "min");
        holder.rateTxt.setText(String.valueOf(currentFood.getStar()));

        // --- PENYESUAIAN HARGA DARI DOLAR KE RUPIAH ---
        // 1. Dapatkan harga (misal: 25000.0)
        double price = currentFood.getPrice();

        // 2. Buat formatter untuk mata uang Indonesia (Rupiah)
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

        // 3. Format harga
        String formattedPrice = formatter.format(price);

        // 4. Atur teks
        holder.priceTxt.setText(formattedPrice);
        // ------------------------------------------------

        // --- PENYESUAIAN 1: CARA MEMUAT GAMBAR ---
        // Mencari ID resource di folder "drawable" berdasarkan namanya
        int drawableResourceId = context.getResources().getIdentifier(currentFood.getImagePath(),
                "drawable", context.getPackageName());

        Glide.with(context)
                .load(drawableResourceId) // Memuat gambar dari resource aplikasi
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.pic);
        // -------------------------------------------

        // --- PENYESUAIAN 2: MENAMBAHKAN AKSI KLIK ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            // Mengirim seluruh data makanan yang di-klik ke halaman Detail
            intent.putExtra("object", currentFood);
            context.startActivity(intent);
        });
        // -------------------------------------------
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt, rateTxt, timeTxt;
        ImageView pic;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            rateTxt = itemView.findViewById(R.id.rateTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            pic = itemView.findViewById(R.id.img);
        }
    }
}

