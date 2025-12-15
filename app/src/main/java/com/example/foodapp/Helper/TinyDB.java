package com.example.foodapp.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

// =======================================================
//          IMPORT YANG PERLU DITAMBAHKAN
// =======================================================
import com.example.foodapp.Domain.Foods;
import com.google.gson.Gson;
// =======================================================

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class TinyDB {

    private SharedPreferences preferences;
    private String DEFAULT_APP_IMAGEDATA_DIRECTORY;
    private File mFolder = null;
    private static final String PREFERENCES_NAME = "TinyDB_Preferences";

    public TinyDB(Context appContext) {
        preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public Bitmap getImage(String path) {
        Bitmap bitmapFromPath = null;
        try {
            bitmapFromPath = BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmapFromPath;
    }

    private String getSavedImagePath(String key) {
        return preferences.getString(key, "");
    }

    private String putImage(String theFolder, String theImageName, Bitmap theBitmap) {
        if (theFolder == null || theImageName == null || theBitmap == null)
            return null;

        this.DEFAULT_APP_IMAGEDATA_DIRECTORY = theFolder;
        File mFolder = new File(Environment.getExternalStorageDirectory(), DEFAULT_APP_IMAGEDATA_DIRECTORY);

        if (!mFolder.exists()) {
            if (!mFolder.mkdirs()) {
                Log.e("Error", "Failed to create directory");
                return null;
            }
        }

        File file = new File(mFolder.getAbsolutePath(), theImageName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            theBitmap.compress(CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean putImageWithFullPath(String fullPath, Bitmap theBitmap) {
        if (fullPath == null || theBitmap == null)
            return false;

        try (FileOutputStream fOut = new FileOutputStream(fullPath)) {
            theBitmap.compress(CompressFormat.PNG, 100, fOut);
            fOut.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    public long getLong(String key) {
        return preferences.getLong(key, 0L);
    }

    public String getString(String key) {
        return preferences.getString(key, "");
    }

    public double getDouble(String key) {
        String number = getString(key);
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public float getFloat(String key) {
        return preferences.getFloat(key, 0f);
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public ArrayList<Integer> getListInt(String key) {
        String[] myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚");
        ArrayList<String> arrayToList = new ArrayList<>(Arrays.asList(myList));
        ArrayList<Integer> newList = new ArrayList<>();

        for (String item : arrayToList) {
            if (!item.isEmpty())
                newList.add(Integer.parseInt(item));
        }
        return newList;
    }

    public ArrayList<Long> getListLong(String key) {
        String[] myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚");
        ArrayList<String> arrayToList = new ArrayList<>(Arrays.asList(myList));
        ArrayList<Long> newList = new ArrayList<>();

        for (String item : arrayToList) {
            if (!item.isEmpty())
                newList.add(Long.parseLong(item));
        }
        return newList;
    }

    public ArrayList<Double> getListDouble(String key) {
        String[] myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚");
        ArrayList<String> arrayToList = new ArrayList<>(Arrays.asList(myList));
        ArrayList<Double> newList = new ArrayList<>();

        for (String item : arrayToList) {
            if (!item.isEmpty())
                newList.add(Double.parseDouble(item));
        }
        return newList;
    }

    public ArrayList<String> getListString(String key) {
        return new ArrayList<>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    public ArrayList<Boolean> getListBoolean(String key) {
        ArrayList<String> myList = getListString(key);
        ArrayList<Boolean> newList = new ArrayList<>();

        for (String item : myList) {
            if (item.equals("true")) {
                newList.add(true);
            } else {
                newList.add(false);
            }
        }
        return newList;
    }

    public <T> ArrayList<T> getListObject(String key, Class<T> mClass) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = getListString(key);
        ArrayList<T> objects = new ArrayList<>();

        for (String jObjString : objStrings) {
            T value = gson.fromJson(jObjString, mClass);
            objects.add(value);
        }
        return objects;
    }

    // Custom method to get list of Foods without specifying class
    public ArrayList<Foods> getListObject(String key) {
        Gson gson = new Gson();
        ArrayList<String> objStrings = getListString(key);
        ArrayList<Foods> objects = new ArrayList<>();
        for (String jObjString : objStrings) {
            Foods value = gson.fromJson(jObjString, Foods.class);
            objects.add(value);
        }
        return objects;
    }

    public void putInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public void putLong(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    public void putDouble(String key, double value) {
        putString(key, String.valueOf(value));
    }

    public void putFloat(String key, float value) {
        preferences.edit().putFloat(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    public void putListInt(String key, ArrayList<Integer> integerList) {
        Integer[] myIntList = integerList.toArray(new Integer[0]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myIntList)).apply();
    }

    public void putListLong(String key, ArrayList<Long> longList) {
        Long[] myLongList = longList.toArray(new Long[0]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myLongList)).apply();
    }

    public void putListDouble(String key, ArrayList<Double> doubleList) {
        Double[] myDoubleList = doubleList.toArray(new Double[0]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myDoubleList)).apply();
    }

    public void putListString(String key, ArrayList<String> stringList) {
        String[] myStringList = stringList.toArray(new String[0]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    public void putListBoolean(String key, ArrayList<Boolean> boolList) {
        ArrayList<String> newBoolList = new ArrayList<>();
        for (Boolean item : boolList) {
            if (item) {
                newBoolList.add("true");
            } else {
                newBoolList.add("false");
            }
        }
        putListString(key, newBoolList);
    }

    public void putListObject(String key, ArrayList<Foods> objArray) {
        Gson gson = new Gson();
        ArrayList<String> objStrings = new ArrayList<>();
        for (Foods obj : objArray) {
            objStrings.add(gson.toJson(obj));
        }
        putListString(key, objStrings);
    }

    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    public void registerOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}

