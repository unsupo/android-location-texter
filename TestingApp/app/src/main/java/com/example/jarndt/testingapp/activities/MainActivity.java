package com.example.jarndt.testingapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.jarndt.testingapp.Constants;
import com.example.jarndt.testingapp.MyService;
import com.example.jarndt.testingapp.R;
import com.example.jarndt.testingapp.objects.ListItemObject;
import com.example.jarndt.testingapp.utilities.ListItemCache;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String[] permisions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createUI();

        if(checkAllPermissions())
            startServices();
    }

    private void createUI() {
//        Arrays.asList(fileList()).forEach(a->deleteFile(a));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ListItemCache.onCreate(this);
        listItem();
    }

    private int LIST_ITEMS_ID = "List Items arraylist id".hashCode();

    private Gson gson = new Gson();
    private void listItem() {
        ListView listView = (ListView) findViewById(R.id.list_item);
        ArrayList<ListItemObject> listItems = new ArrayList<>(ListItemCache.getListItemObjects());
        ArrayAdapter<ListItemObject> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);

        ((SwipeMenuListView)findViewById(R.id.list_item)).setMenuCreator(new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_delete));
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        });
        ((SwipeMenuListView)findViewById(R.id.list_item)).setOnMenuItemClickListener((position, menu, index)-> {
//            ApplicationInfo item = mAppList.get(position);
            switch (index) {
                case 0:
                    // open
                    openItem(listItems,position);
                    break;
                case 1:
                    // delete
//					delete(item);
                    ListItemCache.deleteListItemById(listItems.get(position).getId());
                    listItems.remove(position);
                    adapter.notifyDataSetChanged();
                    break;
            }
            return false;
        });

        findViewById(R.id.fab).setOnClickListener(view-> {
            ListItemObject listItemObject = new ListItemObject();
            ListItemCache.addListItemObject(listItemObject);
            listItems.add(listItemObject);
            adapter.notifyDataSetChanged();
            Intent myIntent = new Intent(MainActivity.this,
                    ListItemActivity.class);
            myIntent.putExtra("listItemActivity",listItemObject.getId());
            startActivity(myIntent);
        });

        listView.setOnItemClickListener((parent, view, position, id)-> {
            openItem(listItems,position);
        });
        listView.smoothScrollToPosition(0);
    }

    private void openItem(ArrayList<ListItemObject> listItems, int position) {
        Intent myIntent = new Intent(this,
                ListItemActivity.class);
        myIntent.putExtra("listItemActivity",listItems.get(position).getId());
        startActivity(myIntent);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
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
    protected void onResume() {
        super.onResume();
        ((ListView) findViewById(R.id.list_item)).invalidateViews();
    }

    @Override
    public void onPause(){
        ListItemCache.writeToFile(this);
        Log.e(this.getLocalClassName(),"onPause");
        super.onPause();
    }

    @Override
    public void onStop(){
        ListItemCache.writeToFile(this);
        Log.e(this.getLocalClassName(),"onStop");
        super.onStop();
    }
}
