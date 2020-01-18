package com.example.julio.apptaller_taller.Service;

import android.content.Intent;


import com.example.julio.apptaller_taller.CustomerCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Julio on 20/09/2018.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData() != null) {
            Map<String,String> data = new HashMap<>();
            String customer = data.get("customer");
            String lat = data.get("lat");
            String lng = data.get("lng");

            Intent intent = new Intent(getBaseContext(), CustomerCall.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lon", lng);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("customer", customer);
            startActivity(intent);
        }

    }
}
