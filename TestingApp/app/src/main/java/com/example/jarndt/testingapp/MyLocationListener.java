package com.example.jarndt.testingapp;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

/**
 * Created by jarndt on 7/29/17.
 */

public class MyLocationListener extends Service implements LocationListener {

    public MyLocationListener(Activity context){
        this.context = context;
    }

    Location location;
    Activity context;

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        ((MainActivity)context).getLocationChangeCallable(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
