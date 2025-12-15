package com.example.foodapp.Domain;

public class Price {
    private int id;
    private String price;

    public Price() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return price; // Ini penting agar rentang harga tampil di spinner
    }
}
