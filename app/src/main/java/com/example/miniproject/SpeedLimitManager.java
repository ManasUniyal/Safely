package com.example.miniproject;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

public class SpeedLimitManager {

    private static SpeedLimitManager instance;
    private Context context;
    private Queue<LatLng> locationsQueue;
    private int roadSpeedLimit;
    private int currentRoadSegmentSpeed;
    private LatLng currentLocation;
    private static String locationString;
    private static final int QUEUE_CAPACITY = 20;
    private static final int DEFAULT_SPEED_LIMIT = 45;

    private SpeedLimitManager(Context context) {
        this.context = context.getApplicationContext();
        locationsQueue = new LinkedList<>();
        locationString = new String();
        roadSpeedLimit = DEFAULT_SPEED_LIMIT;
    }

    public static synchronized SpeedLimitManager getInstance(Context context) {
        if (instance == null)
            instance = new SpeedLimitManager(context);
        return instance;
    }

    public void update(LatLng location, int speed) {
        currentRoadSegmentSpeed = speed;
        addLocation(location);
    }

    public void addLocation(LatLng location) {
        this.currentLocation = location;
        if (locationsQueue.size() == QUEUE_CAPACITY) {
            locationsQueue.remove();
            int indexOfDelimiter = locationString.indexOf('|');
            locationString = locationString.substring(indexOfDelimiter + 1);
        }
        locationsQueue.add(location);
        if(!locationString.isEmpty()) {
            locationString += "|";
        }
        locationString += location.latitude + "," + location.longitude;
        Log.e("addLocation", locationString);
        getNearestRoadPlaceId();
    }

    private void getNearestRoadPlaceId() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://roads.googleapis.com/v1/snapToRoads?path=" + locationString + "&" + "interpolate=true" + "&" + "key=" + BuildConfig.roadsAPIKey;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray snappedPoints = response.getJSONArray("snappedPoints");
                    String nearestRoadPlaceId = null;
                    for(int i=snappedPoints.length()-1;i>=0;i--) {
                        try {
                            JSONObject obj = snappedPoints.getJSONObject(i);
                            int originalIndex = obj.getInt("originalIndex");
                            nearestRoadPlaceId = obj.getString("placeId");
                            Log.e("getNearestRoadPlaceId", nearestRoadPlaceId);
                        } catch (Exception e) {
                            continue;
                        }
                    }
                    getRoadSpeedLimit(nearestRoadPlaceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(jsonObjectRequest);
    }

    public int getRoadSpeedLimit(String nearestRoadPlaceId) {
        RequestQueue queue = Volley.newRequestQueue(context);
        if(nearestRoadPlaceId == null) {
            return roadSpeedLimit;
        }
        String url = "https://roads.googleapis.com/v1/speedLimits?placeId=" + nearestRoadPlaceId + "&" + "key=" + BuildConfig.roadsAPIKey;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("speedLimits");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    roadSpeedLimit = jsonObject.getInt("speedLimit");
                    Log.e("getRoadSpeedLimit", String.valueOf(roadSpeedLimit));
                    checkSpeed(roadSpeedLimit);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("RoadAPIManager", String.valueOf(roadSpeedLimit));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(jsonObjectRequest);
        return roadSpeedLimit;
    }

    public void checkSpeed(int roadSpeedLimit) {
        if(currentRoadSegmentSpeed <= roadSpeedLimit) {
            Log.e("CheckSpeed", "Alert User");
            JourneyStatus.getInstance(context).incrementOverSpeedCount();
            updatedDetailedLogs();
            alertUser();
        }
    }

    private void updatedDetailedLogs() {
        //TODO: Do this in a thread
        DetailedLog detailedLogObject = new DetailedLog();
        detailedLogObject.setSummaryId(JourneyStatus.getInstance(context).getLastDetailedLogEntryNumber() + 1);
        detailedLogObject.setLatitude(currentLocation.latitude);
        detailedLogObject.setLongitude(currentLocation.longitude);
        detailedLogObject.setSpeed(currentRoadSegmentSpeed);
        detailedLogObject.setSpeedLimit(roadSpeedLimit);
        detailedLogObject.setDateTime(JourneyStatus.getInstance(context).getDateTime(System.currentTimeMillis()));
        DataBaseHelper.getInstance(context).insertDetailedLog(detailedLogObject);
        Log.e("Speed Limit Manager", detailedLogObject.toString());
        Log.e("Summary log number", String.valueOf(JourneyStatus.getInstance(context).getLastDetailedLogEntryNumber()));
    }

    private void alertUser() {
        AlertUserAudio.getInstance(context).startWarning();
        //TODO: Make entry into database, if condition satisfies (to be decided)
    }
}
