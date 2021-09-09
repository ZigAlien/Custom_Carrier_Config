package com.example.customcarrierconfigsample;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/*
Main Activity.
Shows subscription info and whether app has carrier privileges granted.
Allows user to update carrier config settings or go to view config settings screen.
 */
@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {
    private TextView carrierPrivs_text;
    private TextView sim_info;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        carrierPrivs_text = (TextView) findViewById(R.id.carrierPrivs_text);
        sim_info = (TextView) findViewById(R.id.sim_info);

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String carrierPrivsDisplay = "Has carrier privileges: " + tm.hasCarrierPrivileges();
        carrierPrivs_text.setText(carrierPrivsDisplay);
        if (tm.hasCarrierPrivileges() || hasReadPhoneStatePermissions()) {
            getSubId();
        }
        FileMethods.makeConfigFileIfFirstTime();
    }

    // Goes to new activity displaying contents of config file when button is clicked
    public void viewConfig(View view) {
        Log.d(LOG_TAG, "View config button clicked!");
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }

    // Reloads carrier config when Update Config button is clicked
    // Does nothing if app has no carrier privileges
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void updateConfig(View view) {
        Log.d(LOG_TAG, "Update config button clicked!");
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String carrierPrivsDisplay = "Has carrier privileges: "+tm.hasCarrierPrivileges();
        if (!tm.hasCarrierPrivileges()) {
            carrierPrivs_text.setText(carrierPrivsDisplay);
            Toast.makeText(App.getContext(), "No carrier privileges", Toast.LENGTH_SHORT).show();
            return;
        } else {
            carrierPrivs_text.setText(carrierPrivsDisplay);
            Toast.makeText(App.getContext(), "Updating config...", Toast.LENGTH_SHORT).show();
        }
        int subId = getSubId();
        CarrierConfigManager configManager = (CarrierConfigManager) getSystemService(CARRIER_CONFIG_SERVICE);
        configManager.notifyConfigChangedForSubId(subId);
    }

    // Returns the process subId and updates the sim info displayed
    public int getSubId() {
        SubscriptionManager sm = (SubscriptionManager) getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
        // doesn't require permission if has Carrier Privileges
        @SuppressLint("MissingPermission") List<SubscriptionInfo> subInfoList = sm.getActiveSubscriptionInfoList();
        if (subInfoList.isEmpty()) {
            String text = "No Sim";
            sim_info.setText(text);
            return -1;
        }
        StringBuilder simIds = new StringBuilder();
        for (SubscriptionInfo subscriptionInfo : subInfoList) {
            simIds.append(subscriptionInfo.getDisplayName()).append("\n");
            simIds.append("subId: ").append(subscriptionInfo.getSubscriptionId()).append("\n");
            simIds.append("Sim slot: ").append(subscriptionInfo.getSimSlotIndex()).append("\n");
        }
        sim_info.setText(simIds.toString());
        return subInfoList.get(0).getSubscriptionId(); // assuming the currently active sim is the first element in the list
    }

    // Checks whether app has READ_PHONE permission
    private boolean hasReadPhoneStatePermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_PHONE_STATE);
        } else { return true; }
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getSubId();
                } else {
                    String text = "May need to grant phone permissions in Settings.";
                    sim_info.setText(text);
                }
            });

}