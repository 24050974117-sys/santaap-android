package com.example.foodapp.Domain;

public class Promo {
    private String title;
    private String subtitle;
    private String picUrl;
    private int bgColor;
    private int titleColor;
    private int subtitleColor;
    private int btnColor;
    private int btnTextColor;

    // --- TAMBAHAN BARU ---
    private String foodId; // ID makanan yang akan ditambahkan (misal: "food_1")
    private int quantityToAdd; // Jumlah yang akan ditambahkan
    // ----------------------

    // Kosongkan constructor untuk Firebase (jika perlu)
    public Promo() {
    }

    // Constructor lama Anda (sudah dimodifikasi)
    public Promo(String title, String subtitle, String picUrl, int bgColor, int titleColor, int subtitleColor, int btnColor, int btnTextColor, String foodId, int quantityToAdd) {
        this.title = title;
        this.subtitle = subtitle;
        this.picUrl = picUrl;
        this.bgColor = bgColor;
        this.titleColor = titleColor;
        this.subtitleColor = subtitleColor;
        this.btnColor = btnColor;
        this.btnTextColor = btnTextColor;

        // --- TAMBAHAN BARU ---
        this.foodId = foodId;
        this.quantityToAdd = quantityToAdd;
        // ----------------------
    }

    // ... (Getter & Setter untuk title, subtitle, picUrl, dan warna tetap ada) ...

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getPicUrl() { return picUrl; }
    public void setPicUrl(String picUrl) { this.picUrl = picUrl; }
    public int getBgColor() { return bgColor; }
    public void setBgColor(int bgColor) { this.bgColor = bgColor; }
    public int getTitleColor() { return titleColor; }
    public void setTitleColor(int titleColor) { this.titleColor = titleColor; }
    public int getSubtitleColor() { return subtitleColor; }
    public void setSubtitleColor(int subtitleColor) { this.subtitleColor = subtitleColor; }
    public int getBtnColor() { return btnColor; }
    public void setBtnColor(int btnColor) { this.btnColor = btnColor; }
    public int getBtnTextColor() { return btnTextColor; }
    public void setBtnTextColor(int btnTextColor) { this.btnTextColor = btnTextColor; }

    // --- GETTER & SETTER BARU ---
    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public int getQuantityToAdd() {
        return quantityToAdd;
    }

    public void setQuantityToAdd(int quantityToAdd) {
        this.quantityToAdd = quantityToAdd;
    }
    // -----------------------------
}