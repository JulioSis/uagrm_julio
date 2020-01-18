package com.example.julio.apptaller_taller.Firebase;


import android.location.Location;

import com.example.julio.apptaller_taller.Model.User_Mecanico;
import com.example.julio.apptaller_taller.Model.User_Taller;
import com.example.julio.apptaller_taller.Remote.FCMClient;
import com.example.julio.apptaller_taller.Remote.GoogleApi;
import com.example.julio.apptaller_taller.Remote.IFCMService;
import com.example.julio.apptaller_taller.Remote.RetrofitClient;


/**
 * Created by Julio on 18/09/2018.
 */

public class Tablas {

    public static final String token_tbl = "Tokens";

    public static final String talleres_tbl = "Talleres";
    public static final String talleres_location_tbl = "Talleres_location";
    public static final String peticion_tbl = "Peticion_talleres";
    public static final String user_conductores_tbl = "User_Conductores";
    public static final String user_talleres_tbl = "User_Talleres";
    public static final String user_mecanicos_tbl = "User_Mecanicos";
    public static final String mecanicos_location_tbl = "Mecanicos_location";

    public static Location mLastLocation;

    public static final int Pick_Image_Request = 9999;

    public static User_Mecanico currentUser;

    public static  final String baseURL = "https://maps.googleapis.com";
    public static  final String fcmURL = "https://fcm.googleapis.com/";
    public static GoogleApi getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(GoogleApi.class);
    }

    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }


}
