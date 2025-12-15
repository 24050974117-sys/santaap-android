package com.example.foodapp.Helper;

import android.content.Context;
import android.widget.Toast;
import com.example.foodapp.Domain.Foods;
import java.util.ArrayList;

// Kelas ini bertanggung jawab untuk semua logika keranjang belanja
public class ManagmentCart {
    private Context context;
    private TinyDB tinyDB;

    public ManagmentCart(Context context) {
        this.context = context;
        this.tinyDB = new TinyDB(context); // Menggunakan TinyDB untuk menyimpan data di HP
    }

    // Fungsi untuk memasukkan makanan ke keranjang
    public void insertFood(Foods item) {
        ArrayList<Foods> listFood = getListCart();
        boolean existAlready = false;
        int n = 0;
        for (int i = 0; i < listFood.size(); i++) {
            // Cek apakah makanan dengan nama yang sama sudah ada di keranjang
            if (listFood.get(i).getTitle().equals(item.getTitle())) {
                existAlready = true;
                n = i;
                break;
            }
        }

        if (existAlready) {
            // Jika sudah ada, cukup update jumlahnya
            listFood.get(n).setNumberInCart(item.getNumberInCart());
        } else {
            // Jika belum ada, tambahkan item baru
            listFood.add(item);
        }

        // Simpan daftar keranjang yang sudah diupdate ke TinyDB
        tinyDB.putListObject("CartList", listFood);
        Toast.makeText(context, "Ditambahkan ke Keranjang Anda", Toast.LENGTH_SHORT).show();
    }

    // Fungsi untuk mengambil semua item dari keranjang
    public ArrayList<Foods> getListCart() {
        return tinyDB.getListObject("CartList");
    }

    // Fungsi untuk menambah jumlah item (tombol +)
    public void plusNumberFood(ArrayList<Foods> listFood, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        listFood.get(position).setNumberInCart(listFood.get(position).getNumberInCart() + 1);
        tinyDB.putListObject("CartList", listFood); // Simpan perubahan
        changeNumberItemsListener.changed(); // Beri tahu halaman Cart untuk update total harga
    }

    // Fungsi untuk mengurangi jumlah item (tombol -)
    public void minusNumberFood(ArrayList<Foods> listfood, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        if (listfood.get(position).getNumberInCart() == 1) {
            // Jika jumlahnya 1 dan dikurangi, hapus item dari keranjang
            listfood.remove(position);
        } else {
            // Jika lebih dari 1, kurangi jumlahnya
            listfood.get(position).setNumberInCart(listfood.get(position).getNumberInCart() - 1);
        }
        tinyDB.putListObject("CartList", listfood); // Simpan perubahan
        changeNumberItemsListener.changed(); // Beri tahu halaman Cart untuk update total harga
    }

    // Fungsi untuk menghitung total harga semua item di keranjang
    public Double getTotalFee() {
        ArrayList<Foods> listfood = getListCart();
        double fee = 0;
        for (int i = 0; i < listfood.size(); i++) {
            fee = fee + (listfood.get(i).getPrice() * listfood.get(i).getNumberInCart());
        }
        return fee;
    }
    public void clearCart() {
        // Menghapus daftar keranjang dari penyimpanan TinyDB
        tinyDB.remove("CartList");
    }
}

