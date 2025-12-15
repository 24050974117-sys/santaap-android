package com.example.foodapp.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy; // <-- IMPORT BARU
import com.example.foodapp.R;
import com.example.foodapp.databinding.FragmentProfileBinding;
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

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private DatabaseReference userRef;
    private DatabaseReference ordersRef;
    private DatabaseReference favoritesRef;

    private ValueEventListener userListener;
    private ValueEventListener ordersListener;
    private ValueEventListener favoritesListener;

    private ActivityResultLauncher<String> mGetContent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && getContext() != null) {
                        uploadProfileImage(uri);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            ordersRef = FirebaseDatabase.getInstance().getReference("Orders").child(uid);
            favoritesRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Favorites");

            loadUserInfo();
            loadUserStats();
        } else {
            goToLogin();
        }

        setClickListeners();
    }

    private void loadUserInfo() {
        if (currentUser.getEmail() != null) {
            binding.emailTxt.setText(currentUser.getEmail());
        }

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null || binding == null) {
                    return;
                }

                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    binding.nameTxt.setText(name != null ? name : "Set Your Name");

                    String phone = snapshot.child("phone").getValue(String.class);
                    binding.phoneTxt.setText(phone != null && !phone.isEmpty() ? phone : "Atur No. Telepon Anda");

                    String address = snapshot.child("address").getValue(String.class);
                    binding.addressTxt.setText(address != null && !address.isEmpty() ? address : "Atur Alamat Anda");

                    String profileImageName = snapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageName != null && !profileImageName.isEmpty()) {
                        if (getContext() == null) return;
                        File imageFile = new File(getContext().getFilesDir(), profileImageName);

                        if (imageFile.exists()) {
                            // --- PERBAIKAN CACHE DI SINI ---
                            Glide.with(getContext())
                                    .load(imageFile) // Load dari File
                                    .placeholder(R.drawable.sample_avatar)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Jangan simpan di disk cache
                                    .skipMemoryCache(true) // Jangan simpan di memori cache
                                    .into(binding.profileImage);
                        } else {
                            binding.profileImage.setImageResource(R.drawable.sample_avatar);
                        }
                    } else {
                        binding.profileImage.setImageResource(R.drawable.sample_avatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Gagal memuat data profil.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        userRef.addValueEventListener(userListener);
    }

    private void loadUserStats() {
        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding != null) {
                    long orderCount = snapshot.getChildrenCount();
                    binding.totalOrdersTxt.setText(String.valueOf(orderCount));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        ordersRef.addValueEventListener(ordersListener);

        favoritesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding != null) {
                    long favCount = snapshot.getChildrenCount();
                    binding.totalFavoritesTxt.setText(String.valueOf(favCount));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        favoritesRef.addValueEventListener(favoritesListener);
    }

    private void setClickListeners() {
        binding.logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            goToLogin();
        });

        binding.editProfileBtn.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            }
        });

        binding.changePasswordBtn.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getEmail() != null) {
                String email = currentUser.getEmail();
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (getContext() == null) return;
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Link ganti password telah dikirim ke " + email, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Gagal mengirim link: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Tidak dapat menemukan email pengguna.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.profileImage.setOnClickListener(v -> {
            openGalleryForEdit();
        });
    }

    private void openGalleryForEdit() {
        mGetContent.launch("image/*");
    }

    private void uploadProfileImage(Uri imageUri) {
        if (currentUser == null || getContext() == null) return;
        Toast.makeText(getContext(), "Menyimpan foto...", Toast.LENGTH_SHORT).show();

        String fileName = saveImageToInternalStorage(imageUri);

        if (fileName != null) {
            userRef.child("profileImageUrl").setValue(fileName)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Foto profil diperbarui", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Gagal menyimpan data foto", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Gagal menyimpan file foto", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImageToInternalStorage(Uri uri) {
        if (getContext() == null) return null;
        String fileName = currentUser.getUid() + ".jpg";
        File outputFile = new File(getContext().getFilesDir(), fileName);

        try (InputStream in = getContext().getContentResolver().openInputStream(uri);
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

    private void goToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
        if (ordersRef != null && ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
        }
        if (favoritesRef != null && favoritesListener != null) {
            favoritesRef.removeEventListener(favoritesListener);
        }

        binding = null;
    }
}