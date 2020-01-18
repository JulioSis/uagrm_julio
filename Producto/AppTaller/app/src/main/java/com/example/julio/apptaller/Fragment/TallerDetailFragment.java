package com.example.julio.apptaller.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.julio.apptaller.Firebase.Tablas;
import com.example.julio.apptaller.Model.Taller;
import com.example.julio.apptaller.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class TallerDetailFragment extends Fragment implements OnMapReadyCallback {

    TextView nombre,direccion,horario, telefono;
    ImageView imagen;
    FirebaseDatabase db;
    DatabaseReference taller;

    private GoogleMap mMap;
    private MapView mapView;

    private double Latitud, Longitud;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_taller_detail, container, false);
        db = FirebaseDatabase.getInstance();
        taller = db.getReference(Tablas.talleres_tbl);
        nombre = (TextView) v.findViewById(R.id.Nombre);
        direccion = (TextView) v.findViewById(R.id.Direccion);
        horario = (TextView) v.findViewById(R.id.Horario);
        telefono = (TextView) v.findViewById(R.id.Telefono);
        imagen = (ImageView) v.findViewById(R.id.Image);
        final String Nombre = getArguments().getString("Nombre");
        final String Direccion = getArguments().getString("Direccion");
        final String Horario = getArguments().getString("Horario");
        final String Imagen = getArguments().getString("Imagen");
        final String Telefono = getArguments().getString("Telefono");
        Latitud = Double.valueOf(getArguments().getString("Latitud"));
        Longitud = Double.valueOf(getArguments().getString("Longitud"));
        nombre.setText(Nombre);
        direccion.setText(Direccion);
        horario.setText(Horario);
        telefono.setText(Telefono);
        Picasso.get().load(Imagen).into(imagen);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.mapa);
        if (mapView!=null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.addMarker(new MarkerOptions().position(new LatLng(Latitud, Longitud)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Latitud,Longitud),13.0f));
    }
}
