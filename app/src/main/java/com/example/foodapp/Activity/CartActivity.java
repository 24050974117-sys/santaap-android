package com.example.foodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Adapter.CartAdapter;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.Domain.Order;
import com.example.foodapp.Helper.ManagmentCart;
import com.example.foodapp.MyApplication; // Import ini
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityCartBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Import yang DIPERLUKAN
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;


public class CartActivity extends BaseActivity {
    private static final String TAG = "CartActivity";

    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private NumberFormat formatter;

    private long taxForUi;
    private long deliveryForUi;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    private String currentOrderId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);
        formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        setVariable();
        calculateCartForUi();
        initList();
    }

    private void initList() {
        if (managmentCart.getListCart().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollView3.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollView3.setVisibility(View.VISIBLE);
        }

        binding.cartView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new CartAdapter(managmentCart.getListCart(), this, this::calculateCartForUi, formatter);
        binding.cartView.setAdapter(adapter);
    }

    private void calculateCartForUi() {
        double percentTax = 0.02;
        deliveryForUi = 15000L;
        long subtotal = Math.round(managmentCart.getTotalFee());
        taxForUi = Math.round(subtotal * percentTax);
        long total = subtotal + taxForUi + deliveryForUi;
        long itemTotal = subtotal;

        binding.totalFeeTxt.setText(formatter.format(itemTotal));
        binding.taxTxt.setText(formatter.format(taxForUi));
        binding.deliveryTxt.setText(formatter.format(deliveryForUi));
        binding.totalTxt.setText(formatter.format(total));
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.button.setOnClickListener(v -> {
            Log.d(TAG, "Button Place Order diklik");
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            if (managmentCart.getListCart().isEmpty()) {
                Toast.makeText(this, "Keranjang Anda kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            // DAPATKAN TOKEN BARU DARI POSTMAN
            String NEW_DEMO_TOKEN = "ac61d6cb-177a-4b1f-b234-a2c80e577c1a"; // <-- GANTI INI DENGAN TOKEN BARU

            Log.d(TAG, "Mode Demo: Langsung menyimpan pesanan...");
            saveOrderToFirebase("Menunggu Pembayaran");

            startMidtransPayment(NEW_DEMO_TOKEN);
        });
    }


    // ========================================================================
    // === FUNGSI INI TELAH DIPERBARUI DENGAN URUTAN YANG BENAR ===
    // ========================================================================
    private void startMidtransPayment(String token) {
        try {
            // --- BAGIAN 1: KONFIGURASI SDK DENGAN CALLBACK ---
            // Kita HARUS melakukan ini SEBELUM setTransactionRequest
            Log.d(TAG, "Membangun UI Flow dengan CALLBACK baru...");
            SdkUIFlowBuilder.init()
                    .setContext(this)
                    .setClientKey(MyApplication.CLIENT_KEY) // Menggunakan Client Key dari MyApplication
                    .setTransactionFinishedCallback(new TransactionFinishedCallback() {
                        @Override
                        public void onTransactionFinished(TransactionResult result) {
                            handlePaymentResult(result);
                        }
                    })
                    .setMerchantBaseUrl(MyApplication.BASE_URL) // Menggunakan Base URL dari MyApplication
                    .enableLog(true)
                    .buildSDK();

            // --- BAGIAN 2: SET DUMMY DATA (Mencegah NullPointerException) ---
            // Ini harus dipanggil SETELAH buildSDK() dan SEBELUM startPaymentUiFlow()
            Log.d(TAG, "Dummy TransactionRequest di-set.");
            TransactionRequest dummyRequest = new TransactionRequest("DUMMY-ORDER-ID-" + System.currentTimeMillis(), 10000);
            ArrayList<ItemDetails> dummyItems = new ArrayList<>();
            dummyItems.add(new ItemDetails("1", 10000, 1, "Dummy Item"));
            dummyRequest.setItemDetails(dummyItems);
            CustomerDetails dummyCustomer = new CustomerDetails();
            dummyCustomer.setFirstName("User");
            dummyCustomer.setPhone("081234567890");
            dummyCustomer.setEmail("user@example.com");
            dummyRequest.setCustomerDetails(dummyCustomer);

            // Pastikan instance-nya tidak null setelah di-build
            if (MidtransSDK.getInstance() == null) {
                Log.e(TAG, "Midtrans SDK GAGAL diinisialisasi oleh builder.");
                Toast.makeText(this, "Error SDK Midtrans", Toast.LENGTH_SHORT).show();
                return;
            }

            MidtransSDK.getInstance().setTransactionRequest(dummyRequest);

            // --- BAGIAN 3: MULAI PEMBAYARAN ---
            Log.d(TAG, "startPaymentUiFlow(token) dipanggil.");
            MidtransSDK.getInstance().startPaymentUiFlow(this, token);

        } catch (Exception e) {
            Log.e(TAG, "Error di startMidtransPayment: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void handlePaymentResult(TransactionResult result) {
        if (result == null) {
            Toast.makeText(this, "Transaksi Error", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "handlePaymentResult DIPANGGIL. Status: " + result.getStatus());

        if (result.getResponse() != null) {
            String status = result.getStatus();
            switch (status) {
                case TransactionResult.STATUS_SUCCESS:
                    Toast.makeText(this, "Pembayaran berhasil!", Toast.LENGTH_SHORT).show();
                    updateOrderStatus("Sedang Dibuat");
                    clearCartAndFinish();
                    break;

                case TransactionResult.STATUS_PENDING:
                    Toast.makeText(this, "Pembayaran pending", Toast.LENGTH_SHORT).show();
                    // Order sudah dibuat, jadi kita hanya clear cart & pindah
                    clearCartAndFinish();
                    break;

                case TransactionResult.STATUS_FAILED:
                    Toast.makeText(this, "Pembayaran gagal", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else if (result.isTransactionCanceled()) {
            Toast.makeText(this, "Transaksi dibatalkan", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Transaksi Selesai (Unknown): " + result.getStatus(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrderToFirebase(String paymentStatus) {
        currentOrderId = "FOODAPP-" + System.currentTimeMillis();
        String userId = mAuth.getCurrentUser().getUid();

        if (userId == null) {
            Toast.makeText(this, "Error: User tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        long subtotal = 0;
        for (Foods food : managmentCart.getListCart()) {
            subtotal += Math.round(food.getPrice() * food.getNumberInCart());
        }
        long localTax = Math.round(subtotal * 0.02);
        long localDelivery = 15000L;
        long total = subtotal + localTax + localDelivery;

        Order newOrder = new Order();
        newOrder.setOrderId(currentOrderId);
        newOrder.setUserId(userId);
        newOrder.setItems(new ArrayList<>(managmentCart.getListCart()));
        newOrder.setTotalPrice((double) total);
        newOrder.setTimestamp(System.currentTimeMillis());
        newOrder.setStatus(paymentStatus);

        DatabaseReference orderRef = database.getReference("Orders")
                .child(userId)
                .child(currentOrderId);

        orderRef.setValue(newOrder).addOnFailureListener(e -> {
            Toast.makeText(CartActivity.this, "Gagal menyimpan pesanan demo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Gagal menyimpan pesanan demo ke Firebase", e);
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Pesanan demo BERHASIL disimpan ke Firebase dengan status: " + paymentStatus);
        });
    }

    private void updateOrderStatus(String newStatus) {
        if (currentOrderId == null || mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Tidak bisa update status, orderId atau userId null");
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        database.getReference("Orders")
                .child(userId)
                .child(currentOrderId)
                .child("status")
                .setValue(newStatus)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Status order berhasil di-update ke: " + newStatus));
    }

    private void clearCartAndFinish() {
        managmentCart.clearCart();
        Toast.makeText(CartActivity.this, "Pesanan berhasil dicatat!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(CartActivity.this, MainActivity.class);
        intent.putExtra("openFragment", "OrdersFragment");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}