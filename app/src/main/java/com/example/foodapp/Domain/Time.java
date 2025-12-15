package com.example.foodapp.Domain;

public class Time {
    private int id;
    private String time;

    public Time() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return time; // Ini penting agar teks waktu tampil di spinner
    }
}
