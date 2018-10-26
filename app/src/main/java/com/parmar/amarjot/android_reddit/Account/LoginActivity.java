package com.parmar.amarjot.android_reddit.Account;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parmar.amarjot.android_reddit.FeedAPI;
import com.parmar.amarjot.android_reddit.R;
import com.parmar.amarjot.android_reddit.URLS;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LoginActivity extends AppCompatActivity{

    private static final String TAG = "LoginActivity";
    private URLS urls = new URLS();
    private ProgressBar mProgressBar;
    private EditText mUsername;
    private EditText mPassword;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: started.");
        final Button btnLogin  = findViewById(R.id.btn_login);
        mPassword = findViewById(R.id.input_password);
        mUsername = findViewById(R.id.input_username);
        mProgressBar = findViewById(R.id.loginRequestLoadingProgressBar);
        mProgressBar.setVisibility(View.GONE);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Attempting to log in.");
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                if(!username.equals("") && !password.equals("")){
                    mProgressBar.setVisibility(View.VISIBLE);
                    //method for signing in
                    login(username, password);
                }
            }
        });

    }

    private void login(final String username, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urls.LOGIN_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FeedAPI feedAPI = retrofit.create(FeedAPI.class);

        // Make header
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");

        // Make sign in request
        Call<CheckLogin> call = feedAPI.signIn(headerMap, username, username, password, "json");

        // Request sign in
        call.enqueue(new Callback<CheckLogin>() {
            @Override
            public void onResponse(Call<CheckLogin> call, Response<CheckLogin> response) {
                Log.d(TAG, "onResponse: Server Response: " + response.toString());
                Log.d(TAG, "onResponse: login: " + response.toString());
            }

            @Override
            public void onFailure(Call<CheckLogin> call, Throwable t) {
                Log.e(TAG, "onFailure: unable to login: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        });
    }
}











