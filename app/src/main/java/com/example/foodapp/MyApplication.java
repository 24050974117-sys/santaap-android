package com.example.foodapp; // Pastikan package name ini sesuai dengan Anda

import android.app.Application;
import android.util.Log;

import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;
// Hapus import TransactionFinishedCallback jika ada
// Hapus "implements TransactionFinishedCallback" jika ada

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    // ========================================================================
    // === INI ADALAH 2 BARIS YANG HILANG (PENYEBAB ERROR ANDA) ===
    // ========================================================================
    // Variabel ini harus 'public static final' agar bisa dibaca dari CartActivity
    public static final String CLIENT_KEY = "Mid-client-Da1grNLzw61zvdWJ"; // <-- GANTI DENGAN CLIENT KEY ANDA
    public static final String BASE_URL = "https://midtrans.com/"; // URL valid (tidak akan dipakai)
    // ========================================================================


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MyApplication onCreate, initializing Midtrans SDK...");

        // Ini adalah inisialisasi SDK global
        SdkUIFlowBuilder.init()
                .setContext(this)
                .setClientKey(CLIENT_KEY) // Menggunakan variabel di atas
                .setMerchantBaseUrl(BASE_URL) // Menggunakan variabel di atas
                .enableLog(true)
                .buildSDK();

        Log.d(TAG, "Midtrans SDK Initialized SUCCESSFULLY.");
    }
}