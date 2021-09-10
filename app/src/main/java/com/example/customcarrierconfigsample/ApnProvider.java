package com.example.customcarrierconfigsample;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.R)
public class ApnProvider {
    static final String LOG_TAG = ApnProvider.class.getSimpleName();
    final Uri APN_TABLE_URI = Telephony.Carriers.CONTENT_URI;
    final Uri SIM_APN_URI = Telephony.Carriers.SIM_APN_URI;
    final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");


    public void insertApnFromXml() {
        ContentResolver contentResolver = App.getContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlparser = parserFactory.newPullParser();
            InputStream is = App.getContext().getAssets().open("default-apn.xml");
            xmlparser.setInput(is, null);
            processApnParsing(contentValues, xmlparser);
            is.close();
            int id = insertApn(contentValues, contentResolver);
            setPreferredAPN(id);;
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    private int insertApn(ContentValues contentValues, ContentResolver contentResolver) {
        int id = -1;
        Cursor c = null;

        // for some reason this is always null
        Uri newRow = contentResolver.insert(APN_TABLE_URI, contentValues);

        if(newRow != null){
            Log.d(LOG_TAG, "Inserting APN into table...");
            c = contentResolver.query(newRow, null, null, null, null);

            //obtain the APN id
            int idindex = c.getColumnIndex("_id");
            c.moveToFirst();
            id = c.getShort(idindex);
        }
        if(c !=null ) c.close();
        return id;
    }

    //Takes the ID of the new record generated in InsertAPN and sets that particular record the default preferred APN configuration
    public boolean setPreferredAPN(int id){

        //If the id is -1, that means the record was found in the APN table before insertion, thus, no action required
        if (id == -1){
            Log.d(LOG_TAG, "APN already configured, no changes made.");
            return false;
        }

        boolean res = false;
        ContentResolver resolver = App.getContext().getContentResolver();
        ContentValues values = new ContentValues();

        values.put("apn_id", id);
        try{
            resolver.update(PREFERRED_APN_URI, values, null, null);
            Cursor c = resolver.query(PREFERRED_APN_URI, new String[]{"name", "apn"}, "_id="+id, null, null);
            if(c != null){
                Log.d(LOG_TAG, "Updating preferred APN...");
                res = true;
                c.close();
            }
        }
        catch (SQLException e){}
        return res;
    }

    public static void processApnParsing(ContentValues contentValues, XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String curr = null;

            if (eventType == XmlPullParser.START_TAG) {
                curr = parser.getName();

                if("string".equals(curr)) {
                    String name = parser.getAttributeValue(null, "name");
                    String value= parser.getAttributeValue(null, "value");
                    contentValues.put(name, value);
                }
                else if ("boolean".equals(curr)) {
                    String name = parser.getAttributeValue(null, "name");
                    String value= parser.getAttributeValue(null, "value");
                    contentValues.put(name, Boolean.valueOf(value));
                }
                else if ("int".equals(curr)) {
                    String name = parser.getAttributeValue(null, "name");
                    String value= parser.getAttributeValue(null, "value");
                    contentValues.put(name, value);
                }
                else if ("apn".equals(curr)){
                    Log.d(LOG_TAG, "Loading APN from XML...");
                }
            }
            eventType = parser.next();
        }
    }
}
