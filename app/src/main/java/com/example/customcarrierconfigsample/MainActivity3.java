package com.example.customcarrierconfigsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity3 extends AppCompatActivity {

    private EditText editText;
    private final String carrierFilename = App.getContext().getResources().getString(R.string.carrierFileName);;
    private static final String LOG_TAG = MainActivity3.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        editText = (EditText) findViewById(R.id.editText);
        showFileContents();
    }

    // Show carrier config file contents inside the TextInputEditText
    public void showFileContents() {
        try {
            FileInputStream fis = App.getContext().openFileInput(carrierFilename);
            InputStreamReader is = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(is);
            String line;
            StringBuilder builder = new StringBuilder();

            while ((line = br.readLine()) != null) {
                builder.append(line).append("\n");
            }
            br.close();
            editText.setText(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // When Save button clicked, save contents of the TextInputEditText to the carrier config file
    public void saveText(View view) {
        Log.d(LOG_TAG, "Save button clicked!");
        String text = editText.getText().toString();
        FileOutputStream outputStream;
        try{
            outputStream = App.getContext().openFileOutput(carrierFilename, Context.MODE_PRIVATE);
            outputStream.write(text.getBytes());
            outputStream.close();
            Toast.makeText(App.getContext(), "Saving...", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
        showFileContents();
    }
}