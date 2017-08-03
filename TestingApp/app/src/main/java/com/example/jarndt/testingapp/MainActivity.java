package com.example.jarndt.testingapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.jarndt.testingapp.sms.SmsDeliveredReceiver;
import com.example.jarndt.testingapp.sms.SmsSentReceiver;
import com.example.jarndt.testingapp.utilities.FileOptions;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    Location location, testLocation;
    LocationManager locationManager;
    TextView tv1,tv2,tv3;//, vt1,vt2,vt3;
    EditText vt1,vt2,vt3;
    Button setLocation;

    String[] permisions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS};

    public static MainActivity instance;
    public static MainActivity getInstance(){
        return instance;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv1 = (TextView)findViewById(R.id.textView);
        tv2 = (TextView)findViewById(R.id.textView2);
        tv3 = (TextView)findViewById(R.id.textView3);

        vt1 = (EditText) findViewById(R.id.textview6);
        vt2 = (EditText)findViewById(R.id.textView7);
        vt3 = (EditText)findViewById(R.id.textView8);
        vt1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if(testLocation == null)
                        testLocation = new Location("");
                    testLocation.setAltitude(Double.parseDouble(vt1.getText().toString()));
                }
            }
        });
        vt2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if(testLocation == null)
                        testLocation = new Location("");
                    testLocation.setLatitude(Double.parseDouble(vt2.getText().toString()));
                }
            }
        });
        vt3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if(testLocation == null)
                        testLocation = new Location("");
                    testLocation.setLongitude(Double.parseDouble(vt3.getText().toString()));
                }
            }
        });

        if(tv1 != null)
            tv1.setText(getLocation()==null?"Altitude":getLocation().getAltitude()+"");
        if(tv2 != null && getLocation() != null)
            tv2.setText(getLocation()==null?"Longitude":getLocation().getLongitude()+"");
        if(tv3 != null && getLocation() != null)
            tv3.setText(getLocation()==null?"Latitude":getLocation().getLatitude()+"");

        testLocation = getLocationFromFile();
        if(vt1 != null)
            vt1.setText(testLocation==null?"Altitude":testLocation.getAltitude()+"");
        if(vt2 != null)
            vt2.setText(testLocation==null?"Longitude":testLocation.getLongitude()+"");
        if(vt3 != null)
            vt3.setText(testLocation==null?"Latitude":testLocation.getLatitude()+"");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action: "+
                        (location == null ? "None!" : location.getAltitude()+" "+location.getLongitude()+" "+location.getLatitude()), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(checkLocationPermission())
            startServices();

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
            }
        });
        instance = this;
    }

    private void startServices() {
        final Intent service = new Intent(MainActivity.this, MyService.class);
        if (!MyService.IS_SERVICE_RUNNING) {
            service.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            MyService.IS_SERVICE_RUNNING = true;
        } else {
            service.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            MyService.IS_SERVICE_RUNNING = false;
        }
        startService(service);
    }

    private void writeGPS(Location testLocation) {
        String filename = "test_location";
        String string = testLocation.getLatitude()+","+testLocation.getLongitude()+","+testLocation.getAltitude();//gson.toJson(testLocation);
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
            Field pathField = FileOutputStream.class.getDeclaredField("path");
            pathField.setAccessible(true);
            String path = (String) pathField.get(outputStream);
            Log.e("MainActivity","writeGPS: "+path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        if(checkLocationPermission())
            startServices();
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
}
