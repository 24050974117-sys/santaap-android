package com.example.foodapp.Domain;

import java.io.Serializable;

// Serializable memungkinkan objek ini untuk dikirim antar Activity (misal ke halaman detail atau keranjang)
public class Foods implements Serializable {
    // Variabel utama untuk data makanan
    private int categoryId;
    private String description;
    private boolean bestFood;
    private String imagePath;
    private double price;
    private double star;
    private int timeValue;
    private String title;

    // Variabel untuk fitur keranjang belanja
    private int numberInCart;

    // Variabel untuk fitur filter
    private int locationId;
    private int timeId;
    private int priceId;

    // Constructor kosong ini wajib ada untuk Firebase
    public Foods() {
    }

    // --- Getters and Setters untuk semua variabel ---

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBestFood() {
        return bestFood;
    }

    public void setBestFood(boolean bestFood) {
        this.bestFood = bestFood;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getStar() {
        return star;
    }

    public void setStar(double star) {
        this.star = star;
    }

    public int getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(int timeValue) {
        this.timeValue = timeValue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumberInCart() {
        return numberInCart;
    }

    public void setNumberInCart(int numberInCart) {
        this.numberInCart = numberInCart;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getTimeId() {
        return timeId;
    }

    public void setTimeId(int timeId) {
        this.timeId = timeId;
    }

    public int getPriceId() {
        return priceId;
    }

    public void setPriceId(int priceId) {
        this.priceId = priceId;
    }
}

