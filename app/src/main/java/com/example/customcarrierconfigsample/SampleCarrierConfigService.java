package com.example.customcarrierconfigsample;

import android.app.Service;
import android.os.PersistableBundle;
import android.service.carrier.CarrierIdentifier;
import android.service.carrier.CarrierService;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SampleCarrierConfigService extends CarrierService {
    private static final String TAG = "SampleCarrierConfigServ";

    public SampleCarrierConfigService() {
        Log.d(TAG, "Service created");
    }

    private final String carrierFilename = App.getContext().getResources().getString(R.string.carrierFileName);

    @Override
    public PersistableBundle onLoadConfig(CarrierIdentifier id) {
        Log.d(TAG, "Config being fetched");
        PersistableBundle config = new PersistableBundle();
        loadConfigFromXml(config);
        Log.d(TAG, "Config completed");
        return config;
    }

    public void loadConfigFromXml(PersistableBundle config) {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            // InputStream is = getAssets().open(carrierFilename);
            FileInputStream fis = App.getContext().openFileInput(carrierFilename);
            InputStreamReader is = new InputStreamReader(fis, StandardCharsets.UTF_8);
            parser.setInput(is);
            processParsing(config, parser);
            is.close();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public void processParsing(PersistableBundle config, XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<CarrierStringArray> carrierStringArrays = new ArrayList<>();
        int eventType = parser.getEventType();
        CarrierStringArray currArray = null;
        int numArrayItems = 0;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String curr = null;

            if (eventType == XmlPullParser.START_TAG) {
                curr = parser.getName();

                if ("boolean".equals(curr)) {
                    String name = parser.getAttributeValue(null, "name");
                    String value = parser.getAttributeValue(null, "value");
                    config.putBoolean(name, Boolean.parseBoolean(value));
                } else if ("int".equals(curr)) {
                    String name = parser.getAttributeValue(null, "name");
                    String value = parser.getAttributeValue(null, "value");
                    config.putInt(name, Integer.parseInt(value));
                } else if ("string".equals(curr)) {
                    String name = parser.getAttributeValue(null, "name");
                    String value = parser.getAttributeValue(null, "value");
                    if (value == null) {
                        value = parser.nextText();
                    }
                    config.putString(name, value);
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
        for (CarrierStringArray array : carrierStringArrays) {
            config.putStringArray(array.name, array.items);
        }
    }

}
