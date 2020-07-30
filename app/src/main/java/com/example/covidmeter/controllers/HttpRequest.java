package com.example.covidmeter.controllers;


import android.util.Log;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import com.example.covidmeter.MainActivity;

import com.example.covidmeter.models.Symptom;
import com.example.covidmeter.models.User;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;


public class HttpRequest extends Observable {
    private String url;
    private List<JSONObject> objects;
    private String apiID;

    private static HttpRequest INSTANCE = new HttpRequest();

    public HttpRequest() {
        this.objects = new ArrayList<>();
    }

    public static HttpRequest getINSTANCE() {
        return INSTANCE;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiID() {
        return apiID;
    }

    public void clearAll() {
        this.objects = new ArrayList<>();
    }

    void getRequest() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, INSTANCE.url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<JSONObject> result = new ArrayList<>();
                        try {

                            List<JSONObject> temp = new ArrayList<>();
                            for (int i = 0; i < response.length(); i++) {
                                temp.add(response.getJSONObject(i));
                            }
                            result.addAll(temp);
                            INSTANCE.objects.addAll(result);
                            setChanged();
                            notifyObservers(objects);
                            Log.d("json size", String.valueOf(temp.size()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("x-apikey", "b510a13d59f8b87e9bf89e0075f1885b47224");
                params.put("cache-control", "no-cache");
                return params;
            }
        };
        MainActivity.getRequestQueue().add(request);

    }

    public void postRequest(final User user) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("fb_id", user.getFbID());
            jsonBody.put("name", user.getName());
            jsonBody.put("age", String.valueOf(user.getAge()));
            jsonBody.put("gender", user.getGender());
            jsonBody.put("state", user.getHealthState().getState());
            if (!user.getHealthState().getSymptoms().get(0).getName().equals("Sem sintomas"))
                jsonBody.put("qtd_sintomas", user.getHealthState().getSymptoms().size());
            else
                jsonBody.put("qtd_sintomas", 0);
            jsonBody.put("latitude", user.getLatitude());
            jsonBody.put("longitude", user.getLongitude());

            Log.d("body", jsonBody.toString());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, INSTANCE.url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("postResponse", response.toString());
                            try {
                                apiID = response.getString("_id");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("content-type", "application/json");
                    params.put("x-apikey", "b510a13d59f8b87e9bf89e0075f1885b47224");
                    params.put("cache-control", "no-cache");
                    return params;
                }

            };
            MainActivity.getRequestQueue().add(request);
        } catch (JSONException e) {
            e.getCause();
        }
    }

    public void putRequest(User user) {
        postStatisticsUpdate(user);
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("fb_id", user.getFbID());
            jsonBody.put("name", user.getName());
            jsonBody.put("age", String.valueOf(user.getAge()));
            jsonBody.put("gender", user.getGender());
            jsonBody.put("state", user.getHealthState().getState());
            if (!user.getHealthState().getSymptoms().get(0).getName().equals("Sem sintomas"))
                jsonBody.put("qtd_sintomas", user.getHealthState().getSymptoms().size());
            else
                jsonBody.put("qtd_sintomas", 0);
            jsonBody.put("latitude", user.getLatitude());
            jsonBody.put("longitude", user.getLongitude());

            Log.d("body", jsonBody.toString());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, INSTANCE.url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("putResponse", response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("content-type", "application/json");
                    params.put("x-apikey", "b510a13d59f8b87e9bf89e0075f1885b47224");
                    params.put("cache-control", "no-cache");
                    return params;
                }

            };
            MainActivity.getRequestQueue().add(request);
        } catch (JSONException e) {
            e.getCause();
        }
    }

    public void postStatisticsUpdate(User user) {
        String url = "https://covidmeter-687f.restdb.io/rest/usersdata";

        try {
            StringBuilder symptoms = new StringBuilder();
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("iduser", user.getApiID());
            jsonBody.put("state", user.getHealthState().getState());
            for(Symptom s : user.getHealthState().getSymptoms()) {
                symptoms.append(s.getName()).append("\n");
            }
            jsonBody.put("symptoms_list", symptoms.toString());
            jsonBody.put("date", LocalDateTime.now());

            Log.d("body", jsonBody.toString());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("postResponse", response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("content-type", "application/json");
                    params.put("x-apikey", "b510a13d59f8b87e9bf89e0075f1885b47224");
                    params.put("cache-control", "no-cache");
                    return params;
                }

            };
            MainActivity.getRequestQueue().add(request);
        } catch (JSONException e) {
            e.getCause();
        }
    }
}
