package com.example.jarndt.testingapp.activities;

/**
 * Created by jarndt on 8/2/17.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jarndt.testingapp.Constants;
import com.example.jarndt.testingapp.MyService;
import com.example.jarndt.testingapp.R;
import com.example.jarndt.testingapp.objects.ListItemObject;
import com.example.jarndt.testingapp.utilities.FileOptions;
import com.example.jarndt.testingapp.utilities.ListItemCache;

public class ListItemActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    Location location, testLocation;
    LocationManager locationManager;
    ListItemObject listItemObject;

    String[] permisions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkLocationPermission();

        String target = getIntent().getStringExtra("listItemActivity");
        listItemObject = ListItemCache.getListItemObjectById(target);

        autoFillListItemObject();

        setButtonListeners();
    }

    private void setButtonListeners() {
        ((Button) findViewById(R.id.saveButton)).setOnClickListener(view ->{
            if(listItemObject == null)
                return;
            Location l = new Location("");
            if(listItemObject.getLocation() != null)
                l = listItemObject.getLocation();
            if(location != null)
                l = location;
            double a = l.getAltitude(),la = l.getLatitude(),lo = l.getLongitude();
            try{ a = Double.parseDouble(((EditText)findViewById(R.id.altitudeEditText)).getText().toString());}catch (Exception e){/*DO NOTHING*/}
            try{ la = Double.parseDouble(((EditText)findViewById(R.id.latitudeEditText)).getText().toString());}catch (Exception e){/*DO NOTHING*/}
            try{ lo = Double.parseDouble(((EditText)findViewById(R.id.longitudeEditText)).getText().toString());}catch (Exception e){/*DO NOTHING*/}
            l.setAltitude(a);
            l.setLatitude(la);
            l.setLongitude(lo);

            listItemObject.setLocation(l);
            listItemObject.setName(((EditText)findViewById(R.id.nameEditText)).getText().toString());
            listItemObject.setSmsNumber(((EditText)findViewById(R.id.phoneNumberEditText)).getText().toString());
            listItemObject.setMessage(((EditText)findViewById(R.id.messageEditText)).getText().toString());
            listItemObject.setActive(((CheckBox)findViewById(R.id.activeCheckBox)).isChecked());

            onBackPressed();
        });
        ((Button) findViewById(R.id.setThisLocation)).setOnClickListener(view -> {
            if(listItemObject == null)
                return;
            listItemObject.setLocation(location);
            autoFillListItemObject(view.getId());
        });
    }

    private void autoFillListItemObject(int...excludeIds) {
        if(listItemObject == null)
            return;

        setLocation(excludeIds);
        setText(R.id.nameEditText,listItemObject.getName(),excludeIds);
        setText(R.id.phoneNumberEditText,listItemObject.getSmsNumber(),excludeIds);
        setText(R.id.messageEditText,listItemObject.getMessage(),excludeIds);

        ((CheckBox)findViewById(R.id.activeCheckBox)).setChecked(listItemObject.isActive());
    }

    private void setText(int id, String name,int...excludeIds) {
        if(isIn(excludeIds,id))
            return;
        View v = findViewById(id);
        if(v instanceof EditText)
            ((EditText)findViewById(id)).setText(name);
        else if(v instanceof TextView)
            ((TextView)findViewById(id)).setText(name);
    }

    private boolean isIn(int[] excludeIds, int id) {
        if(excludeIds == null || excludeIds.length == 0)
            return false;
        for(int i : excludeIds)
            if(id == i)
                return true;
        return false;
    }

    public void setLocation(int...excludeIds) {
        if(listItemObject.getLocation() == null){
            setText(R.id.altitudeEditText,getLocation().getAltitude()+"",excludeIds);
            setText(R.id.latitudeEditText,getLocation().getLatitude()+"",excludeIds);
            setText(R.id.longitudeEditText,getLocation().getLongitude()+"",excludeIds);
        }else {
            setText(R.id.altitudeEditText,listItemObject.getLocation().getAltitude()+"",excludeIds);
            setText(R.id.latitudeEditText,listItemObject.getLocation().getLatitude()+"",excludeIds);
            setText(R.id.longitudeEditText,listItemObject.getLocation().getLongitude()+"",excludeIds);
        }
        if(getLocation() != null) {
            setText(R.id.altitudeTextview,getLocation().getAltitude()+"",excludeIds);
            setText(R.id.latitudeTextView,getLocation().getLatitude()+"",excludeIds);
            setText(R.id.longitudeTextView,getLocation().getLongitude()+"",excludeIds);
        }
    }

    private void writeGPS(Location testLocation) {
        String filename = "test_location";
        String string = testLocation.getLatitude()+","+testLocation.getLongitude()+","+testLocation.getAltitude();//gson.toJson(testLocation);
        FileOptions.writeToFile(this,filename,string,Context.MODE_PRIVATE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean checkLocationPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }public boolean checkSMSPermission() {
        String permission = Manifest.permission.SEND_SMS;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }public boolean checkAllPermissions(){
        for(String s : permisions)
            if(this.checkCallingOrSelfPermission(s) == PackageManager.PERMISSION_DENIED)
                return false;
        return true;
    }

    public Location getLocation() {
        if (location == null) {
            if (checkLocationPermission())
                return getLocationManager().getLastKnownLocation(getLocationManager().getAllProviders().get(0));
            else ActivityCompat.requestPermissions(this, permisions, 1);
        }
        checkLocationPermission();
        return location;
    }
    public LocationManager getLocationManager() {
        if(locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permisions, 1);
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        return locationManager;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        setLocation(new int[]{R.id.altitudeEditText,R.id.latitudeEditText,R.id.longitudeEditText});
    }
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}
    @Override
    public void onProviderEnabled(String s) {}
    @Override
    public void onProviderDisabled(String s) {}


    @Override
    public void onPause(){
        ListItemCache.writeToFile(this);
        super.onPause();
    }

    @Override
    public void onStop(){
        ListItemCache.writeToFile(this);
        super.onStop();
    }
}
