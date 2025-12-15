package com.example.foodapp.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class BaseActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inisialisasi Firebase Database dan Authentication
        // Pastikan ini ada di dalam onCreate
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Mengatur warna status bar (opsional, tapi bagus untuk konsistensi)
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
    }
}
