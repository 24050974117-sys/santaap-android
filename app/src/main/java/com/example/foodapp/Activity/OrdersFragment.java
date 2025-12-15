package com.example.foodapp.Activity; // Pastikan package-nya com.example.foodapp.Activity

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.foodapp.Adapter.OrderHistoryAdapter;
import com.example.foodapp.Domain.Order;
import com.example.foodapp.R;
import com.example.foodapp.databinding.FragmentOrdersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private OrderHistoryAdapter adapter;
    private ArrayList<Order> orderList;
    private DatabaseReference orderRef;
    private ValueEventListener orderListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Menggunakan View Binding untuk layout fragment
        binding = FragmentOrdersBinding.inflate(inflater, container, false);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        initRecyclerView();
        loadOrderHistory();

        return binding.getRoot();
    }

    private void initRecyclerView() {
        orderList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(orderList); // Adapter baru yang akan kita buat
        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.ordersRecyclerView.setAdapter(adapter);
    }

    private void loadOrderHistory() {
        // Cek apakah pengguna sudah login
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            // Referensi ke node /Orders/{userId}
            orderRef = database.getReference("Orders").child(uid);

            binding.progressBar.setVisibility(View.VISIBLE);

            orderListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    orderList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            // Ambil setiap pesanan sebagai objek Order
                            orderList.add(orderSnapshot.getValue(Order.class));
                        }

                        // Balik urutan agar pesanan terbaru di atas
                        Collections.reverse(orderList);
                        binding.emptyStateLayout.setVisibility(View.GONE);
                        binding.ordersRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        // Tidak ada riwayat pesanan
                        binding.emptyStateLayout.setVisibility(View.VISIBLE);
                        binding.ordersRecyclerView.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged(); // Perbarui tampilan
                    binding.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            };
            // Menggunakan addValueEventListener agar daftar otomatis update jika status berubah
            orderRef.addValueEventListener(orderListener);

        } else {
            // Pengguna tidak login, tampilkan pesan "kosong"
            binding.progressBar.setVisibility(View.GONE);
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.ordersRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hapus listener Firebase saat fragment dihancurkan untuk mencegah memory leak
        if (orderRef != null && orderListener != null) {
            orderRef.removeEventListener(orderListener);
        }
        binding = null;
    }
}