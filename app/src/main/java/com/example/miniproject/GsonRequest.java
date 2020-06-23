package com.example.miniproject;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

public class GsonRequest extends Request {

    private Response.Listener listener;

    public GsonRequest(int method, String url, Response.Listener listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//            Gson gson = new Gson();
            JSONObject jsonObject = new JSONObject(json);
            DirectionsJSONParser jsonParser = new DirectionsJSONParser();
            List<List<HashMap<String, String>>> routes = jsonParser.parse(jsonObject);
//            Type collectionType = new TypeToken<List<List<HashMap<String, String>>>>(){}.getType();
//            Log.e("GsonRequest", json);
//            List<List<HashMap<String, String>>> routes = gson.fromJson(json, collectionType);
//            Cache.Entry routesCacheEntry = new Cache.Entry();
//            routesCacheEntry.data = routes.toString().getBytes();
            return Response.success (routes, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(Object response) {
        listener.onResponse(response);
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
