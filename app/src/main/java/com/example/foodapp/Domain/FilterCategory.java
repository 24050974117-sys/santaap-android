package com.example.foodapp.Domain;

public class FilterCategory {
    private int id;
    private String name;

    public FilterCategory() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name; // Ini penting agar nama tampil di Spinner
    }
}