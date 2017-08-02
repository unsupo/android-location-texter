package com.example.jarndt.testingapp.utilities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.jarndt.testingapp.sms.SmsDeliveredReceiver;
import com.example.jarndt.testingapp.sms.SmsSentReceiver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by jarndt on 8/2/17.
 */

public class FileOptions {
    public static final String TAG = "FileOptions";
    public static Location getLocationFromFile(Context context, String fileName) {
        StringBuffer fileContent = new StringBuffer("");
        FileInputStream fis;
        boolean b = false;
        for(String s : context.fileList())
            if(fileName.equals(s))
                b = true;
        if(!b)
            return null;

        try {
            fis = context.openFileInput(fileName);
            byte[] buffer = new byte[1024];

            int n;
            while ((n = fis.read(buffer)) != -1)
                fileContent.append(new String(buffer, 0, n));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
        Location l = new Location("");
        String[] split = fileContent.toString().split(",");
        l.setAltitude(Double.parseDouble(split[2]));
        l.setLongitude(Double.parseDouble(split[1]));
        l.setLatitude(Double.parseDouble(split[0]));
        return l;
    }

    public static String writeToFile(Context context, String filename, String content, int mode){
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(filename, mode);
            outputStream.write(content.getBytes());
            outputStream.close();
            Field pathField = FileOutputStream.class.getDeclaredField("path");
            pathField.setAccessible(true);
            String path = (String) pathField.get(outputStream);
            Log.e(TAG,"writeToFile: "+path);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * BroadcastReceiver mBrSend; BroadcastReceiver mBrReceive;
     */
    private void sendSMS(Context context, String phoneNumber, String message) {
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(context, SmsSentReceiver.class), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(context, SmsDeliveredReceiver.class), 0);
        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> mSMSMessage = sms.divideMessage(message);
            for (int i = 0; i < mSMSMessage.size(); i++) {
                sentPendingIntents.add(i, sentPI);
                deliveredPendingIntents.add(i, deliveredPI);
            }
            sms.sendMultipartTextMessage(phoneNumber, null, mSMSMessage,
                    sentPendingIntents, deliveredPendingIntents);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "SMS sending failed...", Toast.LENGTH_SHORT).show();
        }

    }
}
