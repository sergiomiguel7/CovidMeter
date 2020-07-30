package com.example.covidmeter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.covidmeter.controllers.SessionController;
import com.example.covidmeter.models.Info;
import com.example.covidmeter.models.Symptom;
import com.example.covidmeter.models.User;
import com.example.covidmeter.views.HomeScreenFragment;
import com.example.covidmeter.views.LoginFragment;
import com.example.covidmeter.views.UpdateStateFragment;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity  {

    private static Context context;

    private LoginFragment loginFragment;
    private UpdateStateFragment updateStateFragment;
    private HomeScreenFragment homeScreenFragment;
    private SessionController sessionController;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        sessionController = SessionController.getInstance();
        loginFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, loginFragment).commit();

        loginFragment.getInfo().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                sessionController.setUser(user);
                getCurrentLocation();
                sessionController.startAuthentication();
                sessionController.getSymptomList().observe(MainActivity.this, new Observer<List<Symptom>>() {
                    @Override
                    public void onChanged(List<Symptom> symptoms) {
                        //when the user is logged with fb and the symptoms list ir ready new fragment is called
                        if(symptoms.size()>0) {
                            changeFragment(symptoms);
                            sessionController.getSymptomList().removeObserver(this);
                        }
                    }
                });
            }
        });
    }

    public static Context getContext() {
        return context;
    }

    public static RequestQueue getRequestQueue() {
        return Volley.newRequestQueue(getContext());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(isLocationEnabled()){
            getCurrentLocation();
        }
        else {
            Toast.makeText(context, "App needs location to work!", Toast.LENGTH_SHORT).show();

        }
    }


    public void changeFragment(List<Symptom> symptoms) {

        updateStateFragment = new UpdateStateFragment(symptoms);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(loginFragment.getId(),updateStateFragment);
        ft.commit();

        sessionController.getUpdatedState().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean updated) {
                sessionController.getInformation().observe(MainActivity.this, new Observer<List<Info>>() {
                    @Override
                    public void onChanged(List<Info> infos) {
                        if(updated && infos.size()>0){
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            homeScreenFragment = new HomeScreenFragment(SessionController.getInstance().getUser());
                            ft.replace(updateStateFragment.getId(), homeScreenFragment, "dashboard").addToBackStack("dashboard");
                            ft.commit();
                            sessionController.getInformation().removeObserver(this);
                        }
                    }
                });
            }
        });
    }

    public void updateUserSymptoms(View view) {
        updateStateFragment.updateUserSymptoms(view);
    }

    public void callUpdateFragment(View view){
        updateStateFragment = new UpdateStateFragment(SessionController.getInstance().getSymptomList().getValue());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(homeScreenFragment.getId(), updateStateFragment,"update").addToBackStack("update");
        ft.commit();
    }


    /*
    Methods referring to location methods
     */

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    public void getCurrentLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                final LocationRequest locationRequest = new LocationRequest();
                locationRequest.setInterval(10000);
                locationRequest.setFastestInterval(3000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationServices.getFusedLocationProviderClient(this)
                        .requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                        .removeLocationUpdates(this);
                                if (locationResult != null && locationResult.getLocations().size() > 0) {
                                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                                    sessionController.getActualLocation(locationResult.getLocations().get(latestLocationIndex).getLatitude(), locationResult.getLocations().get(latestLocationIndex).getLongitude());
                                }
                            }
                        }, Looper.getMainLooper());
            } else {
                Toast.makeText(MainActivity.getContext(), "Ligue a localização", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        } else
            requestPermissions();
    }



}
