package com.example.covidmeter.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.covidmeter.MainActivity;
import com.example.covidmeter.R;
import com.example.covidmeter.models.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.AclEntry;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    private CallbackManager callbackManager;
    private MutableLiveData<User> info;
    private boolean isLoggedIn;

    public LoginFragment() {
        info = new MutableLiveData<>();
    }


    private void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    private boolean isLoggedIn() {
        return isLoggedIn;
    }

    public MutableLiveData<User> getInfo() {
        return info;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        this.setLoggedIn(accessToken != null && !accessToken.isExpired());

        LoginButton loginButton = view.findViewById(R.id.login_button);
        loginButton.setPermissions(Arrays.asList(
                "public_profile", "user_birthday", "user_gender"));
        callbackManager = CallbackManager.Factory.create();
        loginButton.setFragment(this);

        if (this.isLoggedIn()) {
            userCredentials();
        }
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                userCredentials();
            }

            @Override
            public void onCancel() {
                Log.v("LoginActivity", "cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.v("LoginActivity", Objects.requireNonNull(error.getMessage()));
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    //set the user credentials when they are found
    private void userCredentials() {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.v("LoginActivity Response ", response.toString());
                        try {
                            if (object != null) {
                                String gender;
                                int age = -1;
                                String id = object.getString("id");
                                String name = object.getString("name");
                                if (object.has("gender")) {
                                    gender = object.getString("gender");
                                } else {
                                    gender = "private";
                                }
                                if (object.has("birthday")) {
                                    String aux_birthday = object.getString("birthday");
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                                    LocalDate birthday = LocalDate.parse(aux_birthday, formatter);
                                    age = Period.between(birthday, LocalDate.now()).getYears();
                                }
                                info.setValue(new User(id, name, age, gender));
                            } else {
                                Toast.makeText(MainActivity.getContext(), "Network Connection Error! Restart the app", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender, birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }


}

