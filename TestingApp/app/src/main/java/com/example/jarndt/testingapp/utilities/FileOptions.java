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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jarndt on 8/2/17.
 */

public class FileOptions {
    public static final String TAG = "FileOptions";
    public static String getFileContents(Context context, String fileName) {
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
        Log.e(TAG,"getFileContents: "+fileName+"\n\t"+fileContent.toString());
        return fileContent.toString();
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
            Log.e(TAG,"writeToFile: "+path+"\n\t"+content+"\n\n"+mode);
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

    private static Gson gson;
    public static Gson getGson(){
        if(gson == null){
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Location.class, new LocationDeserializer());
            gsonBuilder.registerTypeAdapter(Location.class, new LocationSerializer());
            gson = gsonBuilder.create();
        }
        return gson;
    }
}

class LocationSerializer implements JsonSerializer<Location>
{
    public JsonElement serialize(Location t, Type type,
                                 JsonSerializationContext jsc)
    {
        JsonObject jo = new JsonObject();
        jo.addProperty("mProvider", t.getProvider());
        jo.addProperty("mAccuracy", t.getAccuracy());
        jo.addProperty("mLongitude", t.getLongitude());
        jo.addProperty("mLatitude", t.getLatitude());
        jo.addProperty("mAltitude", t.getAltitude());
        jo.addProperty("mBearing", t.getBearing());
        jo.addProperty("mBearingAccuracyDegrees", t.getBearingAccuracyDegrees());
        jo.addProperty("mSpeed",t.getSpeed());
        jo.addProperty("mElapsedRealtimeNanos",t.getElapsedRealtimeNanos());
        jo.addProperty("mSpeedAccuracyMetersPerSecond",t.getSpeedAccuracyMetersPerSecond());
        jo.addProperty("mTime",t.getTime());
        jo.addProperty("mVerticalAccuracyMeters",t.getVerticalAccuracyMeters());
        return jo;
    }

}

class LocationDeserializer implements JsonDeserializer<Location>
{
    public Location deserialize(JsonElement je, Type type,
                                JsonDeserializationContext jdc)
            throws JsonParseException
    {
        JsonObject jo = je.getAsJsonObject();
        Location l = new Location("");
        if(jo.getAsJsonPrimitive("mProvider") != null)
            l = new Location(jo.getAsJsonPrimitive("mProvider").getAsString());
        if(jo.getAsJsonPrimitive("mAccuracy") != null)
            l.setAccuracy(jo.getAsJsonPrimitive("mAccuracy").getAsFloat());
        if(jo.getAsJsonPrimitive("mLongitude") != null)
            l.setLongitude(jo.getAsJsonPrimitive("mLongitude").getAsFloat());
        if(jo.getAsJsonPrimitive("mLatitude") != null)
            l.setLatitude(jo.getAsJsonPrimitive("mLatitude").getAsFloat());
        if(jo.getAsJsonPrimitive("mAltitude") != null)
            l.setAltitude(jo.getAsJsonPrimitive("mAltitude").getAsFloat());
        if(jo.getAsJsonPrimitive("mBearing") != null)
            l.setBearing(jo.getAsJsonPrimitive("mBearing").getAsFloat());
        if(jo.getAsJsonPrimitive("mBearingAccuracyDegrees") != null)
            l.setBearingAccuracyDegrees(jo.getAsJsonPrimitive("mBearingAccuracyDegrees").getAsFloat());
        if(jo.getAsJsonPrimitive("mSpeed") != null)
            l.setSpeed(jo.getAsJsonPrimitive("mSpeed").getAsFloat());
        if(jo.getAsJsonPrimitive("mElapsedRealtimeNanos") != null)
            l.setElapsedRealtimeNanos(jo.getAsJsonPrimitive("mElapsedRealtimeNanos").getAsLong());
        if(jo.getAsJsonPrimitive("mSpeedAccuracyMetersPerSecond") != null)
            l.setSpeedAccuracyMetersPerSecond(jo.getAsJsonPrimitive("mSpeedAccuracyMetersPerSecond").getAsFloat());
        if(jo.getAsJsonPrimitive("mTime") != null)
            l.setTime(jo.getAsJsonPrimitive("mTime").getAsLong());
        if(jo.getAsJsonPrimitive("mVerticalAccuracyMeters") != null)
            l.setVerticalAccuracyMeters(jo.getAsJsonPrimitive("mVerticalAccuracyMeters").getAsFloat());
        return l;
    }
}
