package com.example.covidmeter.controllers;


import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.covidmeter.MainActivity;
import com.example.covidmeter.models.HealthState;
import com.example.covidmeter.models.Info;
import com.example.covidmeter.models.Symptom;
import com.example.covidmeter.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class SessionController {
    private User user;
    private MutableLiveData<Boolean> registedUser;
    private MutableLiveData<List<Symptom>> symptomList;
    private MutableLiveData<Boolean> updatedState;
    private MutableLiveData<List<Info>> information;

    private static SessionController instance = new SessionController();

    private SessionController() {
        this.information = new MutableLiveData<>();
        this.symptomList = new MutableLiveData<>();
        this.registedUser = new MutableLiveData<>(false);
        this.updatedState = new MutableLiveData<>(false);
    }

    /*
    Getters and Setters
     */
    public static SessionController getInstance() {
        return instance;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public MutableLiveData<Boolean> getRegistedUser() {
        return registedUser;
    }

    public MutableLiveData<List<Symptom>> getSymptomList() {
        return symptomList;
    }

    public MutableLiveData<Boolean> getUpdatedState() {
        return updatedState;
    }

    public MutableLiveData<List<Info>> getInformation() {
        return information;
    }

    /*
        Methods to control all the application
    */

    //starters off AsyncTasks
    public void getActualLocation(double latitude, double longitude) {
        this.user.setLatitude(latitude);
        this.user.setLongitude(longitude);
        new ActualLocation().execute(latitude, longitude);
    }

    public void getAreaInformation() {
        new HandleAreaInformation(this.user.getLatitude(), this.user.getLongitude()).execute();
    }

    public void startAuthentication() {
        new HandleAuthentication().execute();
    }

    private void postUser() {
        new PostUserAsync().execute();
    }

    private void putUser() {
        new PutUserAsync().execute();
    }

    private void startGetSymptomsList() {
        new APISListAsync().execute();
    }

    /*
    Methods to check if user is already on the db
     */
    private void checkUser(Map<String, String> apiUsers, Map<String, HealthState> usersHealthState) {
        startGetSymptomsList();
        if (apiUsers.size() > 0) {
            for (String fbId : apiUsers.values()) {
                if (fbId.equals(this.user.getFbID())) {
                    this.user.setApiID(getApiId(fbId, apiUsers));
                    this.user.setHealthState(usersHealthState.get(fbId));
                    this.registedUser.setValue(true);
                    break;
                }
            }
        }
    }

    private String getApiId(String fbId, Map<String, String> apiUsers) {
        for (String key : apiUsers.keySet()) {
            if (apiUsers.get(key).equals(fbId)) {
                return key;
            }
        }
        return null;
    }

    /*
    methods to update currently User State and update the db
     */
    public void updateUserState(List<Symptom> userSymptoms, String state) {

        HealthState userHealthState = new HealthState(state, userSymptoms);
        this.user.setHealthState(userHealthState);
        if (!this.registedUser.getValue())
            postUser();
        else {
            if (this.user.getApiID() == null) {
                this.user.setApiID(HttpRequest.getINSTANCE().getApiID());
                HttpRequest.getINSTANCE().postStatisticsUpdate(user);
            }
            putUser();
        }
        this.updatedState.setValue(true);
        if (this.information.getValue() == null) {
            getAreaInformation();
        }

    }

    /*
    method to update information near the user
     */
    private void setInformation(int infected, List<Integer> other) {
        Info infects = new Info("Infetados");
        Info withoutSymptoms = new Info("Sem sintomas");
        Info withSymptoms = new Info("Com sintomas");
        Info suspects = new Info("Suspeitos");

        for (Integer number : other) {
            if (number == 0)
                withoutSymptoms.setQtdInfo(withoutSymptoms.getQtdInfo() + 1);
            else if (number < 3)
                withSymptoms.setQtdInfo(withSymptoms.getQtdInfo() + 1);
            else
                suspects.setQtdInfo(suspects.getQtdInfo() + 1);
        }
        infects.setQtdInfo(infected);
        List<Info> aux = new ArrayList<>();

        aux.add(withoutSymptoms);
        aux.add(withSymptoms);
        aux.add(suspects);
        aux.add(infects);
        this.information.setValue(aux);

    }


    /*
    AsyncTask Classes
     */
    private static class HandleAuthentication extends AsyncTask<Void, Void, Void> implements Observer {

        private APIController apiController;

        public HandleAuthentication() {
            apiController = new APIController();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            apiController.addObserver(this);
            apiController.getUsers();
            return null;
        }

        @Override
        public void update(Observable o, Object arg) {
            List<JSONObject> toParse = (List<JSONObject>) arg;
            apiController.deleteObservers();
            Map<String, String> apiUsers = new HashMap<>();
            Map<String, HealthState> usersHealthState = new HashMap<>();
            if (toParse.size() > 0) {
                try {
                    for (JSONObject object : toParse) {
                        apiUsers.put(object.getString("_id"), object.getString("fb_id"));
                        usersHealthState.put(object.getString("fb_id"), new HealthState(object.getString("state"), new ArrayList<Symptom>(object.getInt("qtd_sintomas"))));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            SessionController.getInstance().checkUser(apiUsers, usersHealthState);
        }
    }

    private static class PostUserAsync extends AsyncTask<Void, Void, Void> {

        private APIController apiController;

        public PostUserAsync() {
            apiController = new APIController();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            apiController.postUser(SessionController.getInstance().getUser());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            SessionController.getInstance().registedUser.setValue(true);
        }
    }

    private static class PutUserAsync extends AsyncTask<Void, Void, Void> {

        private APIController apiController;

        public PutUserAsync() {
            apiController = new APIController();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            apiController.putUser(SessionController.getInstance().getUser());
            return null;
        }
    }

    private static class ActualLocation extends AsyncTask<Double, Void, String> {

        @Override
        protected String doInBackground(Double... doubles) {

            double latitude = doubles[0];
            double longitude = doubles[1];

            Geocoder geocoder = new Geocoder(MainActivity.getContext());
            List<Address> addresses = null;
            Address address;

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            address = addresses.get(0);
            Log.d("Cidade", address.getLocality());
            return address.getLocality();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SessionController.getInstance().getUser().setLocation(s);
            if (SessionController.getInstance().getUpdatedState().getValue() && SessionController.getInstance().getInformation().getValue().size() == 0) {
                SessionController.getInstance().getAreaInformation();
            }
        }
    }

    private static class APISListAsync extends AsyncTask<Void, Void, Void> implements Observer {

        private APIController apiController;

        public APISListAsync() {
            apiController = new APIController();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            apiController.addObserver(this);
            apiController.getSymptomsList();
            return null;
        }

        @Override
        public void update(Observable o, Object arg) {
            List<JSONObject> toParse = (List<JSONObject>) arg;
            apiController.deleteObservers();

            List<Symptom> symptoms = new ArrayList<>();

            try {
                for (JSONObject object : toParse) {
                    symptoms.add(new Symptom(object.getString("nome"), object.getString("descricao")));
                }
            } catch (JSONException e) {
                e.getCause();
            }
            SessionController.getInstance().symptomList.setValue(symptoms);
            if (SessionController.getInstance().getUser().getLocation() != null)
                SessionController.getInstance().getAreaInformation();
        }
    }

    private static class HandleAreaInformation extends AsyncTask<Void, Void, Void> implements Observer {

        private APIController apiController;
        private double latitude, longitude;

        public HandleAreaInformation(double latitude, double longitude) {
            apiController = new APIController();
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            apiController.addObserver(this);
            apiController.getUsers();
            return null;
        }

        @Override
        public void update(Observable o, Object arg) {
            List<JSONObject> toParse = (List<JSONObject>) arg;
            apiController.deleteObservers();

            int infected = 0;
            List<Integer> otherSymptoms = new ArrayList<>();
            if (toParse.size() > 0) {
                try {
                    for (JSONObject object : toParse) {
                        if (!object.getString("_id").equals(SessionController.getInstance().getUser().getApiID())) {
                            if (inRange(object.getDouble("latitude"), object.getDouble("longitude"))) {
                                String state = object.getString("state");
                                if (state.equals("Infetado")) {
                                    infected++;
                                } else {
                                    otherSymptoms.add(object.getInt("qtd_sintomas"));
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            SessionController.getInstance().setInformation(infected, otherSymptoms);
        }

        private boolean inRange(double latitude, double longitude) {
            float[] results = new float[1];
            Location.distanceBetween(this.latitude, this.longitude, latitude, longitude, results);
            float distanceInMeters = results[0];
            return distanceInMeters < 15000;
        }
    }
}
