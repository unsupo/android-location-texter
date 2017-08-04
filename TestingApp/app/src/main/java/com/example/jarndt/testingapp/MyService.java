package com.example.jarndt.testingapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.jarndt.testingapp.activities.MainActivity;
import com.example.jarndt.testingapp.objects.ListItemObject;
import com.example.jarndt.testingapp.sms.SmsDeliveredReceiver;
import com.example.jarndt.testingapp.sms.SmsSentReceiver;
import com.example.jarndt.testingapp.utilities.FileOptions;
import com.example.jarndt.testingapp.utilities.ListItemCache;
import com.google.gson.Gson;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

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

    private static DateTime dateTime;
    private Location sendAtLocation;
    private MyService myService = this;

    public void setSendAtLocation(Location sendAtLocation) {
        this.sendAtLocation = sendAtLocation;
    }

    public Location getLocationFromFile() {
        String fileContent = FileOptions.getFileContents(this, "test_location");
        Location l = new Location("");
        String[] split = fileContent.split(",");
        if(split.length != 3)
            return null;
        l.setAltitude(Double.parseDouble(split[2]));
        l.setLongitude(Double.parseDouble(split[1]));
        l.setLatitude(Double.parseDouble(split[0]));
        return l;
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
//            sendAtLocation = getLocationFromFile();
//            Log.e(TAG, "onLocationChanged: "+sendAtLocation);
            for(ListItemObject listItemObject : ListItemCache.getListItemObjects()) {
                if (listItemObject.isActive() && listItemObject.getLocation() != null && isAtLocation(location, listItemObject.getLocation())) {
                    if (listItemObject.getLastUpdatedDate() != null && listItemObject.getLastUpdatedDate().isBefore(DateTime.now().minusDays(1))) {
                        Log.e(TAG, "onLocationChange: sending sms");
                        FileOptions.sendSMS(MyService.this, listItemObject.getSmsNumber(), listItemObject.getMessage());
                        Log.e(TAG, "Sent SMS");
                        listItemObject.setLastUpdatedDate(DateTime.now());
                    }
                }
            }
            
//            String filename = "gps";
//            String string = new Gson().toJson(location);
//            FileOutputStream outputStream;
//            try {
//                outputStream = openFileOutput(filename, Context.MODE_APPEND);
//                outputStream.write(string.getBytes());
//                outputStream.close();
//                Field pathField = FileOutputStream.class.getDeclaredField("path");
//                pathField.setAccessible(true);
//                String path = (String) pathField.get(outputStream);
//                Log.e(TAG,"onLocationChange: "+path);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
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
        ListItemCache.onCreate(this);
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
}

