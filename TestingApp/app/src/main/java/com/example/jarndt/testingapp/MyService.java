package com.example.jarndt.testingapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.jarndt.testingapp.sms.SmsDeliveredReceiver;
import com.example.jarndt.testingapp.sms.SmsSentReceiver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.danlew.android.joda.DateUtils;
import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by jarndt on 7/29/17.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class MyService extends Service{
    public class ServiceBinder extends Binder{
        public MyService getMyService(){
            return MyService.this;
        }
    }

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    public static boolean IS_SERVICE_RUNNING = false;

    Gson gson = new Gson();
    public Location getLocationFromFile() {
        StringBuffer fileContent = new StringBuffer("");
        FileInputStream fis;
        boolean b = false;
        for(String s : fileList())
            if("test_location".equals(s))
                b = true;
        if(!b)
            return null;

        try {
            fis = openFileInput("test_location");
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

    private static DateTime dateTime;
    private Location sendAtLocation;

    public void setSendAtLocation(Location sendAtLocation) {
        this.sendAtLocation = sendAtLocation;
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            sendAtLocation = getLocationFromFile();
            Log.e(TAG, "onLocationChanged: "+sendAtLocation);
            if(sendAtLocation != null && isAtLocation(location,sendAtLocation)) {
                Log.e(TAG,"onLocationChange: sending sms");
                if(dateTime != null && dateTime.isBefore(DateTime.now().minusDays(1))) {
//                    sendSMS("8018311017", "At the gym");
                    Log.e(TAG,"Sent SMS");
                    dateTime = DateTime.now();
                }
            }
            
            String filename = "gps";
            String string = new Gson().toJson(location);
            FileOutputStream outputStream;
            try {
                outputStream = openFileOutput(filename, Context.MODE_APPEND);
                outputStream.write(string.getBytes());
                outputStream.close();
                Field pathField = FileOutputStream.class.getDeclaredField("path");
                pathField.setAccessible(true);
                String path = (String) pathField.get(outputStream);
                Log.e(TAG,"onLocationChange: "+path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    private boolean isAtLocation(Location location, Location sendAtLocation) {
        Log.e(TAG,"Comparing: "+location+" \n\t"+sendAtLocation);
        double l1 = sendAtLocation.getLongitude(), l2 = sendAtLocation.getLatitude(),
                ll1 = location.getLongitude(), ll2 = location.getLatitude(),
                deltaX = 1e-3, deltaY = 1e-3;
        return Math.abs(l1-ll1) < deltaX && Math.abs(l2-ll2) < deltaY;

    }

    private double round(double d, int places){
        return Math.round(d*Math.pow(10,places))/Math.pow(10,places);
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        showNotification();
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        JodaTimeAndroid.init(this);
        dateTime = DateTime.now().minusYears(10);
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, MyService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, MyService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, MyService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("TutorialsFace Music Player")
                .setTicker("TutorialsFace Music Player")
                .setContentText("My song")
                .setSmallIcon(R.drawable.ic_launcher)
//                .setLargeIcon(Bitmap.createScaledBitmap(icon, 24, 24, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous",
                        ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play",
                        pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next",
                        pnextIntent).build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);

    }
    /*
     * BroadcastReceiver mBrSend; BroadcastReceiver mBrReceive;
     */
    private void sendSMS(String phoneNumber, String message) {
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(this, SmsSentReceiver.class), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(this, SmsDeliveredReceiver.class), 0);
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
            Toast.makeText(getBaseContext(), "SMS sending failed...", Toast.LENGTH_SHORT).show();
        }

    }
}

