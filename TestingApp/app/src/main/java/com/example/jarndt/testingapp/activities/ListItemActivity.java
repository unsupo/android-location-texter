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
    TextView tv1,tv2,tv3;//, vt1,vt2,vt3;
    EditText vt1,vt2,vt3;
    Button setLocation;
    ListItemObject listItemObject;

    String[] permisions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String target = getIntent().getStringExtra("listItemActivity");
        listItemObject = ListItemCache.getListItemObjectById(target);

        tv1 = (TextView)findViewById(R.id.textView);
        tv2 = (TextView)findViewById(R.id.textView2);
        tv3 = (TextView)findViewById(R.id.textView3);

        vt1 = (EditText)findViewById(R.id.textview6);
        vt2 = (EditText)findViewById(R.id.textView7);
        vt3 = (EditText)findViewById(R.id.textView8);
        vt1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if(testLocation == null)
                        testLocation = new Location("");
                    try {
                        testLocation.setAltitude(Double.parseDouble(vt1.getText().toString()));
                    }catch (NumberFormatException nfe){
                        vt1.setText("");
                    }
                }
            }
        });
        vt2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if(testLocation == null)
                        testLocation = new Location("");
                    try{
                        testLocation.setLatitude(Double.parseDouble(vt2.getText().toString()));
                    }catch (NumberFormatException nfe){
                        vt2.setText("");
                    }
                }
            }
        });
        vt3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if(testLocation == null)
                        testLocation = new Location("");
                    try{
                        testLocation.setLongitude(Double.parseDouble(vt3.getText().toString()));
                    }catch (NumberFormatException nfe){
                        vt3.setText("");
                    }
                }
            }
        });

        if(tv1 != null)
            tv1.setText(getLocation()==null?"Altitude":getLocation().getAltitude()+"");
        if(tv2 != null && getLocation() != null)
            tv2.setText(getLocation()==null?"Longitude":getLocation().getLongitude()+"");
        if(tv3 != null && getLocation() != null)
            tv3.setText(getLocation()==null?"Latitude":getLocation().getLatitude()+"");

        if(listItemObject != null)
            testLocation = listItemObject.getLocation();
        if(vt1 != null)
            vt1.setText(testLocation==null?"Altitude":testLocation.getAltitude()+"");
        if(vt2 != null)
            vt2.setText(testLocation==null?"Longitude":testLocation.getLongitude()+"");
        if(vt3 != null)
            vt3.setText(testLocation==null?"Latitude":testLocation.getLatitude()+"");

        checkLocationPermission();

        setLocation = (Button)findViewById(R.id.button);
        setLocation.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                testLocation = getLocation();
                if(vt1 != null)
                    vt1.setText(testLocation==null?"Altitude":testLocation.getAltitude()+"");
                if(vt2 != null)
                    vt2.setText(testLocation==null?"Longitude":testLocation.getLongitude()+"");
                if(vt3 != null)
                    vt3.setText(testLocation==null?"Latitude":testLocation.getLatitude()+"");
                writeGPS(testLocation);
                if(listItemObject != null)
                    listItemObject.setLocation(testLocation);
            }
        });
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


    public void getLocationChangeCallable(Location location) {
        this.location = location;
        if(tv1 != null)
            tv1.setText(location==null?"Altitude":location.getAltitude()+"");
        if(tv2 != null)
            tv2.setText(location==null?"Longitude":location.getLongitude()+"");
        if(tv3 != null)
            tv3.setText(location==null?"Latitude":location.getLatitude()+"");
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
        getLocationChangeCallable(location);
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
