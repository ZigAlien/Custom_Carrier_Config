package com.example.customcarrierconfigsample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileMethods {
    private static final String carrierFilename = App.getContext().getResources().getString(R.string.carrierFileName);

    // Writes config file to app internal storage only the first time the app is used
    public static void makeConfigFileIfFirstTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        if(!prefs.getBoolean("firstTime", false)) {
            makeConfigFile();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.apply();
        }
    }

    // Writes config file from assets to a file in the app's internal storage
    public static void makeConfigFile() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    App.getContext().getAssets().open(carrierFilename), "UTF-8"));
            String line;
            StringBuilder builder = new StringBuilder();
            int i = 0;

            while ((line = br.readLine()) != null) {
                builder.append(line).append("\n");
            }
            br.close();
            String text = builder.toString();
            FileOutputStream outputStream;
            try{
                outputStream = App.getContext().openFileOutput(carrierFilename, Context.MODE_PRIVATE);
                outputStream.write(text.getBytes());
                outputStream.close();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}
