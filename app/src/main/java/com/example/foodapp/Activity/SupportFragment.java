package com.example.foodapp.Activity; // <-- Pastikan package Anda benar

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodapp.databinding.FragmentSupportBinding;

public class SupportFragment extends Fragment {

    private FragmentSupportBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSupportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setClickListeners();
    }

    private void setClickListeners() {

        // --- PENTING: GANTI DENGAN DATA ANDA ---
        String whatsappNumber = "62881026361181"; // Awali dengan 62, tanpa + atau 0
        String emailAddress = "muhammadhestiavindaffa@gmail.com";
        String emailSubject = "Bantuan Aplikasi FoodApp [Pengguna: Daffa]";
        // ------------------------------------------

        // Listener untuk tombol WhatsApp
        binding.whatsappBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + whatsappNumber));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Tampilkan pesan error jika WhatsApp tidak terinstall
                Toast.makeText(getContext(), "WhatsApp tidak terinstall.", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener untuk tombol Email
        binding.emailBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // Hanya buka aplikasi email
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);

                // Cek apakah ada aplikasi email
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Tidak ada aplikasi email yang terinstall.", Toast.LENGTH_SHORT).show();
                }
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "Gagal membuka aplikasi email.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}