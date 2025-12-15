package com.example.foodapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Domain.Order;
import com.example.foodapp.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// Pastikan Anda 'extends RecyclerView.Adapter'
public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.viewholder> {

    ArrayList<Order> items;
    Context context;
    NumberFormat formatter;

    // Constructor yang menerima ArrayList<Order>
    public OrderHistoryAdapter(ArrayList<Order> items) {
        this.items = items;
        // Siapkan formatter Rupiah di sini
        this.formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_order_history, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        Order order = items.get(position);

        if (order == null || order.getOrderId() == null) {
            return; // Keamanan jika data tidak lengkap
        }

        holder.orderId.setText("Order #" + order.getOrderId().substring(0, 8)); // Tampilkan 8 char pertama ID
        holder.totalPrice.setText(formatter.format(order.getTotalPrice()));
        holder.status.setText(order.getStatus());
        holder.itemCount.setText(order.getItems().size() + " Items");

        // Format tanggal dari timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("in", "ID"));
        holder.date.setText(sdf.format(new Date(order.getTimestamp())));

        // Atur warna status (pastikan Anda membuat drawable-nya)
        if (order.getStatus().equals("Selesai")) {
            // holder.status.setBackgroundResource(R.drawable.status_completed_background); // (Opsional)
        } else if (order.getStatus().equals("Sedang Dibuat")) {
            // holder.status.setBackgroundResource(R.drawable.status_inprogress_background); // (Opsional)
        } else {
            holder.status.setBackgroundResource(R.drawable.status_pending_background); // (Sudah kita buat)
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView orderId, date, totalPrice, status, itemCount;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderIdTxt);
            date = itemView.findViewById(R.id.orderDateTxt);
            totalPrice = itemView.findViewById(R.id.orderTotalTxt);
            status = itemView.findViewById(R.id.orderStatusTxt);
            itemCount = itemView.findViewById(R.id.orderItemCountTxt);
        }
    }
}