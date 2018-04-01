package com.example.dq.gradesafe;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by DQ on 3/19/2018.
 */

public abstract class Maintenance {

    public static <E> void saveArrayList(Activity activity, ArrayList<E> list, String sharedPreferencesName, String key){
        SharedPreferences prefs = activity.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public static <E> ArrayList<E> loadArrayList(Activity activity, Class<E> dataClass, String sharedPreferencesName, String key){
        SharedPreferences prefs = activity.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        ArrayList<E> arrayList = gson.fromJson(json, getType(ArrayList.class, dataClass));
        return arrayList == null ? new ArrayList<E>() : arrayList;
    }

    private static Type getType(final Class<?> rawClass, final Class<?> parameter) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] {parameter};
            }
            @Override
            public Type getRawType() {
                return rawClass;
            }
            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}