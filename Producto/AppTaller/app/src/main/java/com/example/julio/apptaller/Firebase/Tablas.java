package com.example.julio.apptaller.Firebase;


import android.location.Location;

import com.example.julio.apptaller.Model.User;
import com.example.julio.apptaller.Remote.FCMClient;
import com.example.julio.apptaller.Remote.IFCMService;


/**
 * Created by Julio on 18/09/2018.
 */

public class Tablas {

    public static final String talleres_tbl = "Talleres";
    public static final String talleres_location_tbl = "Talleres_location";
    public static final String peticion_tbl = "Peticion_talleres";
    public static final String user_conductores_tbl = "User_Conductores";
    public static final String user_vehiculo_tbl = "User_Vehiculo";
    public static final String user_mecanicos_tbl = "User_Mecanicos";
    public static final String mecanicos_location_tbl = "Mecanicos_location";
    public static final String token_tbl = "Tokens";

    public static final int Pick_Image_Request = 9999;

    public static User currentUser;

    public static Location mLastLocation;

    public static  final String fcmURL = "https://fcm.googleapis.com/";

    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }


}
