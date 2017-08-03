package com.example.jarndt.testingapp.utilities;

import android.content.Context;
import android.util.Log;

import com.example.jarndt.testingapp.MyService;
import com.example.jarndt.testingapp.objects.ListItemObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jarndt on 8/2/17.
 */

public class ListItemCache {
    ////////////// STATIC ///////////////
    private static final String LIST_ITEM_CACHE = "List_Item_Cache";
    private static ListItemCache instance;

    public static ListItemCache getInstance(){
        if(instance == null)
            instance = new ListItemCache();
        return instance;
    }

    public static List<ListItemObject> getListItemObjects(){
        return new ArrayList<>(getInstance()._getListItemObjects().values());
    }

    public static void addListItemObject(ListItemObject listItemObject){
        getInstance()._getListItemObjects().put(listItemObject.getId(),listItemObject);
    }

    public static ListItemObject getListItemObjectById(String id){
        return getInstance()._getListItemObjects().get(id);
    }

    public static void writeToFile(Context context) {
        Log.e(LIST_ITEM_CACHE,"writeToFile");
        FileOptions.writeToFile(context,LIST_ITEM_CACHE,getInstance().toString(),Context.MODE_PRIVATE);
    }

    public static void onCreate(Context context){
        Log.e(LIST_ITEM_CACHE,"onCreate");
        String v = FileOptions.getFileContents(context, LIST_ITEM_CACHE);
        if(v != null)
            instance = new ListItemCache(FileOptions.getGson().fromJson(v, new TypeToken<HashMap<String, ListItemObject>>(){}.getType()));
    }

    //////////// NON STATIC /////////////
    private ListItemCache(){
        gson = FileOptions.getGson();
    }private ListItemCache(HashMap<String, ListItemObject> cache){
        this.listItemObjects = cache;
    }

    private Gson gson;
    private HashMap<String,ListItemObject> listItemObjects;
    private HashMap<String, ListItemObject> _getListItemObjects(){
        if(listItemObjects == null)
            listItemObjects = new HashMap<>();
        return listItemObjects;
    }

    @Override
    public String toString() {
        if(gson == null)
            gson = new Gson();
        return gson.toJson(listItemObjects);
    }
}
