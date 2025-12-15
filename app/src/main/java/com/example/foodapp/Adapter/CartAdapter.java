package com.example.foodapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.Helper.ChangeNumberItemsListener;
import com.example.foodapp.Helper.ManagmentCart;
import com.example.foodapp.R;

// --- IMPORT BARU YANG DIPERLUKAN ---
import java.text.NumberFormat;
import java.util.ArrayList;
// -----------------------------------

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.viewholder> {
    ArrayList<Foods> list;
    private ManagmentCart managmentCart;
    ChangeNumberItemsListener changeNumberItemsListener;
    NumberFormat formatter; // <-- VARIABEL BARU UNTUK FORMATTER

    // --- PENYESUAIAN 1: Tambahkan 'NumberFormat formatter' di constructor ---
    public CartAdapter(ArrayList<Foods> list, Context context, ChangeNumberItemsListener changeNumberItemsListener, NumberFormat formatter) {
        this.list = list;
        this.managmentCart = new ManagmentCart(context);
        this.changeNumberItemsListener = changeNumberItemsListener;
        this.formatter = formatter; // Simpan formatter
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cart, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        Foods currentFood = list.get(position);
        holder.title.setText(currentFood.getTitle());
        holder.num.setText(String.valueOf(currentFood.getNumberInCart()));

        // --- PENYESUAIAN 2: Gunakan formatter untuk harga per item ---
        holder.feeEachItem.setText(formatter.format(currentFood.getPrice()));
        // ---------------------------------------------------------

        int drawableResourceId = holder.itemView.getContext().getResources().getIdentifier(
                currentFood.getImagePath(), "drawable", holder.itemView.getContext().getPackageName());

        Glide.with(holder.itemView.getContext())
                .load(drawableResourceId)
                .into(holder.pic);

        // Logika tombol plus
        holder.plusBtn.setOnClickListener(v ->
                managmentCart.plusNumberFood(list, position, () -> {
                    notifyDataSetChanged();
                    changeNumberItemsListener.changed();
                })
        );

        // Logika tombol minus
        holder.minusBtn.setOnClickListener(v ->
                managmentCart.minusNumberFood(list, position, () -> {
                    notifyDataSetChanged();
                    changeNumberItemsListener.changed();
                })
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView title, feeEachItem, plusBtn, minusBtn, num;
        ImageView pic;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleTxt);
            pic = itemView.findViewById(R.id.pic);
            feeEachItem = itemView.findViewById(R.id.feeEachItem);
            plusBtn = itemView.findViewById(R.id.plusCartBtn);
            minusBtn = itemView.findViewById(R.id.minusCartBtn);
            num = itemView.findViewById(R.id.numberItemTxt);
        }
    }
}

