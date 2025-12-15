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

public class BestFoodsAdapter extends RecyclerView.Adapter<BestFoodsAdapter.viewholder> {
    ArrayList<Foods> items;
    Context context;

    public BestFoodsAdapter(ArrayList<Foods> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_best_food, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        Foods currentFood = items.get(position);
        holder.titleTxt.setText(currentFood.getTitle());
        holder.timeTxt.setText(currentFood.getTimeValue() + " min");
        holder.starTxt.setText(String.valueOf(currentFood.getStar()));

        // --- PENYESUAIAN HARGA DARI DOLAR KE RUPIAH ---
        // 1. Dapatkan harga (misal: 45000.0)
        double price = currentFood.getPrice();

        // 2. Buat formatter untuk mata uang Indonesia (Rupiah)
        // Locale "in", "ID" akan menghasilkan format "Rp45.000,00"
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

        // 3. Format harga
        String formattedPrice = formatter.format(price);

        // 4. Atur teks
        holder.priceTxt.setText(formattedPrice);
        // ------------------------------------------------

        // Mencari ID resource di folder "drawable" berdasarkan namanya dari database
        int drawableResourceId = context.getResources().getIdentifier(currentFood.getImagePath(),
                "drawable", context.getPackageName());

        Glide.with(context)
                .load(drawableResourceId) // Sekarang memuat gambar dari resource aplikasi
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.pic);

        // Menambahkan aksi klik untuk membuka halaman detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", currentFood);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt, starTxt, timeTxt;
        ImageView pic;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            starTxt = itemView.findViewById(R.id.starTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}

