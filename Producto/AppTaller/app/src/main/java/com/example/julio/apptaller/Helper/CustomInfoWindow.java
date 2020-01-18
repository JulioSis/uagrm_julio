package com.example.julio.apptaller.Helper;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.julio.apptaller.Firebase.Tablas;
import com.example.julio.apptaller.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

/**
 * Created by Julio on 19/09/2018.
 */

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    View myView;
    TextView nombre, telefono;
    ImageView foto;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    public CustomInfoWindow(Context context) {
        myView = LayoutInflater.from(context)
                .inflate(R.layout.custom_rider_info_window,null);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        nombre = (TextView)myView.findViewById(R.id.txtPickupInfo);
        marker.setTitle(Tablas.currentUser.getName());
        nombre.setText(marker.getTitle());

        telefono = (TextView)myView.findViewById(R.id.txtPickupSnippet);
        marker.setSnippet(Tablas.currentUser.getTelefono());
        telefono.setText(marker.getSnippet());

        foto = (ImageView) myView.findViewById(R.id.imageCircle);
        if (Tablas.currentUser.getImageName() != null && !TextUtils.isEmpty(Tablas.currentUser.getImageName())) {
            Picasso.get()
                    .load(Tablas.currentUser.getImageName())
                    .into(foto);
        }

        return  myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
