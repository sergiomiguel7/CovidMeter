package com.example.covidmeter.controllers;


import android.util.Log;



import com.example.covidmeter.models.User;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

public class APIController extends Observable implements Observer {
    private final String USERS_URL = "https://covidmeter-687f.restdb.io/rest/usertable";
    private final String SYMPTOMS_URL ="https://covidmeter-687f.restdb.io/rest/sintomastable";
    private HttpRequest httpRequest = HttpRequest.getINSTANCE();

    public APIController() {
    }

    //metodos
    public void getUsers() {
        httpRequest.addObserver(this);
        httpRequest.setUrl(USERS_URL);
        httpRequest.getRequest();
    }

    public void postUser(User user){
        httpRequest.setUrl(USERS_URL);
        httpRequest.postRequest(user);
    }

    public void putUser(User user) {
        String url = USERS_URL.concat(String.format(Locale.getDefault(),"/%s", user.getApiID()));
        Log.d("postUrl", url);
        httpRequest.setUrl(url);
        httpRequest.putRequest(user);
    }

    public void getSymptomsList(){
        httpRequest.addObserver(this);
        httpRequest.setUrl(SYMPTOMS_URL);
        httpRequest.getRequest();
    }


    @Override
    public void update(Observable o, Object arg) {
        List<JSONObject> objects = (List<JSONObject>) arg;
        for (JSONObject j : objects) {
            Log.d("response", j.toString());
        }
        httpRequest.clearAll();
        httpRequest.deleteObservers();
        setChanged();
        notifyObservers(objects);
        objects =new ArrayList<>();
    }


}
