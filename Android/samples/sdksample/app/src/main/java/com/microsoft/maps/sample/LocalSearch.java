package com.microsoft.maps.sample;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.microsoft.maps.Geolocation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class LocalSearch {

    private static final String URL_ENDPOINT
            = "https://dev.virtualearth.net/REST/v1/LocalSearch/"
            + "?query={query}"
            + "&userMapView=47.551145,-122.437374,47.694152,-122.086498"
            + "&key=" + BuildConfig.CREDENTIALS_KEY;

    static class Poi {
        String name;
        Geolocation location;
    }

    interface Callback {
        void onSuccess(List<Poi> results);
        void onFailure();
    }

    static void sendRequest(Context context, String query, Callback callback) {
        if (query == null || query.isEmpty()) {
            Toast.makeText(context, "Invalid query", Toast.LENGTH_LONG).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(new StringRequest(
            Request.Method.GET,
            URL_ENDPOINT.replace("{query}", query),
            (String response) -> {
                List<Poi> results = parse(response);
                if (results == null) {
                    callback.onFailure();
                    return;
                }
                callback.onSuccess(results);
            },
            (VolleyError error) -> callback.onFailure()
        ));
    }

    private static List<Poi> parse(String json) {
        List<Poi> results = new ArrayList<>();

        try {
            JSONObject responseJObj = new JSONObject(json);
            JSONArray resourceSetsJArr = responseJObj.getJSONArray("resourceSets");
            JSONObject resourceSetJObj = resourceSetsJArr.getJSONObject(0);
            JSONArray resourcesJArr = resourceSetJObj.getJSONArray("resources");

            for (int i = 0; i < resourcesJArr.length(); ++i) {
                JSONObject resourceJObj = resourcesJArr.getJSONObject(i);
                Poi poi = new Poi();

                poi.name = resourceJObj.getString("name");

                JSONObject pointJObj = resourceJObj.getJSONObject("point");
                JSONArray coordinatesJArr = pointJObj.getJSONArray("coordinates");
                poi.location = new Geolocation(
                        coordinatesJArr.getDouble(0),
                        coordinatesJArr.getDouble(1));

                results.add(poi);
            }
        }
        catch (Exception ex) {
            return null;
        }

        return results;
    }

}
