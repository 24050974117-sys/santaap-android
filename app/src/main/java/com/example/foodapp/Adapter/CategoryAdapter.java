package com.example.foodapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout; // Pastikan ini di-import
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodapp.Activity.ListFoodsActivity2;
import com.example.foodapp.Domain.Category;
import com.example.foodapp.R;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.viewholder> {
    ArrayList<Category> items;
    Context context;

    public CategoryAdapter(ArrayList<Category> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_category, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        Category currentCategory = items.get(position);
        holder.titleTxt.setText(currentCategory.getTitle());

        // --- PENYESUAIAN: Blok 'switch' dihapus ---

        // Ganti dengan satu baris ini untuk membuat semua warna sama.
        // Ganti 'cat_0_background' dengan drawable lain jika Anda ingin warna yang berbeda.
        holder.background.setBackgroundResource(R.drawable.cat_0_background);

        // Mencari ID resource ikon di folder "drawable"
        int drawableResourceId = context.getResources().getIdentifier(currentCategory.getPic(),
                "drawable", context.getPackageName());

        Glide.with(context)
                .load(drawableResourceId)
                .into(holder.pic);

        // Menambahkan aksi klik
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ListFoodsActivity2.class);
            intent.putExtra("categoryId", currentCategory.getId());
            intent.putExtra("categoryName", currentCategory.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt;
        ImageView pic;
        ConstraintLayout background; // Referensi ke background ikon

        public viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.catNameTxt);
            pic = itemView.findViewById(R.id.imgCat);
            background = itemView.findViewById(R.id.background_cat); // ID dari viewholder_category.xml
        }
    }
}

