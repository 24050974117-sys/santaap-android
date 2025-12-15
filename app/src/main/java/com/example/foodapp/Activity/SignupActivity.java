package com.example.foodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.foodapp.databinding.ActivitySignupBinding;
import com.google.firebase.database.DatabaseReference;
import java.util.HashMap;
import com.example.foodapp.Helper.ManagmentCart; // <-- TAMBAHKAN IMPORT INI

public class SignupActivity extends BaseActivity {
    ActivitySignupBinding binding;
    private final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
    }

    private void setVariable() {
        binding.signupBtn.setOnClickListener(v -> {
            String username = binding.usernameEdt.getText().toString();
            String email = binding.userEdt.getText().toString();
            String password = binding.passEdt.getText().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Tolong isi semua kolom", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(SignupActivity.this, "Password minimal harus 6 karakter", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Pendaftaran di Firebase Auth berhasil.");
                    String uid = mAuth.getCurrentUser().getUid();

                    DatabaseReference userRef = database.getReference("Users").child(uid);

                    HashMap<String, String> userInfo = new HashMap<>();
                    userInfo.put("name", username);
                    userInfo.put("email", email);
                    userInfo.put("phone", "");
                    userInfo.put("address", "");
                    userInfo.put("profileImageUrl", "");

                    userRef.setValue(userInfo).addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            Log.i(TAG, "Data pengguna berhasil disimpan ke database.");
                            Toast.makeText(SignupActivity.this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show();

                            // ======================================================
                            // === SOLUSI UNTUK MEMBERSIHKAN KERANJANG LAMA ===
                            // ======================================================
                            ManagmentCart managmentCart = new ManagmentCart(SignupActivity.this);
                            managmentCart.clearCart();
                            Log.d(TAG, "Keranjang dari sesi lama telah dihapus.");
                            // ======================================================

                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Gagal menyimpan data pengguna.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Log.w(TAG, "Pendaftaran gagal: ", task.getException());
                    Toast.makeText(SignupActivity.this, "Pendaftaran gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        binding.loginTxt.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });
    }
}