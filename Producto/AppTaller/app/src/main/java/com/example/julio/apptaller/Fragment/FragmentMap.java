package com.example.julio.apptaller.Fragment;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.julio.apptaller.Firebase.Tablas;
import com.example.julio.apptaller.Helper.CustomInfoWindow;
import com.example.julio.apptaller.Model.DataMessage;
import com.example.julio.apptaller.Model.FCMResponse;
import com.example.julio.apptaller.Model.Token;
import com.example.julio.apptaller.Model.User_Mecanico;
import com.example.julio.apptaller.R;
import com.example.julio.apptaller.Remote.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FragmentMap extends Fragment implements OnMapReadyCallback {

    //Location
    private GoogleMap mMap;
    private MapView mapView;
    private View view;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    
    private static final int My_Permission_Request_Code = 7192;

    private LocationRequest mLocationRequest;

    private static int Update_Interval = 5000;
    private static int Fast_Interval = 3000;    //Cada cuanto tiempo se actualiza
    private static int Displacement = 10;  //Se actualiza cada 10 metros

    Marker mCurrent;

    boolean isDriverFound = false;
    String driverId = "";
    int radius = 1;

    int distance = 1;
    float LIMIT = (float) 3;  // 2.2=3

    DatabaseReference mecanicosDisponibles;

    FirebaseDatabase db;
    DatabaseReference mecanico;

    Button btnPickupRequest;

    IFCMService mService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);

        mService = Tablas.getFCMService();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        btnPickupRequest = (Button) view.findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                findDriver();
                if (!isDriverFound){
                    //btnPickupRequest.setText("No hay mecanicos cerca");
                    Toast.makeText(getContext(),"No hay mecanicos cerca",Toast.LENGTH_SHORT).show();
                }else
                    sendPeticionToMechanic(driverId);
            }
        });


        db = FirebaseDatabase.getInstance();
        mecanico = db.getReference(Tablas.user_mecanicos_tbl);

        setUpLocation();
        updateFirebaseToken();
        return view;
    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase .getInstance();
        DatabaseReference tokens = db.getReference(Tablas.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void sendPeticionToMechanic(String driverId) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Tablas.token_tbl);

        tokens.orderByKey().equalTo(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                        {
                            Token token = postSnapShot.getValue(Token.class);
                            String riderToken = FirebaseInstanceId.getInstance().getToken();

                            Map<String,String> content = new HashMap<>();
                            content.put("customer",riderToken);
                            content.put("lat",String.valueOf(Tablas.mLastLocation.getLatitude()));
                            content.put("lng",String.valueOf(Tablas.mLastLocation.getLongitude()));
                            DataMessage dataMessage = new DataMessage(token.getToken(),content);

                            mService.sendMessage(dataMessage)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success == 1)
                                                Toast.makeText(getContext(),"Peticion Enviada",Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(getContext(),"No se puedo enviar la Peticion ",Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
        if (mapView!=null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    private void requestPickupHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Tablas.peticion_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid,new GeoLocation(Tablas.mLastLocation.getLatitude(),Tablas.mLastLocation.getLongitude()));

        if (mCurrent.isVisible())
        {
            mCurrent.remove();
        }

        mCurrent = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Tablas.mLastLocation.getLatitude(),Tablas.mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

        mCurrent.showInfoWindow();
        //btnPickupRequest.setText("Buscando Mecanico");
        //findDriver();
    }

    private void findDriver() {
        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Tablas.mecanicos_location_tbl);
        GeoFire gfDrivers = new GeoFire(drivers);

        GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(Tablas.mLastLocation.getLatitude(),Tablas.mLastLocation.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                if (!isDriverFound)
                {
                    isDriverFound = true;
                    driverId = key;
                    btnPickupRequest.setText("Enviar Peticion al Mecanico");
                    mecanico.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(key).exists()){
                                User_Mecanico mec = dataSnapshot.child(key).getValue(User_Mecanico.class);
                                Toast.makeText(getContext(),""+mec.getNombre(),Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("TAG", "Error!", databaseError.toException());
                        }
                    });
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (!isDriverFound){
                    radius++;
                    findDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void loadAllAvailableDriver(final LatLng location) {
        mMap.clear();
        if (mCurrent != null)
            mCurrent.remove();
        mCurrent = mMap.addMarker(new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

        //Mover Camara
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

        DatabaseReference mechanicLocation = FirebaseDatabase.getInstance().getReference(Tablas.mecanicos_location_tbl);
        GeoFire gf = new GeoFire(mechanicLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(Tablas.mLastLocation.getLatitude(),Tablas.mLastLocation.getLongitude()),distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference(Tablas.user_mecanicos_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User_Mecanico user = dataSnapshot.getValue(User_Mecanico.class);
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude,location.longitude))
                                        .flat(true)
                                        .title(user.getNombre())
                                        .snippet("Telefono: " + user.getTelefono())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.taller)));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <= LIMIT)
                {
                    distance++;
                    loadAllAvailableDriver(location);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (Tablas.mLastLocation != null) {
                            Tablas.mLastLocation = location;
                            mecanicosDisponibles = FirebaseDatabase.getInstance().getReference(Tablas.talleres_location_tbl);
                            mecanicosDisponibles.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    loadAllAvailableDriver(new LatLng(Tablas.mLastLocation.getLatitude(),Tablas.mLastLocation.getLongitude()));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            loadAllAvailableDriver(new LatLng(Tablas.mLastLocation.getLatitude(),Tablas.mLastLocation.getLongitude()));

                        } else {
                            Log.d("Error", "Cannot ger your location");
                        }
                    }
                });

    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, My_Permission_Request_Code);
        } else {
                buildLocationCallBack();
                createLocationRequest();
                displayLocation();
        }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Tablas.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size()-1);
                displayLocation();
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Update_Interval);
        mLocationRequest.setFastestInterval(Fast_Interval); //Actualiza cada fast interval
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(Displacement);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case My_Permission_Request_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpLocation();
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        try{
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.uber_style_map)
            );
            if (!isSuccess)
                Log.e("Error", "No se pudo cargar el estilo del mapa");
        }
        catch (Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getContext()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-17.7832,-63.182),13.0f));

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        createLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
    }
}
