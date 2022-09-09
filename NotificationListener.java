package com.blackcat.baemincalc.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.blackcat.baemincalc.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class NotificationListener extends NotificationListenerService implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        preferences = sharedPreferences;
        editor = preferences.edit();
    }

    private static final class ApplicationPackageNames {
        public static final String WOOWA_RIDER = "com.logistics.rider.woowa";
    }

    public static final class InterceptedNotificationCode {
        public static final int WOOWA_RIDER_CODE = 1;
        public static final int OTHER_NOTIFICATIONS_CODE = 0; // We ignore all notification with code == 4
    }

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        preferences = getSharedPreferences("notification_listener",MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
        editor = preferences.edit();

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (matchNotificationCode(sbn) == InterceptedNotificationCode.WOOWA_RIDER_CODE) {
            Notification notification = sbn.getNotification();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

            Bundle bundle = notification.extras;
            Set<String> keys = bundle.keySet();
            Iterator<String> iterator = keys.iterator();
            JSONObject object = new JSONObject();
            while (iterator.hasNext()) {
                String nextKey = iterator.next();
                if (bundle.get(nextKey) != null) {
                    try {
                        object.put(nextKey,bundle.get(nextKey).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                if (object.getString("android.title").contains("\uD83D\uDD51") && !preferences.getAll().containsValue(object.toString())) {
                        String key = LocalDateTime.now().format(formatter);
                        editor.remove(key).putString(key,object.toString()).apply();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if (packageName.equals(ApplicationPackageNames.WOOWA_RIDER))
            return InterceptedNotificationCode.WOOWA_RIDER_CODE;
        else
            return InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE;
    }
}
