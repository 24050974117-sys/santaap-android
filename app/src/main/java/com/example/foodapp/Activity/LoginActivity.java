package com.example.foodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.foodapp.databinding.ActivityLoginBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends BaseActivity {
    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
    }

    private void setVariable() {
        // Tombol untuk pindah ke halaman Signup
        binding.signupTxt.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class))
        );

        // Tombol untuk proses Login
        binding.loginBtn.setOnClickListener(v -> {
            String usernameOrName = binding.userEdt.getText().toString(); // Pengguna mengetik 'name'
            String password = binding.passEdt.getText().toString();

            if (usernameOrName.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Tolong isi semua kolom", Toast.LENGTH_SHORT).show();
                return;
            }

            // Langkah 1: Cari email berdasarkan 'name' di Realtime Database
            DatabaseReference usersRef = database.getReference("Users");

            // --- INI PERBAIKANNYA ---
            // Kita ubah 'username' menjadi 'name' agar cocok dengan database baru Anda
            Query query = usersRef.orderByChild("name").equalTo(usernameOrName);
            // -------------------------

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Nama ditemukan, ambil email yang terhubung
                        String emailFromDb = "";
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            emailFromDb = userSnapshot.child("email").getValue(String.class);
                            break; // Cukup ambil satu email
                        }

                        if (emailFromDb == null || emailFromDb.isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Data email tidak ditemukan untuk user ini.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Langkah 2: Gunakan email untuk login ke Firebase Auth
                        mAuth.signInWithEmailAndPassword(emailFromDb, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish(); // Tutup halaman login
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Password salah.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Nama tidak ditemukan
                        Toast.makeText(LoginActivity.this, "Nama pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Gagal mengakses database", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}