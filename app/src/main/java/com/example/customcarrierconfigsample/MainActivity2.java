package com.example.customcarrierconfigsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    private TextView xml_text;

    private String carrierFilename = App.getContext().getResources().getString(R.string.carrierFileName);
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        xml_text = (TextView) findViewById(R.id.xml_text);

        parseXML();
    }

    private void parseXML() {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            //InputStream is = getAssets().open(carrierFilename);
            FileInputStream fis = App.getContext().openFileInput(carrierFilename);
            InputStreamReader is = new InputStreamReader(fis, StandardCharsets.UTF_8);
            parser.setInput(is);
            processParsing(parser);
            is.close();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public CarrierKeyPair processPair(XmlPullParser parser) throws IOException, XmlPullParserException {
        CarrierKeyPair currPair = new CarrierKeyPair();
        String name = parser.getAttributeValue(null, "name");
        if (name != null) {
            currPair.name = "KEY_" + name.toUpperCase();
        }
        currPair.value = parser.getAttributeValue(null, "value");
        return currPair;
    }

    private void processParsing(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<CarrierKeyPair> carrierBools = new ArrayList<>();
        ArrayList<CarrierKeyPair> carrierStrings = new ArrayList<>();
        ArrayList<CarrierKeyPair> carrierInts = new ArrayList<>();
        ArrayList<CarrierStringArray> carrierStringArrays = new ArrayList<>();
        int eventType = parser.getEventType();
        CarrierKeyPair currPair = null;
        CarrierStringArray currArray = null;
        int numArrayItems = 0;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String curr = null;

            if (eventType == XmlPullParser.START_TAG) {
                curr = parser.getName();
                if ("boolean".equals(curr)) {
                    currPair = new CarrierKeyPair();
                    currPair.name = parser.getAttributeValue(null, "name");
                    currPair.value = parser.getAttributeValue(null, "value");
                    carrierBools.add(currPair);
                } else if ("int".equals(curr)) {
                    currPair = new CarrierKeyPair();
                    currPair.name = parser.getAttributeValue(null, "name");
                    currPair.value = parser.getAttributeValue(null, "value");
                    carrierInts.add(currPair);
                } else if ("string".equals(curr)) {
                    currPair = new CarrierKeyPair();
                    currPair.name = parser.getAttributeValue(null, "name");
                    currPair.value = parser.getAttributeValue(null, "value");
                    if (currPair.value == null) {
                        currPair.value = parser.nextText();
                    }
                    carrierStrings.add(currPair);
                } else if ("string-array".equals(curr)) {
                    currArray = new CarrierStringArray();
                    currArray.name = parser.getAttributeValue(null, "name");
                    int size = Integer.parseInt(parser.getAttributeValue(null, "num"));
                    currArray.items = new String[size];
                    numArrayItems = 0;
                    carrierStringArrays.add(currArray);
                } else if ("item".equals(curr) && currArray != null) {
                    String item = parser.getAttributeValue(null, "value");
                    currArray.items[numArrayItems++] = item;
                }
            }
            eventType = parser.next();
        }
        printPairs(carrierBools, carrierInts, carrierStrings, carrierStringArrays);
    }


    private void printPairs(ArrayList<CarrierKeyPair> carrierBools, ArrayList<CarrierKeyPair> carrierInts,
                            ArrayList<CarrierKeyPair> carrierStrings, ArrayList<CarrierStringArray> carrierStringArrays) {
        StringBuilder builder = new StringBuilder();

        for (CarrierKeyPair pair : carrierBools) {
            builder.append(pair.name + ": " + pair.value + "\n");
        }
        builder.append("\n");

        for (CarrierKeyPair pair : carrierInts) {
            builder.append(pair.name + ": " + pair.value + "\n");
        }
        builder.append("\n");

        for (CarrierKeyPair pair : carrierStrings) {
            builder.append(pair.name + ": " + pair.value + "\n");
        }
        builder.append("\n");

        for (CarrierStringArray array : carrierStringArrays) {
            builder.append(array.name + ":\n");
            for (String item : array.items) {
                builder.append("  item: " + item + "\n");
            }
        }

        xml_text.setText(builder.toString());
    }

    public void goToActivity3(View view) {
        Log.d(LOG_TAG, "Next button clicked!");
        Intent intent = new Intent(this, MainActivity3.class);
        startActivity(intent);
    }
}