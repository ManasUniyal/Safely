package com.example.miniproject.RequestClasses;

import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.example.miniproject.Utilities.DirectionsJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

public class DirectionsRequest extends Request {

    private Response.Listener listener;

    public DirectionsRequest(int method, String url, Response.Listener listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        try {
            Log.e("DirectionsRequest","Not cached");
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject jsonObject = new JSONObject(json);
            DirectionsJSONParser jsonParser = new DirectionsJSONParser();
            List<List<HashMap<String, String>>> routes = jsonParser.parse(jsonObject);
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
