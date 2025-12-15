package com.example.foodapp.Activity;

import android.Manifest; // <-- IMPORT BARU
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager; // <-- IMPORT BARU
import android.location.Address; // <-- IMPORT BARU
import android.location.Geocoder; // <-- IMPORT BARU
import android.location.Location; // <-- IMPORT BARU
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // <-- IMPORT BARU

import com.bumptech.glide.Glide;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityEditProfileBinding;
import com.google.android.gms.location.FusedLocationProviderClient; // <-- IMPORT BARU
import com.google.android.gms.location.LocationServices; // <-- IMPORT BARU
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List; // <-- IMPORT BARU
import java.util.Locale; // <-- IMPORT BARU
import java.util.Map;

public class EditProfileActivity extends BaseActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private Uri imageUri;

    // --- VARIABEL BARU UNTUK LOKASI ---
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private String currentAddress = ""; // Untuk menyimpan alamat dari GPS
    // ----------------------------------

    private ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            goToLogin();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // Inisialisasi Klien Lokasi
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // --- Inisialisasi Launcher untuk Izin Lokasi ---
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Izin diberikan, ambil lokasi
                fetchLastLocation();
            } else {
                // Izin ditolak
                Toast.makeText(this, "Izin lokasi ditolak. Alamat tidak bisa diambil.", Toast.LENGTH_SHORT).show();
            }
        });

        // Inisialisasi peluncur galeri
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        binding.profileImage.setImageURI(imageUri); // Tampilkan gambar baru
                    }
                });

        loadCurrentUserData();
        setClickListeners();
    }

    private void loadCurrentUserData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class); // Alamat dari Firebase
                    String profileImageName = snapshot.child("profileImageUrl").getValue(String.class);

                    binding.nameEdt.setText(name);
                    binding.phoneEdt.setText(phone);

                    // Tampilkan alamat yang tersimpan di Firebase
                    if (address != null && !address.isEmpty()) {
                        binding.addressTxt.setText(address);
                        currentAddress = address; // Simpan sebagai alamat saat ini
                    } else {
                        binding.addressTxt.setText("Tekan tombol untuk mendapatkan lokasi...");
                    }

                    // Logika "Cara Lokal" untuk memuat gambar
                    if (profileImageName != null && !profileImageName.isEmpty()) {
                        File imageFile = new File(getFilesDir(), profileImageName);
                        if (imageFile.exists()) {
                            Glide.with(EditProfileActivity.this)
                                    .load(imageFile) // Load dari File
                                    .placeholder(R.drawable.sample_avatar)
                                    .into(binding.profileImage);
                        } else {
                            binding.profileImage.setImageResource(R.drawable.sample_avatar);
                        }
                    } else {
                        binding.profileImage.setImageResource(R.drawable.sample_avatar);
                    }
                }
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfileActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setClickListeners() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.profileImage.setOnClickListener(v -> openGallery());

        // --- TOMBOL LOKASI BARU ---
        binding.getLocationBtn.setOnClickListener(v -> {
            checkLocationPermissionAndFetch();
        });

        // --- TOMBOL SIMPAN DIPERBARUI ---
        binding.saveBtn.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);

            String fileName = null;
            if (imageUri != null) {
                fileName = saveImageToInternalStorage(imageUri);
                if (fileName == null) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // Simpan data (termasuk NAMA FILE jika ada DAN alamat baru)
            saveDataToDatabase(fileName);
        });
    }

    // --- FUNGSI BARU: Memeriksa Izin Lokasi ---
    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Izin sudah ada, langsung ambil lokasi
            fetchLastLocation();
        } else {
            // Izin belum ada, minta ke pengguna
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    // --- FUNGSI BARU: Mengambil Lokasi GPS ---
    private void fetchLastLocation() {
        // Cek lagi (walaupun harusnya sudah aman)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Mengambil lokasi...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (location != null) {
                        // Lokasi didapat, ubah jadi alamat
                        getAddressFromLocation(location);
                    } else {
                        Toast.makeText(this, "Gagal mendapatkan lokasi. Coba lagi.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error lokasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- FUNGSI BARU: Mengubah Koordinat jadi Alamat ---
    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Bangun alamat lengkap
                String fullAddress = address.getAddressLine(0);

                // Set teks di TextView
                binding.addressTxt.setText(fullAddress);
                binding.addressTxt.setTextColor(getResources().getColor(R.color.blue_grey_800)); // Ubah warna teks
                currentAddress = fullAddress; // Simpan untuk disimpan
            } else {
                Toast.makeText(this, "Alamat tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error Geocoder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void openGallery() {
        mGetContent.launch("image/*");
    }

    private String saveImageToInternalStorage(Uri uri) {
        String fileName = currentUser.getUid() + ".jpg";
        File outputFile = new File(getFilesDir(), fileName);

        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(outputFile)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- FUNGSI SIMPAN DIPERBARUI ---
    private void saveDataToDatabase(String fileName) {
        String newName = binding.nameEdt.getText().toString().trim();
        String newPhone = binding.phoneEdt.getText().toString().trim();
        // Ambil alamat dari TextView (yang sudah diisi oleh GPS)
        String newAddress = binding.addressTxt.getText().toString();

        if (newName.isEmpty()) {
            binding.nameEdt.setError("Nama tidak boleh kosong");
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("phone", newPhone);

        // Hanya simpan jika alamat BUKAN teks placeholder
        if (!newAddress.equals("Tekan tombol untuk mendapatkan lokasi...")) {
            updates.put("address", newAddress);
        }

        if (fileName != null) {
            updates.put("profileImageUrl", fileName);
        }

        userRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}