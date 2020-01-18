package com.example.julio.apptaller_taller;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.julio.apptaller_taller.Firebase.Tablas;
import com.example.julio.apptaller_taller.Helper.DirectionJSONParser;
import com.example.julio.apptaller_taller.Remote.GoogleApi;
import com.example.julio.apptaller_taller.Remote.IFCMService;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MechanicTracking extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private GoogleMap mMap;

    double riderLat, riderLng;
    String customerId;

    private static final int Play_Services_Res_Request = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int Update_Interval = 5000;
    private static int Fast_Interval = 3000;
    private static int Displacement = 10;

    private Circle clienteMarker;

    private Marker mechanicMarker;

    private Polyline direction;

    GoogleApi mService;
    IFCMService mFCMService;

    GeoFire geoFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (getIntent() != null)
        {
            riderLat = getIntent().getDoubleExtra("lat",-1.0);
            riderLng = getIntent().getDoubleExtra("lng",-1.0);
            customerId = getIntent().getStringExtra("customerId");
        }

        mService = Tablas.getGoogleAPI();

        setUpLocation();
    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED   ){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED   ){
            return;
        }
        Tablas.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Tablas.mLastLocation!=null)
        {
            final double latitud = Tablas.mLastLocation.getLatitude();
            final double longitud = Tablas.mLastLocation.getLongitude();
            if (mechanicMarker != null)
                mechanicMarker.remove();
            mechanicMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitud,longitud))
                    .title("You")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud,longitud),17.0f));

            if (direction != null)
                direction.remove();
            getDirection();
        }
        else{
            Log.d("Error","Cannot ger your location");
        }
    }

    private void getDirection() {
        LatLng currentPosition = new LatLng(Tablas.mLastLocation.getLatitude(), Tablas.mLastLocation.getLongitude());

        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+riderLat+","+riderLng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api2);
            Log.d("EDMTDEV",requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                new ParserTask().execute(response.body().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(MechanicTracking.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setUpLocation() {
        if (checkPlayServices())
        {
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();
        }

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Update_Interval);
        mLocationRequest.setFastestInterval(Fast_Interval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(Displacement);
    }

    private boolean checkPlayServices() {
        int resultcode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultcode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultcode))
                GooglePlayServicesUtil.getErrorDialog(resultcode,this,Play_Services_Res_Request).show();
            else {
                Toast.makeText(this, "Este dispositivo no tiene soporte", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try{
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_style_map)
            );
            if (!isSuccess)
                Log.e("Error", "No se pudo cargar el estilo del mapa");
        }
        catch (Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }

        clienteMarker = mMap.addCircle(new CircleOptions()
                .center(new LatLng(riderLat,riderLng))
                .radius(10)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Tablas.mLastLocation = location;
        displayLocation();
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String, String>>>>
    {
        ProgressDialog mDialog = new ProgressDialog(MechanicTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Por favor espere");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String,String>>> routes = null;
            try{
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jObject);
            } catch (JSONException e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();
            ArrayList points = null;
            PolylineOptions polylineOptions = null;
            for (int i=0; i<lists.size(); i++)
            {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                List<HashMap<String,String>> path = lists.get(i);
                for (int j=0; j<path.size(); j++)
                {
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);

                    points.add(position);
                }
                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }
}
