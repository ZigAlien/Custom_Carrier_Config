package com.example.customcarrierconfigsample;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {
    private TextView carrierPrivs_text;
    private TextView feedback_text;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String filename = App.getContext().getResources().getString(R.string.carrierFileName);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        carrierPrivs_text = (TextView) findViewById(R.id.carrierPrivs_text);
        feedback_text = (TextView) findViewById(R.id.feedback_text);

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String carrierPrivsDisplay = "Has carrier privs: " + tm.hasCarrierPrivileges();
        carrierPrivs_text.setText(carrierPrivsDisplay);
        if (tm.hasCarrierPrivileges()) {
            getSubId();
        }
        makeConfigFileIfFirstTime();
    }

    // Goes to new activity displaying contents of config file when button is clicked
    public void viewConfig(View view) {
        Log.d(LOG_TAG, "View config button clicked!");
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }

    // Reloads carrier config when Update Config button is clicked
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void updateConfig(View view) {
        Log.d(LOG_TAG, "Update config button clicked!");
        CharSequence text;
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String carrierPrivsDisplay = "Has carrier privs: "+tm.hasCarrierPrivileges();
        if (!tm.hasCarrierPrivileges()) {
            carrierPrivs_text.setText(carrierPrivsDisplay);
            Toast.makeText(App.getContext(), "no carrier privileges", Toast.LENGTH_SHORT).show();
            return;
        } else {
            carrierPrivs_text.setText(carrierPrivsDisplay);
            Toast.makeText(App.getContext(), "Updating config...", Toast.LENGTH_SHORT).show();
        }
        int subId = getSubId();
        System.out.println(subId == 1);
        CarrierConfigManager configManager = (CarrierConfigManager) getSystemService(CARRIER_CONFIG_SERVICE);
        configManager.notifyConfigChangedForSubId(subId);
    }

    // Gets the subId of the sim card
    // Hasn't been tested with multiple sims
    public int getSubId() {
        SubscriptionManager sm = (SubscriptionManager) getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> subInfoList = sm.getActiveSubscriptionInfoList(); // doesn't require permission if has Carrier Privileges
        StringBuilder simIds = new StringBuilder();
        for (SubscriptionInfo subscriptionInfo : subInfoList) {
            simIds.append(subscriptionInfo.getDisplayName()).append("\n");
            simIds.append("subId: ").append(subscriptionInfo.getSubscriptionId()).append("\n");
            simIds.append("Sim slot: ").append(subscriptionInfo.getSimSlotIndex()).append("\n");
        }
        feedback_text.setText(simIds.toString());
        return subInfoList.get(0).getSubscriptionId();
    }

    private void makeConfigFileIfFirstTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("firstTime", false)) {
            // run your one time code
            makeConfigFile();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }
    }

    private void makeConfigFile() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open(filename), "UTF-8"));
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
                outputStream = App.getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(text.getBytes());
                outputStream.close();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    // Checks whether app has READ_PHONE permission
    private boolean hasReadPhoneStatePermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            feedback_text.setText("May need to grant phone and storage permissions in Settings.");
            return false;
        }
        return true;
    }

}