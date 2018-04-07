/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.notify;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.tech.NfcB;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.dertyp7214.apkmirror.R;
import com.dertyp7214.apkmirror.components.BottomPopup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Notifications {

    public final String TITLE, SUBTITLE, TIME;
    private BottomPopup notificationPopup;

    public Notifications(String title, String subTitle, String time){
        this.TITLE=title;
        this.SUBTITLE=subTitle;
        this.TIME=time;
    }

    public static List<Notifications> getNotificationsList(Context context){
        SharedPreferences preferences = context.getSharedPreferences("notify", Context.MODE_PRIVATE);
        try {
            List<Notifications> notifications = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(preferences.getString("notiJSON", "[]"));
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                notifications.add(new Notifications(object.getString("title"), object.getString("sub_title"), object.getString("time")));
            }
            return notifications;
        } catch (JSONException ignored) {}
        return new ArrayList<>();
    }

    public static void addNotification(Notifications notifications, Context context){
        List<Notifications> notificationsList = getNotificationsList(context);
        notificationsList.add(notifications);
        setNotifications(notificationsList, context);
    }

    public static void setNotifications(List<Notifications> notifications, Context context){
        StringBuilder json = new StringBuilder("[");
        for(int i=0;i<notifications.size();i++){
            json.append("{\"title\":\"").append(notifications.get(i).TITLE).append("\",\"sub_title\":\"").append(notifications.get(i).SUBTITLE).append("\",\"time\":\"").append(notifications.get(i).TIME).append("\"}");
            if(i<notifications.size()-1)
                json.append(",");
        }
        json.append("]");
        context.getSharedPreferences("notify", Context.MODE_PRIVATE).edit().putString("notiJSON", json.toString()).apply();
    }

    public void onClick(int currentColor, Activity activity, View parent, View root_layout){
        notificationPopup = new BottomPopup(currentColor, parent, activity);
        notificationPopup.setUp(root_layout, R.layout.notification_popup);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            notificationPopup.setText(Html.fromHtml("<h2>"+TITLE+"</h2><br><p>"+SUBTITLE+"</p><br><p>"+TIME+"</p>", Html.FROM_HTML_MODE_LEGACY));
        else
            notificationPopup.setText(Html.fromHtml("<h2>"+TITLE+"</h2><br><p>"+SUBTITLE+"</p><br><p>"+TIME+"</p>"));
        notificationPopup.show();
    }
}