package com.example.miniproject.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.miniproject.Utilities.AlertUserAudio;
import com.example.miniproject.BuildConfig;
import com.example.miniproject.RequestClasses.DirectionsRequest;
import com.example.miniproject.SingletonClasses.JourneyStatus;
import com.example.miniproject.Utilities.ObjectSerialiserDeserialiser;
import com.example.miniproject.R;
import com.example.miniproject.SingletonClasses.SpeedLimitManager;
import com.example.miniproject.SingletonClasses.VolleySingleton;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, PlaceSelectionListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private Location lastKnownLocation;
    private AutocompleteSupportFragment autocompleteFragment;
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    final String TAG = "Volley from UI thread";
    private LocationRequest mLocationRequest;
    private LatLng source;
    private LatLng destination;
    private Polyline line = null;
    private Marker sourceMarker = null;
    private Marker destinationMarker = null;
    private LatLng previousLocation;
    private static final int DEFAULT_TIME_GAP = 10000;
    private BottomNavigationView bottomNavigationView;
    private Button journeyStateButton;
    private TextView currentSpeedTextView;
//    private long previousTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Places.initialize(getApplicationContext(), BuildConfig.mapsAPIKey);
        placesClient = Places.createClient(this);

        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(this);

        VolleySingleton.getInstance(getApplicationContext()).getCache().clear();

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.maps);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.camera:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.maps:
                        return true;
                    case R.id.logs:
                        startActivity(new Intent(getApplicationContext(), DrivingLogs.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        journeyStateButton = findViewById(R.id.journeyStateButton);
        JourneyStatus.getInstance(getApplicationContext()).setJourneyStateButton(journeyStateButton, bottomNavigationView, MapsActivity.this);
        journeyStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JourneyStatus.getInstance(MapsActivity.this).updateJourneyLog(journeyStateButton, bottomNavigationView, MapsActivity.this);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("Map","Loaded");
        mMap = googleMap;
        getLocationPermission();
        getDeviceLocation();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(DEFAULT_TIME_GAP);
        mLocationRequest.setFastestInterval(DEFAULT_TIME_GAP);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if(locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                LatLng latLngSource = new LatLng(location.getLatitude(), location.getLongitude());
                source = latLngSource;
                double segmentDistance = getDistance(source, previousLocation);
                JourneyStatus.getInstance(getApplicationContext()).updateDistance(segmentDistance);
                int speed = getSpeed(segmentDistance);
                SpeedLimitManager.getInstance(getApplicationContext()).update(source,speed);
                Log.e("Speed", String.valueOf(speed));
                previousLocation = source;
//                previousTime = System.currentTimeMillis();
                Log.e("Location", source.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(sourceMarker != null)
                            sourceMarker.remove();
                        if(lastKnownLocation == null)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, DEFAULT_ZOOM));
                        sourceMarker = mMap.addMarker(new MarkerOptions().position(source).title("Current location"));
                        Log.e("Marker","Added at source");
                    }
                });
            }
        }
    };

    private void displayRoadSegmentSpeed(final int speed) {
        currentSpeedTextView = findViewById(R.id.currentSpeedTextView);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentSpeedTextView.setText(speed + " km/h");
            }
        });
    }

    private double getDistance(LatLng newPosition, LatLng oldPosition) {
        assert (newPosition!=null && oldPosition!=null);
        float[] results = new float[1];
        Location.distanceBetween(oldPosition.latitude, oldPosition.longitude, newPosition.latitude, newPosition.longitude, results);
        return results[0];
    }

    //TODO: Improve the logic for calculating speed
    private int getSpeed(double distance) {
//        long endTime = System.currentTimeMillis();
//        long time = endTime - previousTime;
        final double speed = (distance/DEFAULT_TIME_GAP) * 1000 * 3.6;  //speed in km/h
        displayRoadSegmentSpeed((int) Math.floor(speed));
        Log.e("Speed", String.valueOf(speed));
        return (int) Math.floor(speed);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // API key
        String key = "key=" + BuildConfig.mapsAPIKey;

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    //TODO: Use proper naming for class DirectionsRequest
    public void drawRoute(LatLng source, LatLng destination) {

//        final String url = "https://maps.googleapis.com/maps/api/directions/json?origin=30.3165,78.0325&destination=28.7041,77.1025&sensor=false&key=" + BuildConfig.mapsAPIKey;
        final String url = getDirectionsUrl(source, destination);
        Cache.Entry entry = VolleySingleton.getInstance(getApplicationContext()).getCache().get(url);
        if(entry == null || entry.data == null) {
            DirectionsRequest routeRequest = new DirectionsRequest(Request.Method.GET, url, new Response.Listener() {
                @Override
                public void onResponse(Object response) {
                    Log.e("Data", response.toString());
                    Cache.Entry newEntry = new Cache.Entry();
                    try {
                        newEntry.data = ObjectSerialiserDeserialiser.objToByte(response);
                        VolleySingleton.getInstance(getApplicationContext()).getCache().put(url, newEntry);
                        new SetDirectionOnMap().execute(newEntry.data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Volley");
                }
            });
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(routeRequest);
        } else {
            byte[] response = VolleySingleton.getInstance(getApplicationContext()).getCache().get(url).data;
            new SetDirectionOnMap().execute(response);
        }
    }

    private class SetDirectionOnMap extends AsyncTask<byte[], Void, PolylineOptions>{

        @Override
        protected PolylineOptions doInBackground(byte[]... bytes) {

            byte[] response = bytes[0];
            List<List<HashMap<String, String>>> result = null;
            try {
                result = (List<List<HashMap<String, String>>>) ObjectSerialiserDeserialiser.byteToObj((byte[]) response);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);
            }
            return lineOptions;
        }

        @Override
        protected void onPostExecute(PolylineOptions polylineOptions) {
            if(polylineOptions != null) {
//                mMap.clear();
                if(line != null)
                    line.remove();
                final PolylineOptions finalLineOptions = polylineOptions;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(line != null)
                            line.remove();
                        line = mMap.addPolyline(finalLineOptions);
                        if(destinationMarker != null)
                            destinationMarker.remove();
                        destinationMarker = mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
                    }
                });
            } else {
                Log.d(TAG,"without Polylines drawn");
            }
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                ((Task) locationResult).addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            LatLng latLngSource = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            source = latLngSource;
                            previousLocation = source;
//                            previousTime = System.currentTimeMillis();
//                            Log.e("Last location", lastKnownLocation.toString());
                            if (lastKnownLocation != null) {
                                if(sourceMarker != null)
                                    sourceMarker.remove();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                sourceMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude())).title("Current location"));
                            } else {
                                Toast.makeText(getApplicationContext(), "Getting current location", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e("Device last location","Not found");
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //TODO: Handle the case when user denies permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Location permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onPlaceSelected(@NonNull Place place) {
        Log.e("Place searched", "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng());
        destination = place.getLatLng();
        drawRoute(source, destination);
    }

    @Override
    public void onError(@NonNull Status status) {
        Log.e("Place searched", status.toString());
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
        AlertUserAudio.getInstance(MapsActivity.this).endWarning();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFusedLocationProviderClient != null){
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//            previousTime = System.currentTimeMillis();
        }
    }

}